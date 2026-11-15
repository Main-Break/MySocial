package com.mysocial.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mysocial.data.CepResponse
import com.mysocial.data.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CepState {
    object Idle : CepState()
    object Loading : CepState()
    data class Success(val dados: CepResponse) : CepState()
    data class Error(val mensagem: String) : CepState()
}

class CepViewModel : ViewModel() {

    private val _estadoCep = MutableStateFlow<CepState>(CepState.Idle)
    val estadoCep: StateFlow<CepState> = _estadoCep

    fun consultarCep(cepInput: String) {
        val cepLimpo = cepInput.filter { it.isDigit() }

        if (cepLimpo.length != 8) {
            _estadoCep.value = CepState.Error("Informe um CEP válido com 8 dígitos.")
            return
        }

        viewModelScope.launch {
            _estadoCep.value = CepState.Loading
            try {
                val response = RetrofitClient.viaCepApi.buscarCep(cepLimpo)
                val resultado = response.body()
                if (response.isSuccessful && resultado != null) {
                    if (resultado.erro == true) {
                        _estadoCep.value = CepState.Error("CEP não encontrado.")
                    } else {
                        _estadoCep.value = CepState.Success(resultado)
                    }
                } else {
                    _estadoCep.value = CepState.Error("Erro ao consultar a API.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estadoCep.value = CepState.Error("Falha de conexão: ${e.localizedMessage}")
            }
        }
    }

    fun consumirResultado() {
        _estadoCep.value = CepState.Idle
    }
}
