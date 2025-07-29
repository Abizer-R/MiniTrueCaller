package com.abizer_r.minitruecaller.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abizer_r.minitruecaller.data.repository.ResultData
import com.abizer_r.minitruecaller.domain.model.CallerInfo
import com.abizer_r.minitruecaller.domain.usecase.GetCallerInfoUseCase
import com.abizer_r.minitruecaller.domain.usecase.SaveCallerInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallerViewModel @Inject constructor(
    private val getCallerInfoUseCase: GetCallerInfoUseCase,
    private val saveCallerInfoUseCase: SaveCallerInfoUseCase
) : ViewModel() {

    private val _callerInfo = MutableStateFlow<ResultData<CallerInfo>?>(null)
    val callerInfo: StateFlow<ResultData<CallerInfo>?> = _callerInfo

    fun fetchCallerInfo(number: String) {
        viewModelScope.launch {
            getCallerInfoUseCase.invoke(number).onEach { result ->
                _callerInfo.value = result
            }.collect()
        }
    }

    fun saveCallerInfo(info: CallerInfo) {
        viewModelScope.launch {
            saveCallerInfoUseCase.invoke(info).onEach { result ->
                _callerInfo.value = result
            }.collect()
        }
    }
}
