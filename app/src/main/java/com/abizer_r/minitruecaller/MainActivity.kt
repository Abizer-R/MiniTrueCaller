package com.abizer_r.minitruecaller

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.abizer_r.minitruecaller.ui.theme.MiniTrueCallerTheme
import com.abizer_r.minitruecaller.utils.CallPermissionsState
import com.abizer_r.minitruecaller.utils.PermissionHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiniTrueCallerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {

    val context = LocalContext.current
    var callPermissionsState by remember {
        mutableStateOf(CallPermissionsState())
    }
    var launchRuntimePermissionsRequest: () -> Unit by remember {
        mutableStateOf({})
    }

    val callerIdRoleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (isCallerIdRoleHeld(context)) {
            Toast.makeText(context, "Caller ID role granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Caller ID role not granted.", Toast.LENGTH_SHORT).show()
        }
    }

    PermissionHandler(
        requiredPermissions = listOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE,
        )
    ) { state, launcher ->
        callPermissionsState = state
        launchRuntimePermissionsRequest = launcher
    }

    PermissionsPanel(
        modifier = Modifier.fillMaxSize(),
        permissionState = callPermissionsState,
        context = context,
        onRequestRuntimePermissions = launchRuntimePermissionsRequest,
        onRequestCallerIdRole = callerIdRoleLauncher
    )

}

@Composable
fun PermissionsPanel(
    modifier: Modifier,
    permissionState: CallPermissionsState,
    context: Context,
    onRequestRuntimePermissions: () -> Unit,
    onRequestCallerIdRole: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        OverlayPermissionSection(permissionState.overlayGranted, context)
        Spacer(Modifier.height(16.dp))

        CallPermissionsSection(
            runtimePermissionsGranted = permissionState.grantedPermissions.all { it.value },
            onClick = onRequestRuntimePermissions
        )
        Spacer(Modifier.height(16.dp))

        CallerIdRoleSection(
            isCallerIdRoleHeld = isCallerIdRoleHeld(context),
            context = context,
            onRequestCallerIdRole = onRequestCallerIdRole
        )
    }
}

@Composable
private fun CallerIdRoleSection(
    isCallerIdRoleHeld: Boolean,
    context: Context,
    onRequestCallerIdRole: ManagedActivityResultLauncher<Intent, ActivityResult>,
) {
    if (isCallerIdRoleHeld) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = null,
                tint = Color.Green
            )
            Text("Already default Caller ID app")
        }
        return
    }
    Button(
        onClick = {
            val intent = buildCallerIdRoleIntent(context)
            if (intent != null) {
                onRequestCallerIdRole.launch(intent)
            } else {
                Toast.makeText(context, "Caller ID role not available", Toast.LENGTH_SHORT)
                    .show()
            }
        },
        colors = ButtonDefaults.buttonColors().copy(
            containerColor = Color.Red
        )
    ) {
        Text("Make default Caller ID app")
    }
}

@Composable
private fun OverlayPermissionSection(
    overlayGranted: Boolean,
    context: Context
) {
    if (overlayGranted) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = null,
                tint = Color.Green
            )
            Text(stringResource(R.string.overlay_permission_granted))
        }
        return
    }
    Button(
        onClick = {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:${context.packageName}".toUri()
            )
            context.startActivity(intent)
        },
        colors = ButtonDefaults.buttonColors().copy(
            containerColor = Color.Red
        )
    ) {
        Text(stringResource(R.string.grant_overlay_permission))
    }
}

@Composable
private fun CallPermissionsSection(
    runtimePermissionsGranted: Boolean,
    onClick: () -> Unit
) {
    if (runtimePermissionsGranted) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = null,
                tint = Color.Green
            )
            Spacer(Modifier.size(16.dp))
            Text(stringResource(R.string.phone_permissions_granted))
        }
        return
    }
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors().copy(
            containerColor = Color.Red
        )
    ) {
        Text(stringResource(R.string.grant_phone_permissions))
    }
}


fun isCallerIdRoleHeld(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
        return false
    val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
    return roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
}

fun buildCallerIdRoleIntent(context: Context): Intent? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
        if (roleManager?.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) == true) {
            roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
        } else null
    } else null
}

@Preview
@Composable
private fun PreviewPermissionNotGranted() {
    MiniTrueCallerTheme {
        MainScreen(
        )
    }
}