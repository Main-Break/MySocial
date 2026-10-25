package com.mysocial.ui.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mysocial.ui.theme.MySocialTheme

// Tela onde será listado os seguidores e quem o usuário está seguindo.
@Composable
fun FollowsScreen(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

// Para vizualizar a tela
@Preview(showBackground = true)
@Composable
fun FollowsScreenPreview() {
    MySocialTheme {
        FollowsScreen("Android")
    }
}