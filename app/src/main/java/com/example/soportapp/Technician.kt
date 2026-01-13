package com.example.soportapp

data class Review(
    val userName: String,
    val rating: Int,
    val comment: String,
    val date: String
)

data class Technician(
    val id: String,
    val name: String,
    val title: String,
    val experience: String,
    val bio: String,
    val totalServices: Int,
    val reviews: List<Review>
) {
    val averageRating: Double
        get() = if (reviews.isEmpty()) 0.0 else reviews.map { it.rating }.average()
}

object TechnicianRepository {
    // Aquí es donde en el futuro conectarás con Firebase o una API REST
    fun getMainTechnician(): Technician {
        return Technician(
            id = "camilo-murcia",
            name = "Camilo Andrés Murcia Romero",
            title = "Ingeniero de Sistemas (9no Semestre)",
            experience = "Más de 15 años de experiencia técnica",
            bio = "Especialista en mantenimiento preventivo y correctivo de equipos de cómputo con amplia trayectoria en soluciones corporativas y de hogar.",
            totalServices = 542,
            reviews = listOf(
                Review("Juan Pérez", 5, "Excelente servicio, muy puntual y profesional.", "15/10/2023"),
                Review("María García", 5, "Resolvió un problema de red que nadie más pudo. Muy recomendado.", "02/11/2023"),
                Review("Carlos Ruiz", 4, "Muy buen trabajo con el mantenimiento de mi laptop.", "20/11/2023"),
                Review("Ana Martínez", 5, "Camilo es muy honesto y explica todo claramente.", "05/12/2023")
            )
        )
    }
}
