package com.example.soportapp.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String = "",
    val telefono: String = "",
    @ColumnInfo(name = "tipo_usuario")
    val userType: String = "",
    @ColumnInfo(name = "creado_en", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: String = ""
)
