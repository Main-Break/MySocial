package com.mysocial.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mysocial.auth.UserSession
import com.mysocial.ui.theme.Dimens
import com.mysocial.ui.theme.MySocialTheme
import com.mysocial.ui.theme.PrimaryColor
import com.mysocial.ui.theme.Username
import com.mysocial.ui.viewmodel.ProfileViewModel

// Tela de editar o próprio perfil: bio e endereço (CEP, rua, número, bairro, cidade,
// estado). Username/e-mail/senha não são editados aqui (ficariam numa tela separada
// de "conta", que não foi pedida ainda). O "X" no topo volta sem salvar nada.
@Composable
fun EditProfileScreen(
    modifier: Modifier = Modifier,
    voltar: () -> Unit = {},
    profileViewModel: ProfileViewModel = viewModel()
) {
    val userId = UserSession.requireUser()

    LaunchedEffect(Unit) {
        profileViewModel.load(userId)
    }

    var bio by remember { mutableStateOf("") }
    var cep by remember { mutableStateOf("") }
    var rua by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var bairro by remember { mutableStateOf("") }
    var cidade by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }

    // Assim que os dados do usuário chegam do banco, preenche os campos com o que
    // já estava salvo (se a pessoa já tinha editado o perfil antes).
    LaunchedEffect(profileViewModel.user) {
        profileViewModel.user?.let {
            bio = it.bio
            cep = it.cep
            rua = it.rua
            numero = it.numero
            bairro = it.bairro
            cidade = it.cidade
            estado = it.estado
        }
    }

    // Quando salvar com sucesso, volta sozinho pro perfil.
    LaunchedEffect(profileViewModel.saved) {
        if (profileViewModel.saved) {
            profileViewModel.consumeSaved()
            voltar()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.spaceLarge),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = voltar) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
            }
            Text("Editar perfil", style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = {
                    profileViewModel.updateProfile(bio, cep, rua, numero, bairro, cidade, estado)
                },
                enabled = !profileViewModel.loading,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) { Text(if (profileViewModel.loading) "Salvando..." else "Salvar") }
        }

        if (profileViewModel.loading && profileViewModel.user == null) {
            CircularProgressIndicator()
        }

        profileViewModel.message?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        OutlinedTextField(
            value = bio,
            onValueChange = { if (it.length <= 160) bio = it },
            label = { Text("Bio") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        Text("${bio.length}/160", color = Username, style = MaterialTheme.typography.bodyMedium)

        Text("Endereço", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = cep,
            onValueChange = { if (it.length <= 9) cep = it },
            label = { Text("CEP") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = rua,
            onValueChange = { if (it.length <= 120) rua = it },
            label = { Text("Rua") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
        ) {
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
        ) {
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
    }
}

// Para vizualizar a tela
@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    MySocialTheme {
        EditProfileScreen()
    }
}
