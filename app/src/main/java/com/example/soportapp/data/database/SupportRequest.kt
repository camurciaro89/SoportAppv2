package com.example.soportapp.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "solicitudes_soporte")
data class SupportRequest(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_solicitud")
    val id: Int = 0,

    @ColumnInfo(name = "id_usuario", index = true)
    val userId: Int? = null,

    @ColumnInfo(name = "id_tecnico", index = true)
    val technicianId: Int? = null,

    @ColumnInfo(name = "id_servicio_catalogo", index = true)
    val serviceCatalogId: String = "",

    @ColumnInfo(name = "descripcion_problema")
    val problemDescription: String = "",

    val ubicacion: String = "",

    val modalidad: String = "",

    val estado: String = "Pendiente",

    @ColumnInfo(name = "fecha_creacion", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: String = "",

    @ColumnInfo(name = "direccion_servicio")
    val serviceAddress: String = "",

    @ColumnInfo(name = "dia_sugerido")
    val suggestedDay: String = "",

    @ColumnInfo(name = "hora_sugerida")
    val suggestedTime: String = "",

    @ColumnInfo(name = "es_festivo_excepcional")
    val isHolidayException: Boolean = false,

    val pagado: Boolean = false,

    val reembolsado: Boolean = false,

    @ColumnInfo(name = "modalidad_sugerida")
    val suggestedModality: String = "",

    @ColumnInfo(name = "sede_asignada")
    val assignedBranch: String? = null,

    @ColumnInfo(name = "servicio_nombre_snapshot")
    val serviceNameSnapshot: String = "",

    @ColumnInfo(name = "descripcion_final")
    val finalDescription: String = "",

    @ColumnInfo(name = "direccion_confirmada")
    val confirmedAddress: String = "",

    @ColumnInfo(name = "estado_solicitud")
    val requestStatus: String = "POR_PAGAR",

    @ColumnInfo(name = "tipo_cliente_id")
    val clientTypeId: String = ""
)
