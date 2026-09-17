import httpx
import os
import json
from typing import List, Dict

class AIService:
    def __init__(self):
        self.ollama_url = os.getenv("OLLAMA_URL", "http://ollama:11434/api/generate")
        self.model = os.getenv("OLLAMA_MODEL", "llama3")

    async def _call_ollama(self, prompt: str) -> str:
        async with httpx.AsyncClient() as client:
            try:
                response = await client.post(
                    self.ollama_url,
                    json={
                        "model": self.model,
                        "prompt": prompt,
                        "stream": False
                    },
                    timeout=30.0
                )
                if response.status_code == 200:
                    return response.json().get("response", "")
                return "Error al conectar con la IA local."
            except Exception as e:
                return f"IA no disponible: {str(e)}"

    async def get_clarifying_questions(self, problem_description: str) -> List[str]:
        """
        Genera preguntas basadas en la descripción inicial del usuario.
        """
        prompt = f"""
        Eres un experto en soporte técnico. Un usuario reporta: "{problem_description}"
        Genera exactamente 3 preguntas cortas y técnicas que ayudarían a un técnico a entender mejor el problema.
        No saludes, no des introducciones. Solo las preguntas.
        Formato de salida: una lista JSON de strings.
        Ejemplo: ["¿Pregunta 1?", "¿Pregunta 2?", "¿Pregunta 3?"]
        """

        response_text = await self._call_ollama(prompt)
        try:
            # Intentar extraer el JSON de la respuesta
            start = response_text.find("[")
            end = response_text.rfind("]") + 1
            if start != -1 and end != 0:
                questions = json.loads(response_text[start:end])
                return questions[:4] # Máximo 4
            return ["¿Desde cuándo ocurre el problema?", "¿Has realizado algún cambio reciente?", "¿Hay algún mensaje de error?"]
        except:
            return ["¿Desde cuándo ocurre el problema?", "¿Has realizado algún cambio reciente?", "¿Hay algún mensaje de error?"]

    async def generate_final_diagnosis(self, problem_description: str, qa_history: List[Dict[str, str]]):
        """
        Genera el diagnóstico final basado en la descripción y las respuestas.
        """
        answers_str = "\n".join([f"P: {item['question']} R: {item['answer']}" for item in qa_history])

        prompt = f"""
        Eres un experto en soporte técnico de computadores para SoportApp.
        Contexto del problema: {problem_description}
        Respuestas del usuario:
        {answers_str}

        Proporciona un DIAGNÓSTICO PRELIMINAR profesional.
        Incluye:
        1. Diagnóstico breve.
        2. 3 Posibles causas.
        3. Prioridad (Baja, Media, Alta).
        4. Acción inmediata.
        5. Servicio sugerido del catálogo (ej: Mantenimiento Preventivo, Reparación de Software, Soporte de Redes).

        Responde en español con un tono profesional y tranquilo. Máximo 150 palabras.
        """

        return await self._call_ollama(prompt)

ai_service = AIService()
