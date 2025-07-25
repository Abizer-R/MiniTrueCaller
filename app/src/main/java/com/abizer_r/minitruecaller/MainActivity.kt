package com.abizer_r.minitruecaller

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telecom.TelecomManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.abizer_r.minitruecaller.ui.theme.MiniTrueCallerTheme
import androidx.core.net.toUri
import com.abizer_r.minitruecaller.ui.overlay.CallOverlayService
import com.abizer_r.minitruecaller.utils.CallPermissionsState
import com.abizer_r.minitruecaller.utils.Constants
import com.abizer_r.minitruecaller.utils.PermissionHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiniTrueCallerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val intent = Intent(this, CallOverlayService::class.java)
        intent.putExtra(Constants.INCOMING_CALL_EXTRA, "9999999999")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    var permissionState by remember {
        mutableStateOf(CallPermissionsState())
    }
    var launchPermissionRequest: () -> Unit by remember {
        mutableStateOf({})
    }

    PermissionHandler(
        requiredPermissions = listOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE,
        )
    ) { state, launcher ->
        permissionState = state
        launchPermissionRequest = launcher
    }

//    var callPermissionsState by remember { mutableStateOf(CallPermissionsState(false, false)) }
//    var onRequestPermissions: () -> Unit by remember { mutableStateOf({}) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            if (permissionState.overlayGranted) {
                Toast.makeText(context, R.string.overlay_permission_granted, Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    "package:${context.packageName}".toUri()
                )
                context.startActivity(intent)
            }
        }) {
            val stringRes = if (permissionState.overlayGranted) {
                R.string.overlay_permission_granted
            } else R.string.grant_overlay_permission
            Text(stringResource(stringRes))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Request runtime permissions
        Button(onClick = launchPermissionRequest) {
            Text(
                if (permissionState.grantedPermissions.all { it.value })
                    stringResource(R.string.phone_permissions_granted)
                else
                    stringResource(R.string.grant_phone_permissions)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            val currentDefault = telecomManager.defaultDialerPackage
            Log.d("DialerCheck", "Default dialer: $currentDefault")
            if (context.packageName != currentDefault) {
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                    putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
                }
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Already default dialer or unsupported", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(stringResource(R.string.make_app_default_dialer))
        }
    }
}


@Preview
@Composable
private fun PreviewPermissionNotGranted() {
    MiniTrueCallerTheme {
        MainScreen(
        )
    }
}