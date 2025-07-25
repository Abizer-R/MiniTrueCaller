package com.abizer_r.minitruecaller.ui.receiver

import android.Manifest
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.CallLog
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.abizer_r.minitruecaller.ui.overlay.CallOverlayService
import com.abizer_r.minitruecaller.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val state = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
        Log.d("CallReceiver", "onReceive called - state = $state, app alive = ${isAppAlive(context)}")

        if (state == TelephonyManager.EXTRA_STATE_RINGING) {
            Log.d("CallReceiver", "Call is ringing")

            val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            if (number != null) {
                Log.d("CallReceiver", "Incoming number: $number")
                startOverlayService(context, number)
            } else {
                Log.d("CallReceiver", "Went into workaround block")
                // Android 10+ workaround: query call log
                CoroutineScope(Dispatchers.IO).launch {
                    delay(2000)
                    val number = getLastIncomingNumber(context)
                    Log.d("CallReceiver", "Workaround block number: $number")
                    if (!number.isNullOrBlank()) {
                        withContext(Dispatchers.Main) {
                            startOverlayService(context, number)
                        }
                    } else {
                        Log.w("CallReceiver", "Failed to fetch incoming number from CallLog.")
                        startOverlayService(context, "00000")
                    }
                }
            }
        }
    }

    private fun startOverlayService(context: Context?, number: String) {
        val serviceIntent = Intent(context, CallOverlayService::class.java).apply {
            putExtra(Constants.INCOMING_CALL_EXTRA, number)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.startForegroundService(serviceIntent)
        } else {
            context?.startService(serviceIntent)
        }
    }

    private fun getLastIncomingNumber(context: Context?): String? {
        if (context == null)    return null
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            Log.e("CallReceiver", "READ_CALL_LOG permission not granted")
            return "11111"
        }

        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE),
            "${CallLog.Calls.TYPE} = ?",
            arrayOf(CallLog.Calls.INCOMING_TYPE.toString()),
            "${CallLog.Calls.DATE} DESC"
        )

        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
            }
        }
        return "22222"
    }

    fun isAppAlive(context: Context?): Boolean {
        val am = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return am.runningAppProcesses.any { it.processName == context.packageName }
    }
}