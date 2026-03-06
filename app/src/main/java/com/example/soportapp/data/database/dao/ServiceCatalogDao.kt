package com.example.soportapp.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.soportapp.data.database.ServiceCatalog

@Dao
interface ServiceCatalogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(services: List<ServiceCatalog>)

    @Query("SELECT * FROM servicios_catalogo")
    suspend fun getAllServices(): List<ServiceCatalog>

    @Query("SELECT * FROM servicios_catalogo WHERE categoria_id = :categoryId")
    suspend fun getServicesByCategory(categoryId: String): List<ServiceCatalog>

    @Query("SELECT * FROM servicios_catalogo WHERE id_servicio = :serviceId")
    suspend fun getServiceById(serviceId: String): ServiceCatalog?
}
