package com.abizer_r.minitruecaller.utils

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

object FirebaseUtils {
    fun getFirestore(context: Context): FirebaseFirestore {
//        if (FirebaseApp.getApps(context).isEmpty()) {
//            FirebaseApp.initializeApp(context)
//        }
//        return FirebaseFirestore.getInstance()
        return Firebase.firestore

    }
}
