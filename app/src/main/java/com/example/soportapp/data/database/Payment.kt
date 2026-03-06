package com.example.soportapp.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "pagos",
    foreignKeys = [
        ForeignKey(
            entity = SupportRequest::class,
            parentColumns = ["id_solicitud"],
            childColumns = ["id_solicitud"]
        )
    ]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_pago")
    val id: Int = 0,

    @ColumnInfo(name = "id_solicitud", index = true)
    val supportRequestId: Int = 0,

    val monto: Double = 0.0,

    @ColumnInfo(name = "estado_pago")
    val paymentStatus: String = "", // 'Éxito', 'Fallido'

    @ColumnInfo(name = "metodo_pago")
    val paymentMethod: String = "",

    @ColumnInfo(name = "estado_transaccion")
    val transactionState: String = "", // 'Pendiente', 'Exitosa', 'Rechazada'

    @ColumnInfo(name = "fecha_pago", defaultValue = "CURRENT_TIMESTAMP")
    val paymentDate: String = "",

    @ColumnInfo(name = "referencia_bancaria")
    val bankReference: String = ""
)
