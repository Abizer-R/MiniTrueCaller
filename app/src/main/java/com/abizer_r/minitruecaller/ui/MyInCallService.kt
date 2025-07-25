package com.abizer_r.minitruecaller.ui

import android.telecom.Call
import android.telecom.InCallService
import android.util.Log

class MyInCallService : InCallService() {
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Log.d("InCallService", "onCallAdded: ${call.details.handle}")
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        Log.d("InCallService", "onCallRemoved")
    }
}
