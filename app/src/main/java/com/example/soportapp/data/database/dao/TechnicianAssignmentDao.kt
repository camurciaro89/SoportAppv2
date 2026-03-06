package com.example.soportapp.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.soportapp.data.database.TechnicianAssignment

@Dao
interface TechnicianAssignmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assignment: TechnicianAssignment)
}
