package com.abizer_r.minitruecaller.domain.repository

import com.abizer_r.minitruecaller.data.repository.ResultData
import com.abizer_r.minitruecaller.domain.model.CallerInfo

interface CallerRepository {
    suspend fun getCallerInfo(number: String): ResultData<CallerInfo>
    suspend fun saveCallerInfo(info: CallerInfo): ResultData<CallerInfo>
}
