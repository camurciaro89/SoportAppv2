package com.example.soportapp.data.service

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Servicio encargado de la comunicación con el modelo de lenguaje Gemini de Google
 * para generar diagnósticos técnicos preliminares.
 *
 * @param apiKey Clave secreta para acceder a la API de Google AI.
 */
class AiDiagnosisService(apiKey: String) {

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 32
            topP = 1f
            maxOutputTokens = 500
        },
        systemInstruction = content { 
            text("""
                Eres un experto en soporte técnico de computadores (hardware y software) para la plataforma SoportApp.
                Tu objetivo es proporcionar un DIAGNÓSTICO PRELIMINAR breve, profesional y útil basado en la descripción del problema del usuario.
                
                Instrucciones:
                1. Saluda cordialmente.
                2. Menciona explícitamente que este es un diagnóstico preliminar y no definitivo.
                3. Analiza el problema y menciona las posibles causas (máximo 3).
                4. Determina una PRIORIDAD sugerida (Baja, Media, Alta) basada en la gravedad del problema.
                5. Sugiere una acción inmediata si aplica.
                6. Si la información es insuficiente, sugiere qué más podría preguntar el técnico (ej: ¿Hubo un corte de luz? ¿Se derramó líquido?).
                7. Sé conciso (máximo 150 palabras).
                8. Usa un tono que transmita tranquilidad.
                9. Idioma: Español.
                
                Formato de salida sugerido:
                - Diagnóstico: ...
                - Posibles causas: ...
                - Prioridad sugerida: [Baja/Media/Alta]
                - Acción inmediata: ...
            """.trimIndent())
        }
    )

    suspend fun generateDiagnosis(problemDescription: String): String? = withContext(Dispatchers.IO) {
        if (problemDescription.isBlank()) return@withContext null
        
        try {
            val response = model.generateContent("El usuario reporta el siguiente problema: $problemDescription")
            response.text
        } catch (e: Exception) {
            "Lo siento, en este momento no puedo generar un diagnóstico automático. Un técnico revisará tu caso pronto."
        }
    }
}
