package com.abizer_r.minitruecaller.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

fun Context.toast(msg: String?) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Context.toast(@StringRes stringRes: Int) {
    Toast.makeText(this, getString(stringRes), Toast.LENGTH_SHORT).show()
}

@Composable
fun toast(msg: String?) {
    val context = LocalContext.current
    context.toast(msg)
}

@Composable
fun toast(@StringRes stringRes: Int) {
    val context = LocalContext.current
    context.toast(stringRes)
}

