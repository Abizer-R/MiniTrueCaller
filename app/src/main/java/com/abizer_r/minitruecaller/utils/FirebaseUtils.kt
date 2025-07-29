package com.abizer_r.minitruecaller.utils

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

object FirebaseUtils {
    fun getFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }
}
