package com.abizer_r.minitruecaller.domain.usecase

import com.abizer_r.minitruecaller.data.repository.ResultData
import com.abizer_r.minitruecaller.domain.model.CallerInfo
import com.abizer_r.minitruecaller.domain.repository.CallerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class GetCallerInfoUseCase(
    private val repository: CallerRepository
) {
    suspend operator fun invoke(number: String) = flow {
        emit(ResultData.Loading())
        val result = repository.getCallerInfo(number)
        emit(result)
    }.catch { e ->
        emit(ResultData.Failed(e))
    }.flowOn(Dispatchers.IO)
}
