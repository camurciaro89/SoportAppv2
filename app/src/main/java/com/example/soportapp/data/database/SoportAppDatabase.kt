package com.example.soportapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.soportapp.data.database.dao.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        ServiceCatalog::class,
        SupportRequest::class,
        Technician::class,
        Payment::class,
        Rating::class,
        EvidencePhoto::class,
        TechnicianAssignment::class
    ],
    version = 8, // Subimos a versión 8 para forzar actualización
    exportSchema = false
)
abstract class SoportAppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun serviceCatalogDao(): ServiceCatalogDao
    abstract fun supportRequestDao(): SupportRequestDao
    abstract fun technicianDao(): TechnicianDao
    abstract fun paymentDao(): PaymentDao
    abstract fun ratingDao(): RatingDao
    abstract fun evidencePhotoDao(): EvidencePhotoDao
    abstract fun technicianAssignmentDao(): TechnicianAssignmentDao

    private class SoportAppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(db: SoportAppDatabase) {
            try {
                val serviceCatalogDao = db.serviceCatalogDao()
                val services = listOf(
                    ServiceCatalog("soporte-computadores", "general", "Soporte técnico", "Reparación y mantenimiento", "Remoto o sitio", "monitor", "#2563EB", "#DBEAFE"),
                    ServiceCatalog("mantenimiento-preventivo-empresarial", "empresa", "Mantenimiento empresarial", "Revisión programada", "En sitio", "settings", "#16A34A", "#DCFCE7"),
                    ServiceCatalog("diagnostico-tecnico-empresarial", "general", "Diagnóstico técnico", "Evaluación técnica", "Remoto o sitio", "search", "#EA580C", "#FFF7ED")
                )
                serviceCatalogDao.insertAll(services)
            } catch (e: Exception) { }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SoportAppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SoportAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SoportAppDatabase::class.java,
                    "soportapp_database"
                )
                .fallbackToDestructiveMigration() // Si la versión cambia, borra y crea
                .addCallback(SoportAppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
