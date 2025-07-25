package com.abizer_r.minitruecaller

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
    import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import com.abizer_r.minitruecaller.ui.theme.MiniTrueCallerTheme
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.abizer_r.minitruecaller.ui.DialerActivity
import com.abizer_r.minitruecaller.ui.MyConnectionService
import com.abizer_r.minitruecaller.ui.overlay.CallOverlayService
import com.abizer_r.minitruecaller.utils.CallPermissionsState
import com.abizer_r.minitruecaller.utils.Constants
import com.abizer_r.minitruecaller.utils.PermissionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        registerMiniTrueCallerPhoneAccount(this@MainActivity)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            Log.d("DialerRole", "Dialer Role Available: ${roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)}")
            Log.d("DialerRole", "Dialer Role Held: ${roleManager.isRoleHeld(RoleManager.ROLE_DIALER)}")
        }

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

    override fun onResume() {
        super.onResume()
//        dummyIncomingCall()
    }

    private fun dummyIncomingCall() {
        lifecycleScope.launch {
            delay(3000)

            val intent = Intent(this@MainActivity, CallOverlayService::class.java)
            intent.putExtra(Constants.INCOMING_CALL_EXTRA, "9999999999")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }

    // In your MainActivity.kt
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val number = intent.data?.schemeSpecificPart
        Log.d("DialIntent", "User tried to call: $number")
        val fallbackIntent = Intent(Intent.ACTION_CALL).apply {
            data = "tel:$number".toUri()
        }
        startActivity(fallbackIntent)

    }

    fun registerMiniTrueCallerPhoneAccount(context: Context) {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

        val componentName = ComponentName(context, MyConnectionService::class.java)
        val phoneAccountHandle = PhoneAccountHandle(componentName, "mini_truecaller_id")

        val phoneAccount = PhoneAccount.builder(phoneAccountHandle, "Mini TrueCaller")
            .setCapabilities(
                PhoneAccount.CAPABILITY_CALL_PROVIDER or
                        PhoneAccount.CAPABILITY_CONNECTION_MANAGER
            )
            .setHighlightColor(context.getColor(R.color.purple_500)) // Optional
            .build()

        telecomManager.registerPhoneAccount(phoneAccount)
//        telecomManager.addNewIncomingCall(phoneAccountHandle, Bundle())

        Log.d("PhoneAccount", "Registered with: ${phoneAccountHandle.componentName.className}")
    }


}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {

    val context = LocalContext.current
    var permissionState by remember {
        mutableStateOf(CallPermissionsState())
    }
    var launchPermissionRequest: () -> Unit by remember {
        mutableStateOf({})
    }

    val requestCallerIdRole = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (isDefaultCallerIdApp(context)) {
            Toast.makeText(context, "Caller ID role granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Caller ID role not granted.", Toast.LENGTH_SHORT).show()
        }
    }


    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // After user chooses, check if we're default dialer now
        if (isDefaultDialer(context)) {
            Toast.makeText(context, "✅ App is now the default dialer!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "❌ Not made default dialer", Toast.LENGTH_SHORT).show()
        }
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

        // Default dialer
        Button(onClick = {
//            val intent = requestCallerIdRole(context)
//            if (intent != null) {
//                requestCallerIdRole.launch(intent)
//            } else {
//                Toast.makeText(context, "Caller ID role not available", Toast.LENGTH_SHORT).show()
//            }



            if (!isDefaultDialer(context)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
                    if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                        if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                            launcher.launch(intent)
                        } else {
                            Toast.makeText(context, "Already Default dialer", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Dialer role not available on this device", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                        putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
                    }
                    launcher.launch(intent)
                }
            } else {
                Toast.makeText(context, "You're already the default dialer", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(if (isDefaultDialer(context)) "Already default dialer" else "Make default dialer")
        }



        Spacer(modifier = Modifier.height(16.dp))

        // Default Caller ID
        Button(onClick = {
            val intent = requestCallerIdRole(context)
            if (intent != null) {
                requestCallerIdRole.launch(intent)
            } else {
                Toast.makeText(context, "Caller ID role not available", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(if (isDefaultCallerIdApp(context)) "Already default Caller ID app" else "Make default Caller ID app")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BATTERY OPTIMIZATION
        Button(onClick = {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            context.startActivity(intent)
        }) {
            Text("Allow battery unrestricted access")
        }

//        Spacer(modifier = Modifier.height(16.dp))
//
//        // 🔐 AUTO-START INSTRUCTIONS (for MIUI/Vivo etc.)
//        Button(onClick = {
//            showAutoStartInstructions(context)
//        }) {
//            Text("Instructions for battery restrictions")
//        }


        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            context.startActivity(Intent(TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS))
        }) {
            Text("Change Phone account")
        }


        Button(onClick = {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

            val componentName = ComponentName(context, MyConnectionService::class.java)
            val phoneAccountHandle = PhoneAccountHandle(componentName, "mini_truecaller_id")
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                telecomManager.placeCall(
                    Uri.parse("tel:1234567890"),
                    Bundle().apply {
                        putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle)
                    }
                )
            } else {
                Toast.makeText(context, "call_phone permission not granted", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Place call")
        }
    }
}


fun isDefaultCallerIdApp(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
        return false
    val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
    return roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
}

fun requestCallerIdRole(context: Context): Intent? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
        if (roleManager?.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) == true) {
            roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
        } else null
    } else null
}











fun isDefaultDialer(context: Context): Boolean {
    val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    return telecomManager.defaultDialerPackage == context.packageName
}

fun showAutoStartInstructions(context: Context) {
    AlertDialog.Builder(context)
        .setTitle("Enable Auto-start")
        .setMessage(
            "For apps like this to work reliably on Xiaomi, Vivo, Oppo, or Realme phones, please:\n\n" +
                    "1. Go to Settings > Apps > Your App\n" +
                    "2. Enable 'Auto-start' or 'Run in background'\n" +
                    "3. Disable battery optimization for this app\n\n" +
                    "This helps the app show caller info even when killed."
        )
        .setPositiveButton("Got it") { dialog, _ -> dialog.dismiss() }
        .show()
}



@Preview
@Composable
private fun PreviewPermissionNotGranted() {
    MiniTrueCallerTheme {
        MainScreen(
        )
    }
}