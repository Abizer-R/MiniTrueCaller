package com.abizer_r.minitruecaller.ui

import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.abizer_r.minitruecaller.ui.overlay.CallOverlayService
import com.abizer_r.minitruecaller.utils.Constants

class MyCallScreeningService : CallScreeningService() {

    override fun onCreate() {
        super.onCreate()
        Log.d("MyCallScreeningService", "onCreate called")
    }

    override fun onScreenCall(details: Call.Details) {
        val number = details.handle?.schemeSpecificPart
        Log.d("MyCallScreeningService", "onScreenCall called - number = $number")

        // Optionally respond to allow/block call
        val response = CallResponse.Builder()
            .setDisallowCall(false) // true = block
            .setRejectCall(false)   // true = reject
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()

        respondToCall(details, response)

        val intent = Intent(this, CallOverlayService::class.java).apply {
            putExtra(Constants.INCOMING_CALL_EXTRA, number)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("MyCallScreeningService", "onBind called")
        return super.onBind(intent)
    }
}
