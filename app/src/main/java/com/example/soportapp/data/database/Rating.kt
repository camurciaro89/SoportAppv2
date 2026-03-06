package com.example.soportapp.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "calificaciones",
    foreignKeys = [
        ForeignKey(
            entity = SupportRequest::class,
            parentColumns = ["id_solicitud"],
            childColumns = ["id_solicitud"]
        )
    ]
)
data class Rating(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_rating")
    val id: Int = 0,

    @ColumnInfo(name = "id_solicitud", index = true)
    val supportRequestId: Int,

    val puntuacion: Int,

    val comentario: String?,

    @ColumnInfo(name = "fecha_calificacion", defaultValue = "CURRENT_TIMESTAMP")
    val ratingDate: String
)
