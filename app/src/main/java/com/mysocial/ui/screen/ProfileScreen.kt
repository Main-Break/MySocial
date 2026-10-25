package com.mysocial.ui.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mysocial.ui.theme.MySocialTheme

// Tela de perfil de usuário onde terá a imagem (Placeholder) foto de capa (Placeholder em cor)
// Também haverá a lista de postas feitas pelo usuário.
// Mostra as informações básicas do usuário como:
// - Nome.
// - Username.
// - Botão de seguir ou deixar de seguir.
// - Seguidores e Seguindo.

class ProfileScreen {
}

@Composable
fun ProfileScreen(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

// Para vizualizar a tela
@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MySocialTheme {
        ProfileScreen("Android")
    }
}