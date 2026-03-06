package com.example.soportapp.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.soportapp.data.database.SupportRequest

@Dao
interface SupportRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(supportRequest: SupportRequest): Long

    @Update
    suspend fun update(supportRequest: SupportRequest)

    @Query("SELECT * FROM solicitudes_soporte WHERE id_solicitud = :id")
    suspend fun getRequestById(id: Long): SupportRequest?

    @Query("UPDATE solicitudes_soporte SET id_usuario = :userId WHERE id_solicitud = :supportRequestId")
    suspend fun associateUserToRequest(supportRequestId: Long, userId: Int)

    @Query("UPDATE solicitudes_soporte SET id_tecnico = :technicianId WHERE id_solicitud = :supportRequestId")
    suspend fun assignTechnicianToRequest(supportRequestId: Long, technicianId: Int)
}
