package com.example.soportapp.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tecnicos")
data class Technician(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_tecnico")
    val id: Int = 0,

    val nombre: String,

    val especialidad: String,

    @ColumnInfo(name = "foto_url")
    val photoUrl: String,

    @ColumnInfo(name = "titulo_profesional")
    val professionalTitle: String,

    @ColumnInfo(name = "is_verificado")
    val isVerified: Boolean,

    @ColumnInfo(name = "rating_promedio")
    val averageRating: Float,

    @ColumnInfo(name = "total_servicios")
    val totalServices: Int
)
