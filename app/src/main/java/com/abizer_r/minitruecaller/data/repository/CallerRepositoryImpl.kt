package com.abizer_r.minitruecaller.data.repository

import android.content.Context
import android.util.Log
import com.abizer_r.minitruecaller.domain.model.CallerInfo
import com.abizer_r.minitruecaller.domain.repository.CallerRepository
import com.abizer_r.minitruecaller.utils.FirebaseUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


sealed class ResultData<out T> {
    data class Loading(val nothing: Nothing? = null) : ResultData<Nothing>()
    data class Success<out T>(val data: T) : ResultData<T>()
    data class Failed(
        val exception: Throwable? = null,
        val errorCode: String? = null,
        val message: String? = null,
        val errorBody: String? = null
    ) :
        ResultData<Nothing>()

    fun errorMessage(): String? {
        return when (this) {
            is Failed -> message ?: exception?.message
            else -> null
        }
    }
}

class CallerRepositoryImpl() : CallerRepository {

    private val firestore get() = FirebaseUtils.getFirestore()

    @OptIn(InternalCoroutinesApi::class)
    override suspend fun getCallerInfo(number: String): ResultData<CallerInfo> {
        return try {
            val snapshot = firestore
                .collection("callers")
                .document(number)
                .get()
                .await()

            val info = snapshot.toObject(CallerInfo::class.java)
            info?.let { ResultData.Success(it) }
                ?: ResultData.Failed(message = "No data found")
        } catch (e: Exception) {
            ResultData.Failed(exception = e)
        }
    }


    @OptIn(InternalCoroutinesApi::class)
    override suspend fun saveCallerInfo(callerInfo: CallerInfo): ResultData<CallerInfo> {
        return try {
            firestore
                .collection("callers")
                .document(callerInfo.number) // Use number as ID for quick lookup
                .set(callerInfo)
                .await()
            ResultData.Success(callerInfo)
        } catch (e: Exception) {
            ResultData.Failed(exception = e)
        }
    }

}
