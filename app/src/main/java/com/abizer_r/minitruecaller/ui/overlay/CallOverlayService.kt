package com.abizer_r.minitruecaller.ui.overlay

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.telecom.Call
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abizer_r.minitruecaller.domain.model.CallerInfo
import com.abizer_r.minitruecaller.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CallOverlayService: Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var composeView: ComposeView

    private val callerInfoFlow = MutableStateFlow<CallerInfo?>(null)

    override fun onCreate() {
        super.onCreate()
        composeView = ComposeView(this).apply {
            setContent {
                val callerInfo = callerInfoFlow.collectAsStateWithLifecycle()
                CallerInfoOverlay(callerInfo.value)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val number = intent?.getStringExtra(Constants.INCOMING_CALL_EXTRA)
        Log.i("CallOverlayService", "onStartCommand: number = $number")
        if (number != null) {
            fetchCallerInfo(number)
            showOverlay()
        }
        return START_STICKY
    }

    private fun fetchCallerInfo(number: String) {
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            callerInfoFlow.value = CallerInfo(
                number = number,
                name = "Dummy Name"
            )
        }
    }

    private fun showOverlay() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = 100

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(composeView, params)

        Handler(Looper.getMainLooper()).postDelayed({
            stopSelf()
        }, 5000)

    }

    override fun onDestroy() {
        super.onDestroy()
        if (::composeView.isInitialized) {
            windowManager.removeView(composeView)
        }
    }


    override fun onBind(p0: Intent?): IBinder? = null
}