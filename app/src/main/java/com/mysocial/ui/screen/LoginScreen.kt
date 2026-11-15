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
fun LoginScreen(
    modifier: Modifier = Modifier,
    onNavigateToRegister: () -> Unit = { },
    onLoginSuccess: () -> Unit = { },
    authViewModel: AuthViewModel = viewModel()
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(authViewModel.done) {
        if (authViewModel.done) {
            authViewModel.consumeResult()
            onLoginSuccess()
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

        Spacer(modifier = Modifier.height(Dimens.spaceLarge))

        Text(
            text = "Entre na sua conta",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(Dimens.spaceLarge))

        OutlinedTextField(
            value = identifier,
            onValueChange = { if (it.length <= 254) identifier = it },
            label = {
                Text("E-mail ou @username")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

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
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                val description = if (passwordVisible) "Esconder senha" else "Mostrar senha"
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = description)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        authViewModel.message?.let {
            Spacer(modifier = Modifier.height(Dimens.spaceSmall))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(Dimens.spaceMedium))

        Button(
            onClick = {
                authViewModel.login(identifier, password)
                password = ""
            },
            enabled = !authViewModel.loading && identifier.isNotEmpty() && password.isNotEmpty(),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (authViewModel.loading) "Aguarde..." else "Entrar")
        }

        Spacer(modifier = Modifier.height(Dimens.spaceLarge))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Não tem conta?")

            TextButton(onClick = onNavigateToRegister, enabled = !authViewModel.loading) {
                Text("Cadastre-se")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MySocialTheme {
        LoginScreen()
    }
}
