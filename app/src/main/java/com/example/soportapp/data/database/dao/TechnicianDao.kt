package com.example.soportapp.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.soportapp.data.database.Technician

@Dao
interface TechnicianDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(technician: Technician)

    @Query("SELECT * FROM tecnicos WHERE id_tecnico = :id")
    suspend fun getTechnicianById(id: Int): Technician?
}
