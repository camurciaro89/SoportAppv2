import httpx
import os

class AIService:
    def __init__(self):
        self.ollama_url = os.getenv("OLLAMA_URL", "http://ollama:11434/api/generate")
        self.model = os.getenv("OLLAMA_MODEL", "llama3")

    async def generate_diagnosis(self, problem_description: str):
        prompt = f"""
        Eres un experto en soporte técnico de computadores para la plataforma SoportApp.
        Tu objetivo es proporcionar un DIAGNÓSTICO PRELIMINAR breve, profesional y útil basado en la descripción del problema del usuario.

        Instrucciones:
        1. Saluda cordialmente.
        2. Menciona explícitamente que este es un diagnóstico preliminar y no definitivo.
        3. Analiza el problema y menciona las posibles causas (máximo 3).
        4. Determina una PRIORIDAD sugerida (Baja, Media, Alta) basada en la gravedad del problema.
        5. Sugiere una acción inmediata si aplica.
        6. Sé conciso (máximo 150 palabras).
        7. Idioma: Español.

        Descripción del problema: {problem_description}

        Responde con este formato exacto:
        - Diagnóstico: ...
        - Posibles causas: ...
        - Prioridad: [Baja/Media/Alta]
        - Acción inmediata: ...
        """

        async with httpx.AsyncClient() as client:
            try:
                response = await client.post(
                    self.ollama_url,
                    json={
                        "model": self.model,
                        "prompt": prompt,
                        "stream": False
                    },
                    timeout=10.0
                )
                if response.status_code == 200:
                    return response.json().get("response")
                return "Error al conectar con la IA local."
            except Exception as e:
                return f"IA no disponible: {str(e)}"

ai_service = AIService()
