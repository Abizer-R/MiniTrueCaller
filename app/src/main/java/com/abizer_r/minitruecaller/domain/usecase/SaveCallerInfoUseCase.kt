package com.abizer_r.minitruecaller.domain.usecase

import com.abizer_r.minitruecaller.data.repository.ResultData
import com.abizer_r.minitruecaller.domain.model.CallerInfo
import com.abizer_r.minitruecaller.domain.repository.CallerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SaveCallerInfoUseCase(
    private val repository: CallerRepository
) {
    suspend operator fun invoke(info: CallerInfo) = flow {
        emit(ResultData.Loading())
        emit(repository.saveCallerInfo(info))
    }.catch { e ->
        emit(ResultData.Failed(e))
    }.flowOn(Dispatchers.IO)
}
