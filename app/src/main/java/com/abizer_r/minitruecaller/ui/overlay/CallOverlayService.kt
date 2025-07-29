package com.abizer_r.minitruecaller.ui.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
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
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.core.view.isVisible
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abizer_r.minitruecaller.R
import com.abizer_r.minitruecaller.data.DummyNumbersMapRepo
import com.abizer_r.minitruecaller.data.repository.CallerRepositoryImpl
import com.abizer_r.minitruecaller.data.repository.ResultData
import com.abizer_r.minitruecaller.domain.model.CallerInfo
import com.abizer_r.minitruecaller.domain.usecase.GetCallerInfoUseCase
import com.abizer_r.minitruecaller.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class CallOverlayService: Service() {

    @Inject lateinit var getCallerInfoUseCase: GetCallerInfoUseCase

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null

    private val callerInfoFlow = MutableStateFlow<CallerInfo?>(null)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val number = intent?.getStringExtra(Constants.INCOMING_CALL_EXTRA)
            ?: return START_NOT_STICKY
        Log.i("CallOverlayService", "onStartCommand: number = $number")
        startForeground(1, createNotification())
        showOverlay()
        fetchCallerInfo(number)
        return START_STICKY
    }

    private fun fetchCallerInfo(number: String) {
        CoroutineScope(Dispatchers.IO).launch {
            getCallerInfoUseCase.invoke(number).onEach { result ->
                withContext(Dispatchers.Main) {
                    when (result) {
                        is ResultData.Failed -> {
                            updateOverlay(CallerInfo(number, "Unknown"))
                        }
                        is ResultData.Loading -> {}
                        is ResultData.Success -> {
                            updateOverlay(result.data)
                        }
                    }
                }
            }.collect()
        }

        CoroutineScope(Dispatchers.Main).launch {
            callerInfoFlow
                .filter { it != null }
                .collect { callerInfo ->

            }
        }
    }

    private fun updateOverlay(callerInfo: CallerInfo) {
        overlayView?.apply {
            val nameTextView = findViewById<TextView>(R.id.tvName)
            nameTextView.text = callerInfo.name

            val numberTextView = findViewById<TextView>(R.id.tvNumber)
            numberTextView.text = callerInfo.number

            val progressBar = findViewById<ProgressBar>(R.id.progressBar)

            progressBar.isVisible = false
            nameTextView.isVisible = true
            numberTextView.isVisible = true
        }
    }

    private fun showOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = LayoutInflater.from(this)
        overlayView = inflater.inflate(R.layout.overlay_caller_info, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            ,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER
        params.y = 100

        val rootLayout = overlayView!!.findViewById<View>(R.id.overlay_root)

        val dismissBtn = overlayView!!.findViewById<Button>(R.id.btnDismiss)
        dismissBtn.setOnClickListener {
            rootLayout.animate().alpha(0f).setDuration(200).withEndAction {
                overlayView?.isVisible = false
                removeOverlay()
                stopForeground(true)
                stopSelf()
            }.start()
        }

        Log.d("Overlay", "Adding view to window manager")
        windowManager.addView(overlayView, params)

        // fade in animation
        overlayView?.alpha = 0f
        overlayView?.animate()?.alpha(1f)?.setDuration(300)?.start()

    }

    private fun createNotification(): Notification {
        val channelId = "caller_overlay_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Caller Overlay",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Incoming Call")
            .setContentText("Showing caller info...")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // use a valid icon
            .build()
    }

    private fun removeOverlay() {
        if (::windowManager.isInitialized && overlayView != null) {
            windowManager.removeView(overlayView)
            overlayView = null
        }
    }



    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }


    override fun onBind(p0: Intent?): IBinder? = null
}