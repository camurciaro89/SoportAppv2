package com.example.soportapp.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equipos")
data class Equipment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "id_usuario", index = true)
    val userId: Int? = null,
    
    val tipo: String = "", // Laptop, Desktop, Servidor, etc.
    val marca: String = "",
    val modelo: String = "",
    
    @ColumnInfo(name = "numero_serial")
    val serialNumber: String = "",
    
    @ColumnInfo(name = "fecha_adquisicion")
    val acquisitionDate: String = "",
    
    @ColumnInfo(name = "sistema_operativo")
    val operatingSystem: String = "",
    
    val especificaciones: String = "",
    
    @ColumnInfo(name = "creado_en", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: String = ""
)
