package com.example.soportapp.data.database.dao

import androidx.room.*
import com.example.soportapp.data.database.Equipment
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipos WHERE id_usuario = :userId ORDER BY creado_en DESC")
    fun getEquipmentsByUserId(userId: Int): Flow<List<Equipment>>

    @Query("SELECT * FROM equipos WHERE id = :id")
    suspend fun getEquipmentById(id: Int): Equipment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(equipment: Equipment): Long

    @Update
    suspend fun update(equipment: Equipment)

    @Delete
    suspend fun delete(equipment: Equipment)
}
