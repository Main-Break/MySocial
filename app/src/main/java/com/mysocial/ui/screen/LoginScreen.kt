package com.mysocial.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.mysocial.ui.theme.Dimens
import com.mysocial.ui.theme.MySocialTheme
import com.mysocial.ui.theme.PrimaryColor

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onNavigateToRegister: () -> Unit = { }
) {

    // Pode ser qualquer identificador tanto e-mail como usuário.
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                text = "Entre na sua conta",
                style = MaterialTheme.typography.titleMedium
            )

            // Espaçamento entre as seções.
            Spacer(modifier = Modifier.height(Dimens.spaceLarge))

            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it },
                label = {
                    Text(
                        "Telefone, e-mail ou usuário"
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Espaçamento entre as seções.
            Spacer(modifier = Modifier.height(Dimens.spaceSmall))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = {
                    Text("Senha")
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(mask = '*'),
                modifier = Modifier.fillMaxWidth()
            )

            // Espaçamento entre as seções.
            Spacer(modifier = Modifier.height(Dimens.spaceMedium))

            Button(
                onClick = { },
                // Verifica se o campo de identifiação e senha foram digitados assim liberando o botão.
                enabled = identifier.isNotEmpty() && password.isNotEmpty(),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entrar")
            }

            // Espaçamento entre as seções.
            Spacer(modifier = Modifier.height(Dimens.spaceLarge))

            // Seção onde o usuário não cadastrado pode criar a conta.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Não tem conta?")

                TextButton(onClick = onNavigateToRegister) {
                    Text("Cadastre-se")
                }
            }
        }
    }
}

// Para vizualizar a tela
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MySocialTheme {
        LoginScreen()
    }
}
