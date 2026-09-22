import httpx
import os
import json
import logging
from typing import List, Dict

logger = logging.getLogger(__name__)

class AIService:
    def __init__(self):
        # Configuración Ollama (Local)
        self.ollama_url = os.getenv("OLLAMA_URL", "http://ollama:11434/api/generate")
        self.model_local = os.getenv("OLLAMA_MODEL", "llama3")

        # Configuración Groq (Externa - Gratuita)
        self.groq_api_key = os.getenv("GROQ_API_KEY", "")
        self.groq_url = "https://api.groq.com/openai/v1/chat/completions"
        self.model_external = "llama-3.1-70b-versatile"

        self.timeout = float(os.getenv("OLLAMA_TIMEOUT", "120"))

    async def is_available(self) -> bool:
        """Verifica si la IA está disponible (Groq o Ollama)."""
        if self.groq_api_key:
            return True

        tags_url = self.ollama_url.replace("/api/generate", "/api/tags")
        try:
            async with httpx.AsyncClient() as client:
                response = await client.get(tags_url, timeout=5.0)
                return response.status_code == 200
        except Exception:
            return False

    async def _call_ai(self, prompt: str) -> str:
        """Llamada inteligente: Usa Groq si hay API Key, si no, usa Ollama."""
        if self.groq_api_key:
            return await self._call_groq(prompt)
        return await self._call_ollama(prompt)

    async def _call_groq(self, prompt: str) -> str:
        """Llamada a la API de Groq (rápida y gratuita)."""
        async with httpx.AsyncClient() as client:
            try:
                headers = {
                    "Authorization": f"Bearer {self.groq_api_key}",
                    "Content-Type": "application/json"
                }
                payload = {
                    "model": self.model_external,
                    "messages": [{"role": "user", "content": prompt}],
                    "temperature": 0.7
                }
                response = await client.post(self.groq_url, headers=headers, json=payload, timeout=30.0)

                if response.status_code == 200:
                    return response.json()['choices'][0]['message']['content']
                else:
                    logger.error(f"Error Groq: {response.status_code} - {response.text}")
                    return "Error al conectar con Groq."
            except Exception as e:
                logger.error(f"Excepción Groq: {str(e)}")
                return f"IA externa no disponible: {str(e)}"

    async def _call_ollama(self, prompt: str) -> str:
        """Llamada a la API de Ollama (local)."""
        async with httpx.AsyncClient() as client:
            try:
                response = await client.post(
                    self.ollama_url,
                    json={
                        "model": self.model_local,
                        "prompt": prompt,
                        "stream": False
                    },
                    timeout=self.timeout
                )
                if response.status_code == 200:
                    return response.json().get("response", "")
                return "Error al conectar con la IA local."
            except Exception as e:
                return f"IA local no disponible: {str(e)}"

    async def generate_diagnosis(self, problem_description: str) -> str:
        """Diagnóstico corto inicial."""
        return await self.generate_final_diagnosis(problem_description, [])

    async def get_clarifying_questions(self, problem_description: str) -> List[str]:
        prompt = f"""
        Eres un experto en soporte técnico de computadores. Un usuario reporta el siguiente problema: "{problem_description}"
        Genera exactamente 3 preguntas cortas, técnicas y directas que ayudarían a un experto a determinar si la falla es de hardware, software o externa.

        Reglas:
        - Solo las preguntas.
        - Sin introducciones ni saludos.
        - Idioma: Español.
        - Formato de salida: una lista JSON de strings.
        Ejemplo: ["¿Pregunta 1?", "¿Pregunta 2?", "¿Pregunta 3?"]
        """

        response_text = await self._call_ai(prompt)
        try:
            start = response_text.find("[")
            end = response_text.rfind("]") + 1
            if start != -1 and end != 0:
                questions = json.loads(response_text[start:end])
                return [str(q) for q in questions[:3]]
            return ["¿Desde cuándo ocurre el problema?", "¿Has realizado cambios recientes?", "¿Ves algún mensaje de error?"]
        except:
            return ["¿Desde cuándo ocurre el problema?", "¿Has realizado cambios recientes?", "¿Ves algún mensaje de error?"]

    async def generate_final_diagnosis(self, problem_description: str, qa_history: List[Dict[str, str]]):
        answers_str = "\n".join([f"P: {item['question']} R: {item['answer']}" for item in qa_history])

        prompt = f"""
        Eres un experto en soporte técnico para SoportApp.
        Contexto inicial: {problem_description}
        Respuestas del usuario:
        {answers_str}

        Analiza la información y proporciona un diagnóstico técnico estructurado.

        Reglas de formato (Salida OBLIGATORIA en JSON):
        {{
            "diagnostico": "Resumen técnico corto",
            "causas": ["causa 1", "causa 2", "causa 3"],
            "prioridad": "Baja|Media|Alta",
            "accion": "Acción inmediata sugerida",
            "servicio": "Servicio recomendado del catálogo"
        }}

        Idioma: Español. Máximo 150 palabras en total.
        """

        return await self._call_ai(prompt)

ai_service = AIService()
