package com.example.soportapp

import android.app.Application
import com.example.soportapp.data.database.SoportAppDatabase
import com.example.soportapp.data.repository.SoportAppRepository
import com.example.soportapp.data.service.AiDiagnosisService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Custom Application class to manage global state and dependency injection.
 */
class SoportApplication : Application() {

    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = CoroutineScope(SupervisorJob())

    // AppContainer instance used by the rest of classes to obtain dependencies
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this, applicationScope)
    }
}

/**
 * Interface for the dependency injection container.
 */
interface AppContainer {
    val soportAppRepository: SoportAppRepository
    val aiDiagnosisService: AiDiagnosisService
}

/**
 * Implementation of the dependency injection container.
 */
private class AppContainerImpl(
    private val application: Application,
    private val scope: CoroutineScope
) : AppContainer {

    // Lazy initialization of the repository to create it only when needed
    override val soportAppRepository: SoportAppRepository by lazy {
        val db = SoportAppDatabase.getDatabase(application, scope)
        SoportAppRepository(
            userDao = db.userDao(),
            serviceCatalogDao = db.serviceCatalogDao(),
            supportRequestDao = db.supportRequestDao(),
            technicianDao = db.technicianDao(),
            paymentDao = db.paymentDao(),
            ratingDao = db.ratingDao(),
            evidencePhotoDao = db.evidencePhotoDao(),
            technicianAssignmentDao = db.technicianAssignmentDao(),
            equipmentDao = db.equipmentDao()
        )
    }

    override val aiDiagnosisService: AiDiagnosisService by lazy {
        // Reemplazar con una API Key válida para habilitar Gemini
        AiDiagnosisService(apiKey = "YOUR_GEMINI_API_KEY")
    }
}
