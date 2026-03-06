package com.example.soportapp.data.repository

import com.example.soportapp.data.database.EvidencePhoto
import com.example.soportapp.data.database.Payment
import com.example.soportapp.data.database.Rating
import com.example.soportapp.data.database.ServiceCatalog
import com.example.soportapp.data.database.SupportRequest
import com.example.soportapp.data.database.User
import com.example.soportapp.data.database.dao.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class SoportAppRepository(
    private val userDao: UserDao,
    private val serviceCatalogDao: ServiceCatalogDao,
    private val supportRequestDao: SupportRequestDao,
    private val technicianDao: TechnicianDao,
    private val paymentDao: PaymentDao,
    private val ratingDao: RatingDao,
    private val evidencePhotoDao: EvidencePhotoDao,
    private val technicianAssignmentDao: TechnicianAssignmentDao
) {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun insertUser(user: User) {
        userDao.insert(user)
        firestore.collection("usuarios").document(user.telefono).set(user).await()
    }

    suspend fun insertSupportRequest(request: SupportRequest): Long {
        val id = supportRequestDao.insert(request)
        val requestWithId = request.copy(id = id.toInt())
        syncRequestToFirebase(requestWithId)
        return id
    }

    suspend fun getUserByPhone(phone: String): User? {
        return userDao.getUserByPhone(phone)
    }

    suspend fun getSupportRequest(id: Long): SupportRequest? {
        return supportRequestDao.getRequestById(id)
    }

    suspend fun updateSupportRequest(request: SupportRequest) {
        supportRequestDao.update(request)
        syncRequestToFirebase(request)
    }

    private suspend fun syncRequestToFirebase(request: SupportRequest) {
        try {
            firestore.collection("solicitudes_soporte")
                .document(request.id.toString())
                .set(request)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun insertPayment(payment: Payment) {
        paymentDao.insert(payment)
        firestore.collection("pagos").add(payment).await()
    }

    suspend fun associateUserToRequest(supportRequestId: Long, userId: Int) {
        supportRequestDao.associateUserToRequest(supportRequestId, userId)
        firestore.collection("solicitudes_soporte")
            .document(supportRequestId.toString())
            .update("userId", userId)
            .await()
    }

    suspend fun assignTechnicianToRequest(supportRequestId: Long, technicianId: Int) {
        supportRequestDao.assignTechnicianToRequest(supportRequestId, technicianId)
        firestore.collection("solicitudes_soporte")
            .document(supportRequestId.toString())
            .update("technicianId", technicianId)
            .await()
    }

    // --- NUEVOS MÉTODOS PARA HISTORIAL Y VISTA TÉCNICA ---

    /**
     * Obtiene todas las solicitudes guardadas en la web (Firebase).
     * Ideal para que el administrador o técnicos vean todos los casos.
     */
    suspend fun getAllRequestsFromWeb(): List<SupportRequest> {
        return try {
            val snapshot = firestore.collection("solicitudes_soporte")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(SupportRequest::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene las solicitudes de un usuario específico desde Firebase.
     */
    suspend fun getUserRequestsFromWeb(userId: Int): List<SupportRequest> {
        return try {
            val snapshot = firestore.collection("solicitudes_soporte")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.toObjects(SupportRequest::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun insertEvidencePhotos(photos: List<EvidencePhoto>) {
        evidencePhotoDao.insertAll(photos)
    }

    suspend fun getServiceById(serviceId: String): ServiceCatalog? {
        return serviceCatalogDao.getServiceById(serviceId)
    }

    suspend fun insertRating(rating: Rating) {
        ratingDao.insert(rating)
        firestore.collection("calificaciones").add(rating).await()
    }
}
