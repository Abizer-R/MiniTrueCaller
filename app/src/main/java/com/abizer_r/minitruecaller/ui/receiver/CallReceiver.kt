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

    companion object {
        var isHandled = false
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.d("CallReceiver", "Android Q+ — skipping legacy BroadcastReceiver")
            return
        }

        val state = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
        Log.d("CallReceiver", "onReceive called - state = $state, app alive = ${isAppAlive(context)}")

        if (state == TelephonyManager.EXTRA_STATE_RINGING) {
            val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            if (!number.isNullOrBlank()) {
                if (isHandled) {
                    Log.d("CallReceiver", "Already handled, skipping incoming number")
                    return
                }
                Log.d("CallReceiver", "Incoming number: $number")
                isHandled = true
                startOverlayService(context, number)
            } else {
                // Launch workaround only if not already handled
                if (!isHandled) {
                    Log.d("CallReceiver", "Went into workaround block")
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(2000)
                        val fallbackNumber = getLastIncomingNumber(context)
                        Log.d("CallReceiver", "Workaround block number: $fallbackNumber")

                        if (!fallbackNumber.isNullOrBlank()) {
                            withContext(Dispatchers.Main) {
                                if (!isHandled) {
                                    isHandled = true
                                    startOverlayService(context, fallbackNumber)
                                } else {
                                    Log.d("CallReceiver", "Workaround ignored due to already handled")
                                }
                            }
                        }
                    }
                }
            }
        } else if (state == TelephonyManager.EXTRA_STATE_IDLE) {
            // Reset state for future calls
            isHandled = false
            Log.d("CallReceiver", "Call ended. Resetting state.")
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
            return null
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
        return null
    }

    fun isAppAlive(context: Context?): Boolean {
        val am = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return am.runningAppProcesses.any { it.processName == context.packageName }
    }
}