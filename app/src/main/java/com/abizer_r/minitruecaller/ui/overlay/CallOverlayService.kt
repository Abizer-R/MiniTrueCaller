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
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.isVisible
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abizer_r.minitruecaller.R
import com.abizer_r.minitruecaller.domain.model.CallerInfo
import com.abizer_r.minitruecaller.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CallOverlayService: Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null

    private val callerInfoFlow = MutableStateFlow<CallerInfo?>(null)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val number = intent?.getStringExtra(Constants.INCOMING_CALL_EXTRA)
            ?: return START_NOT_STICKY
        Log.i("CallOverlayService", "onStartCommand: number = $number")
        showOverlay()
        fetchCallerInfo(number)
        return START_STICKY
    }

    private fun fetchCallerInfo(number: String) {
        CoroutineScope(Dispatchers.IO).launch {
            delay(2000)
            callerInfoFlow.value = CallerInfo(
                number = number,
                name = "Dummy Name"
            )
        }

        CoroutineScope(Dispatchers.Main).launch {
            callerInfoFlow.collect { callerInfo ->

                overlayView?.apply {
                    val nameTextView = findViewById<TextView>(R.id.tvName)
                    nameTextView.text = callerInfo?.name

                    val numberTextView = findViewById<TextView>(R.id.tvNumber)
                    numberTextView.text = callerInfo?.number

                    val progressBar = findViewById<ProgressBar>(R.id.progressBar)

                    progressBar.isVisible = false
                    nameTextView.isVisible = true
                    numberTextView.isVisible = true
                }
            }
        }
    }

    private fun showOverlay() {

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_caller_info, null)





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
        windowManager.addView(overlayView, params)

        Handler(Looper.getMainLooper()).postDelayed({
            stopSelf()
        }, 5000)

    }

    override fun onDestroy() {
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
        super.onDestroy()
    }


    override fun onBind(p0: Intent?): IBinder? = null
}