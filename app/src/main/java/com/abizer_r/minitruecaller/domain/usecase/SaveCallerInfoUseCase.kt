package com.abizer_r.minitruecaller.domain.usecase

import com.abizer_r.minitruecaller.data.repository.ResultData
import com.abizer_r.minitruecaller.domain.model.CallerInfo
import com.abizer_r.minitruecaller.domain.repository.CallerRepository

class SaveCallerInfoUseCase(
    private val repository: CallerRepository
) {
    suspend operator fun invoke(info: CallerInfo): ResultData<CallerInfo> {
        return repository.saveCallerInfo(info)
    }
}
