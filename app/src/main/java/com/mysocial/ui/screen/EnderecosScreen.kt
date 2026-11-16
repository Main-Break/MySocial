package com.mysocial.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mysocial.data.Endereco
import com.mysocial.ui.theme.Dimens
import com.mysocial.ui.theme.ErrorColor
import com.mysocial.ui.theme.MySocialTheme
import com.mysocial.ui.theme.PrimaryColor
import com.mysocial.ui.theme.Username
import com.mysocial.ui.viewmodel.CepState
import com.mysocial.ui.viewmodel.CepViewModel
import com.mysocial.ui.viewmodel.EnderecoViewModel

@Composable
fun EnderecosScreen(
    modifier: Modifier = Modifier,
    voltar: () -> Unit = {},
    enderecoViewModel: EnderecoViewModel = viewModel(),
    cepViewModel: CepViewModel = viewModel()
) {
    LaunchedEffect(Unit) { enderecoViewModel.load() }

    var mostrandoForm by remember { mutableStateOf(false) }
    var editandoId by remember { mutableStateOf<Long?>(null) }
    var excluindoId by remember { mutableStateOf<Long?>(null) }

    var apelido by remember { mutableStateOf("") }
    var cep by remember { mutableStateOf("") }
    var rua by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var bairro by remember { mutableStateOf("") }
    var cidade by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }

    val estadoCep by cepViewModel.estadoCep.collectAsState()

    LaunchedEffect(estadoCep) {
        val resultado = estadoCep
        if (resultado is CepState.Success) {
            rua = resultado.dados.logradouro.orEmpty().take(120)
            bairro = resultado.dados.bairro.orEmpty().take(80)
            cidade = resultado.dados.cidade.orEmpty().take(80)
            estado = resultado.dados.uf.orEmpty().take(2)
            cepViewModel.consumirResultado()
        }
    }

    LaunchedEffect(enderecoViewModel.saved) {
        if (enderecoViewModel.saved) {
            enderecoViewModel.consumeSaved()
            mostrandoForm = false
        }
    }

    fun abrirNovo() {
        editandoId = null
        apelido = ""; cep = ""; rua = ""; numero = ""; bairro = ""; cidade = ""; estado = ""
        mostrandoForm = true
    }

    fun abrirEdicao(endereco: Endereco) {
        editandoId = endereco.id
        apelido = endereco.apelido
        cep = endereco.cep
        rua = endereco.rua
        numero = endereco.numero
        bairro = endereco.bairro
        cidade = endereco.cidade
        estado = endereco.estado
        mostrandoForm = true
    }

    Column(modifier = modifier.fillMaxSize().padding(Dimens.spaceLarge)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = voltar) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
            }
            Text("Meus endereços", style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = { abrirNovo() },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) { Text("Adicionar") }
        }

        if (mostrandoForm) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.spaceSmall),
                verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
            ) {
                OutlinedTextField(
                    value = apelido,
                    onValueChange = { if (it.length <= 40) apelido = it },
                    label = { Text("Apelido (ex: Casa, Trabalho)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = cep,
                        onValueChange = { if (it.length <= 9) cep = it },
                        label = { Text("CEP") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { cepViewModel.consultarCep(cep) },
                        enabled = estadoCep !is CepState.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) { Text("Buscar") }
                }
                when (val resultado = estadoCep) {
                    is CepState.Loading -> CircularProgressIndicator()
                    is CepState.Error -> Text(resultado.mensagem, color = MaterialTheme.colorScheme.error)
                    else -> {}
                }
                OutlinedTextField(
                    value = rua,
                    onValueChange = { if (it.length <= 120) rua = it },
                    label = { Text("Rua") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
                    OutlinedTextField(
                        value = numero,
                        onValueChange = { if (it.length <= 10) numero = it },
                        label = { Text("Número") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = bairro,
                        onValueChange = { if (it.length <= 80) bairro = it },
                        label = { Text("Bairro") },
                        singleLine = true,
                        modifier = Modifier.weight(2f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
                    OutlinedTextField(
                        value = cidade,
                        onValueChange = { if (it.length <= 80) cidade = it },
                        label = { Text("Cidade") },
                        singleLine = true,
                        modifier = Modifier.weight(2f)
                    )
                    OutlinedTextField(
                        value = estado,
                        onValueChange = { if (it.length <= 2) estado = it.uppercase() },
                        label = { Text("UF") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                enderecoViewModel.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
                    Button(
                        onClick = {
                            val id = editandoId
                            if (id == null) {
                                enderecoViewModel.create(apelido, cep, rua, numero, bairro, cidade, estado)
                            } else {
                                enderecoViewModel.update(id, apelido, cep, rua, numero, bairro, cidade, estado)
                            }
                        },
                        enabled = !enderecoViewModel.loading && apelido.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) { Text(if (enderecoViewModel.loading) "Salvando..." else "Salvar") }
                    TextButton(onClick = { mostrandoForm = false }) { Text("Cancelar") }
                }
            }
            HorizontalDivider()
        }

        if (enderecoViewModel.loading && enderecoViewModel.enderecos.isEmpty() && !mostrandoForm) {
            CircularProgressIndicator()
        }

        if (!mostrandoForm && enderecoViewModel.enderecos.isEmpty()) {
            Text("Nenhum endereço salvo ainda.", color = Username, modifier = Modifier.padding(top = Dimens.spaceMedium))
        }

        LazyColumn {
            items(enderecoViewModel.enderecos, key = { it.id }) { endereco ->
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.spaceSmall),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall)
                ) {
                    Text(endereco.apelido, style = MaterialTheme.typography.titleMedium)
                    Text("${endereco.rua}, ${endereco.numero} - ${endereco.bairro}", color = Username)
                    Text("${endereco.cidade}/${endereco.estado} - CEP ${endereco.cep}", color = Username)
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
                        TextButton(onClick = { abrirEdicao(endereco) }) { Text("Editar") }
                        TextButton(onClick = { excluindoId = endereco.id }) { Text("Excluir", color = ErrorColor) }
                    }
                }
                HorizontalDivider()
            }
        }
    }

    if (excluindoId != null) {
        AlertDialog(
            onDismissRequest = { excluindoId = null },
            title = { Text("Excluir endereço?") },
            text = { Text("Essa ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    enderecoViewModel.delete(excluindoId!!)
                    excluindoId = null
                }) { Text("Excluir", color = ErrorColor) }
            },
            dismissButton = {
                TextButton(onClick = { excluindoId = null }) { Text("Cancelar") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EnderecosScreenPreview() {
    MySocialTheme {
        EnderecosScreen()
    }
}
