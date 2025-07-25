package com.abizer_r.minitruecaller.utils

import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

data class CallPermissionsState(
    val grantedPermissions: Map<String, Boolean> = emptyMap(),
    val overlayGranted: Boolean = false,
)

@Composable
fun PermissionHandler(
    requiredPermissions: List<String>,
    onPermissionResult: (CallPermissionsState, () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val overlayGranted = remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    val grantedMap = remember { mutableStateOf(mapOf<String, Boolean>()) }

    var launchPermissionsRequest: () -> Unit = {}

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        grantedMap.value = permissions
        onPermissionResult(
            CallPermissionsState(
                overlayGranted = overlayGranted.value,
                grantedPermissions = grantedMap.value
            ),
            launchPermissionsRequest
        )
    }

    launchPermissionsRequest = {
        launcher.launch(requiredPermissions.toTypedArray())
    }

    // on first launch, check current permission state
    LaunchedEffect(Unit) {
        val initialGranted = requiredPermissions.associateWith  {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        grantedMap.value = initialGranted

        onPermissionResult(
            CallPermissionsState(
                overlayGranted = overlayGranted.value,
                grantedPermissions = initialGranted
            ),
            launchPermissionsRequest
        )
    }

    // Observe overlay permission change when activity resumes
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                overlayGranted.value = Settings.canDrawOverlays(context)
                onPermissionResult(
                    CallPermissionsState(
                        overlayGranted = overlayGranted.value,
                        grantedPermissions = grantedMap.value
                    ),
                    launchPermissionsRequest
                )
            }
        })
    }
}