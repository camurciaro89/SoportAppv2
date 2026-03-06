package com.example.soportapp.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "evidencia_fotos",
    foreignKeys = [
        ForeignKey(
            entity = SupportRequest::class,
            parentColumns = ["id_solicitud"],
            childColumns = ["id_solicitud"]
        )
    ]
)
data class EvidencePhoto(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_foto")
    val id: Int = 0,

    @ColumnInfo(name = "id_solicitud", index = true)
    val supportRequestId: Int,

    @ColumnInfo(name = "url_almacenamiento")
    val storageUrl: String,

    @ColumnInfo(name = "fecha_subida", defaultValue = "CURRENT_TIMESTAMP")
    val uploadDate: String
)
