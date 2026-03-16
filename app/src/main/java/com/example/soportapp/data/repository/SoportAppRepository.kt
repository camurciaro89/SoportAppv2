package com.example.soportapp.data.repository

import android.util.Base64
import android.util.Log
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
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

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
    
    private val keyBytes = "TuTranquiloPII24".take(16).toByteArray()
    private val ivBytes = "CaliSecureInitV1".take(16).toByteArray()
    private val secretKey = SecretKeySpec(keyBytes, "AES")
    private val ivSpec = IvParameterSpec(ivBytes)

    private fun obfuscate(data: String?): String {
        if (data.isNullOrEmpty()) return ""
        return try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
            val encrypted = cipher.doFinal(data.toByteArray())
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            data ?: ""
        }
    }

    suspend fun insertUser(user: User) {
        userDao.insert(user)
        try {
            val safeUser = user.copy(nombre = obfuscate(user.nombre))
            firestore.collection("usuarios").document(user.telefono).set(safeUser)
        } catch (e: Exception) {
            Log.e("SoportApp", "Error sincro Firebase User: ${e.message}")
        }
    }

    suspend fun insertSupportRequest(request: SupportRequest): Long {
        // 1. SIEMPRE guardar en local primero (Esto es lo más importante)
        val id = supportRequestDao.insert(request)
        val requestWithId = request.copy(id = id.toInt())
        
        // 2. Intentar sincronizar, pero NO bloquear si falla
        syncRequestToFirebase(requestWithId)
        
        return id
    }

    suspend fun getUserByPhone(phone: String): User? = userDao.getUserByPhone(phone)
    suspend fun getSupportRequest(id: Long): SupportRequest? = supportRequestDao.getRequestById(id)
    
    suspend fun updateSupportRequest(request: SupportRequest) {
        supportRequestDao.update(request)
        syncRequestToFirebase(request)
    }

    private fun syncRequestToFirebase(request: SupportRequest) {
        try {
            val safeRequest = request.copy(
                problemDescription = obfuscate(request.problemDescription),
                serviceAddress = obfuscate(request.serviceAddress),
                confirmedAddress = obfuscate(request.confirmedAddress),
                finalDescription = obfuscate(request.finalDescription)
            )
            
            // Eliminamos el .await() para que sea una tarea en segundo plano 
            // y no bloquee el flujo del usuario si hay problemas de red
            firestore.collection("solicitudes_soporte")
                .document(request.id.toString())
                .set(safeRequest)
                .addOnFailureListener { e ->
                    Log.e("SoportApp", "Fallo Firebase: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("SoportApp", "Error pre-sincro: ${e.message}")
        }
    }

    suspend fun insertPayment(payment: Payment) {
        paymentDao.insert(payment)
        try {
            firestore.collection("pagos").add(payment)
        } catch (e: Exception) { }
    }

    suspend fun associateUserToRequest(supportRequestId: Long, userId: Int) {
        supportRequestDao.associateUserToRequest(supportRequestId, userId)
        try {
            firestore.collection("solicitudes_soporte")
                .document(supportRequestId.toString())
                .update("userId", userId)
        } catch (e: Exception) { }
    }

    suspend fun assignTechnicianToRequest(supportRequestId: Long, technicianId: Int) {
        supportRequestDao.assignTechnicianToRequest(supportRequestId, technicianId)
        try {
            firestore.collection("solicitudes_soporte")
                .document(supportRequestId.toString())
                .update("technicianId", technicianId)
        } catch (e: Exception) { }
    }

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

    suspend fun getServiceById(serviceId: String): ServiceCatalog? = serviceCatalogDao.getServiceById(serviceId)

    suspend fun insertRating(rating: Rating) {
        ratingDao.insert(rating)
        try {
            firestore.collection("calificaciones").add(rating)
        } catch (e: Exception) { }
    }
}
