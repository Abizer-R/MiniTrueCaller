package com.abizer_r.minitruecaller.data

import kotlinx.coroutines.delay

object DummyNumbersMapRepo {
    suspend fun fetchNameForNumber(number: String): String? {
        delay(2000)
        return when {
            number.contains("8871729853") -> "Fatema Rampurawala"
            number.contains("9755384596") -> "Abizer Rampurawala"
            else -> null
        }
    }
}