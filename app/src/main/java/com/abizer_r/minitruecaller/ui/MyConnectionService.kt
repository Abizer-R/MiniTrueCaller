package com.abizer_r.minitruecaller.ui

import android.os.Build
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.DisconnectCause
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log

class MyConnectionService : ConnectionService() {

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection {
        Log.d("MyConnectionService", "Outgoing request for: ${request.address}")

        val connection = object : Connection() {
            init {
                setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
                setConnectionCapabilities(Connection.CAPABILITY_SUPPORT_HOLD)
                setAudioModeIsVoip(true)
                setDialing() // 👈 NEW! Show call as dialing
            }

            override fun onDisconnect() {
                setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
                destroy()
            }
        }

        connection.setInitializing()
        connection.setInitialized() // 👈 Must call to complete connection lifecycle

        return connection
    }



    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection {
        val connection = object : Connection() {
            init {
                setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
                setConnectionCapabilities(Connection.CAPABILITY_SUPPORT_HOLD)
                setAudioModeIsVoip(true)
                setActive()
            }

            override fun onDisconnect() {
                setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
                destroy()
            }
        }

        Log.d("MyConnectionService", "Created incoming connection: ${request.address}")
        return connection
    }
}
