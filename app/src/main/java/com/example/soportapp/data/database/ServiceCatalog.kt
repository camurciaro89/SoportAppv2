package com.example.soportapp.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "servicios_catalogo")
data class ServiceCatalog(
    @PrimaryKey
    @ColumnInfo(name = "id_servicio")
    val serviceId: String,

    @ColumnInfo(name = "categoria_id")
    val categoryId: String, // 'hogar', 'empresa'

    @ColumnInfo(name = "nombre_visible")
    val visibleName: String,

    val description: String,

    @ColumnInfo(name = "modality_info")
    val modalityInfo: String,
    
    @ColumnInfo(name = "icono_name")
    val iconName: String,

    @ColumnInfo(name = "hex_color")
    val hexColor: String,

    @ColumnInfo(name = "hex_bg")
    val hexBg: String
)
