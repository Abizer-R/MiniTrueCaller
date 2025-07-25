package com.abizer_r.minitruecaller.ui.overlay

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.abizer_r.minitruecaller.utils.Constants

class CallOverlayService: Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val number = intent?.getStringExtra(Constants.INCOMING_CALL_EXTRA)
            ?: return START_NOT_STICKY


        Log.i("CallOverlayService", "onStartCommand: number = $number")
        return START_STICKY
    }


    override fun onBind(p0: Intent?): IBinder? = null
}