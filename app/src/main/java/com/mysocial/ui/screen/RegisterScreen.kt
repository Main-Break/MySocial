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
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onNavigateToLogin: () -> Unit = { }
) {
    // Pode ser qualquer identificador tanto e-mail como usuário.
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
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
                text = "Registre-se",
                style = MaterialTheme.typography.titleMedium
            )

            // Espaçamento entre as seções.
            Spacer(modifier = Modifier.height(Dimens.spaceLarge))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
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
                onValueChange = { username = it },
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
                onValueChange = { password = it },
                label = {
                    Text("Senha")
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            // Espaçamento entre as seções.
            Spacer(modifier = Modifier.height(Dimens.spaceMedium))

            Button(
                onClick = { },
                // Verifica se o campo de identifiação e senha foram digitados assim liberando o botão.
                enabled = email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty(),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar-se")
            }

            // Espaçamento entre as seções.
            Spacer(modifier = Modifier.height(Dimens.spaceLarge))

            // Seção onde o usuário não cadastrado pode criar a conta.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Possui conta?")

                TextButton(onClick = onNavigateToLogin) {
                    Text("Ir para Login")
                }
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