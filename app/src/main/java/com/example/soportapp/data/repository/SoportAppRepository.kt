package com.example.soportapp.data.repository

import android.util.Base64
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
import java.security.MessageDigest
import javax.crypto.Cipher
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
    
    // CIBERSEGURIDAD: Llave de ofuscación para datos PII (Nombres, Teléfonos)
    // En producción, esto se gestiona a través de un servicio de secretos.
    private val aesKey = SecretKeySpec("TuTranquiloPII-2024-Cali-Secure!".substring(0, 16).toByteArray(), "AES")

    /**
     * Ofusca datos sensibles antes de enviarlos a la nube.
     */
    private fun obfuscate(data: String): String {
        return try {
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, aesKey)
            val encrypted = cipher.doFinal(data.toByteArray())
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            data // Fallback si falla
        }
    }

    suspend fun insertUser(user: User) {
        // En local guardamos normal (ya que Room está encriptado con SQLCipher)
        userDao.insert(user)
        
        // En la nube (Firebase) ofuscamos los datos sensibles por Ley 1581
        val safeUser = user.copy(
            nombre = obfuscate(user.nombre),
            telefono = user.telefono // El teléfono suele ser el ID, se puede hashear si es necesario
        )
        firestore.collection("usuarios").document(user.telefono).set(safeUser).await()
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
            // CIBERSEGURIDAD: Protegemos la descripción del problema antes de subirla
            val safeRequest = request.copy(
                descripcionProblema = obfuscate(request.descripcionProblema)
            )
            
            firestore.collection("solicitudes_soporte")
                .document(request.id.toString())
                .set(safeRequest)
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

    suspend fun getServiceById(serviceId: String): ServiceCatalog? {
        return serviceCatalogDao.getServiceById(serviceId)
    }

    suspend fun insertRating(rating: Rating) {
        ratingDao.insert(rating)
        firestore.collection("calificaciones").add(rating).await()
    }
}
