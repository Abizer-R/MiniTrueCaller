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
    override fun onScreenCall(details: Call.Details) {
        val number = details.handle.schemeSpecificPart
        Log.d("MyCallScreeningService", "onScreenCall called - number = $number")
        val intent = Intent(this, CallOverlayService::class.java).apply {
            putExtra(Constants.INCOMING_CALL_EXTRA, number)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        // Must respond, even if you do nothing
        respondToCall(details, CallResponse.Builder().build())
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("MyCallScreeningService", "onBind called")
        return super.onBind(intent)
    }
}
