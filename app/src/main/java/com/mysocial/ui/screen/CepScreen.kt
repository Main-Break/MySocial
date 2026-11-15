package com.mysocial.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mysocial.ui.viewmodel.CepState
import com.mysocial.ui.viewmodel.CepViewModel

@Composable
fun CepScreen(cepViewModel: CepViewModel = viewModel()) {
    var inputCep by remember { mutableStateOf("") }
    val estadoCep by cepViewModel.estadoCep.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Consulta de CEP", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = inputCep,
            onValueChange = { inputCep = it },
            label = { Text("Digite o CEP (somente números)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { cepViewModel.consultarCep(inputCep) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Buscar Endereço")
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (val estado = estadoCep) {
            is CepState.Loading -> CircularProgressIndicator()
            is CepState.Error -> Text(text = estado.mensagem, color = MaterialTheme.colorScheme.error)
            is CepState.Success -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Logradouro: ${estado.dados.logradouro ?: "-"}")
                        Text("Bairro: ${estado.dados.bairro ?: "-"}")
                        Text("Cidade: ${estado.dados.cidade ?: "-"}")
                        Text("UF: ${estado.dados.uf ?: "-"}")
                    }
                }
            }
            is CepState.Idle -> Text("Insira o CEP para realizar a busca.")
        }
    }
}
