package com.mysocial.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mysocial.ui.theme.Dimens
import com.mysocial.ui.theme.MySocialTheme
import com.mysocial.ui.theme.PrimaryColor
import com.mysocial.ui.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onNavigateToLogin: () -> Unit = { },
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Quando o cadastro dá certo, manda o usuário pra tela de Login pra ele entrar com a
    // conta recém-criada (o cadastro, por si só, ainda não loga automaticamente).
    LaunchedEffect(authViewModel.done) {
        if (authViewModel.done) {
            authViewModel.consumeResult()
            onNavigateToLogin()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.spaceLarge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MySocial",
            style = MaterialTheme.typography.titleLarge
        )

        // Espaçamento entre as seções.
        Spacer(modifier = Modifier.height(Dimens.spaceLarge))

        Text(
            text = "Registre-se",
            style = MaterialTheme.typography.titleMedium
        )

        // Espaçamento entre as seções.
        Spacer(modifier = Modifier.height(Dimens.spaceLarge))

        OutlinedTextField(
            value = email,
            onValueChange = { if (it.length <= 254) email = it },
            label = {
                Text(
                    "E-mail"
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Espaçamento entre as seções.
        Spacer(modifier = Modifier.height(Dimens.spaceSmall))

        OutlinedTextField(
            value = username,
            onValueChange = { if (it.length <= 30) username = it },
            label = {
                Text(
                    "@username"
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )


        // Espaçamento entre as seções.
        Spacer(modifier = Modifier.height(Dimens.spaceSmall))

        OutlinedTextField(
            value = password,
            onValueChange = { if (it.length <= 128) password = it },
            label = {
                Text("Senha")
            },
            singleLine = true,
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else PasswordVisualTransformation(),
            // Ícone de olho clicável que alterna entre mostrar/esconder a senha digitada.
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                val description = if (passwordVisible) "Esconder senha" else "Mostrar senha"
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = description)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Mensagem de erro do AuthViewModel (ex: "usuário/e-mail já em uso").
        authViewModel.message?.let {
            Spacer(modifier = Modifier.height(Dimens.spaceSmall))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        // Espaçamento entre as seções.
        Spacer(modifier = Modifier.height(Dimens.spaceMedium))

        Button(
            onClick = {
                // Dispara o cadastro de verdade (validação + hash de senha + banco).
                authViewModel.register(username, email, password)
                password = ""
            },
            enabled = !authViewModel.loading && email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty(),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (authViewModel.loading) "Aguarde..." else "Registrar-se")
        }

        // Espaçamento entre as seções.
        Spacer(modifier = Modifier.height(Dimens.spaceLarge))

        // Seção onde o usuário não cadastrado pode criar a conta.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Possui conta?")

            TextButton(onClick = onNavigateToLogin, enabled = !authViewModel.loading) {
                Text("Ir para Login")
            }
        }
    }
}

// Para vizualizar a tela
@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    MySocialTheme {
        RegisterScreen()
    }
}
