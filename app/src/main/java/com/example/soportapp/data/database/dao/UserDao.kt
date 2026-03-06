package com.example.soportapp.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.soportapp.data.database.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("SELECT * FROM usuarios WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?

    @Query("SELECT * FROM usuarios WHERE telefono = :phone")
    suspend fun getUserByPhone(phone: String): User?
}
