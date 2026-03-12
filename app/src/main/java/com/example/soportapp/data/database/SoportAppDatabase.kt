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
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

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
    version = 5,
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
            val technicianDao = db.technicianDao()
            technicianDao.insert(
                Technician(
                    id = 1,
                    nombre = "Camilo Andrés Murcia",
                    especialidad = "Microinformática",
                    photoUrl = "",
                    professionalTitle = "Especialista en Microinformática",
                    isVerified = true,
                    averageRating = 4.9f,
                    totalServices = 150
                )
            )

            val serviceCatalogDao = db.serviceCatalogDao()
            val services = listOf(
                ServiceCatalog("soporte-computadores", "general", "Soporte técnico", "Reparación y mantenimiento", "Remoto o sitio", "monitor", "#2563EB", "#DBEAFE"),
                ServiceCatalog("mantenimiento-preventivo-empresarial", "empresa", "Mantenimiento empresarial", "Revisión programada", "En sitio", "settings", "#16A34A", "#DCFCE7"),
                ServiceCatalog("diagnostico-tecnico-empresarial", "general", "Diagnóstico técnico", "Evaluación técnica", "Remoto o sitio", "search", "#EA580C", "#FFF7ED"),
                ServiceCatalog("soporte-m365", "empresa", "Soporte Microsoft 365", "Configuración nube", "Remoto", "cloud", "#2563EB", "#DBEAFE"),
                ServiceCatalog("seguridad-informatica", "empresa", "Seguridad informática", "Antivirus y protección", "Remoto", "security", "#DC2626", "#FEE2E2")
            )
            serviceCatalogDao.insertAll(services)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SoportAppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SoportAppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Ciberseguridad: Inicialización de SQLCipher
                SQLiteDatabase.loadLibs(context)
                
                // Generamos una llave de encriptación (En producción esto vendría del Android Keystore)
                val passphrase = SQLiteDatabase.getBytes("TuTranquiloSeguro2024-Cali".toCharArray())
                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SoportAppDatabase::class.java,
                    "soportapp_database"
                )
                .openHelperFactory(factory) // Activación de encriptación de disco
                .addCallback(SoportAppDatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
