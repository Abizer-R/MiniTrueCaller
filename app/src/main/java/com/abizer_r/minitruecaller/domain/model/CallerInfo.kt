package com.abizer_r.minitruecaller.domain.model

import androidx.annotation.Keep

@Keep
data class CallerInfo(
    val number: String = "",
    val name: String = ""
)