package com.example.soportapp.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "asignaciones_actuales",
    foreignKeys = [
        ForeignKey(
            entity = SupportRequest::class,
            parentColumns = ["id_solicitud"],
            childColumns = ["id_solicitud"]
        ),
        ForeignKey(
            entity = Technician::class,
            parentColumns = ["id_tecnico"],
            childColumns = ["id_tecnico"]
        )
    ]
)
data class TechnicianAssignment(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_asignacion")
    val id: Int = 0,

    @ColumnInfo(name = "id_solicitud", index = true)
    val supportRequestId: Int,

    @ColumnInfo(name = "id_tecnico", index = true)
    val technicianId: Int,

    @ColumnInfo(name = "fecha_asignacion", defaultValue = "CURRENT_TIMESTAMP")
    val assignmentDate: String,

    @ColumnInfo(name = "estado_asignacion")
    val assignmentStatus: String // 'ASIGNADO', 'EN_PROCESO', 'COMPLETADO'
)
