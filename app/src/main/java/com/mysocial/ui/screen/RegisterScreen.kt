package com.mysocial.ui.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mysocial.ui.theme.MySocialTheme

@Composable
fun RegisterScreen(modifier: Modifier = Modifier) {
    Text(
        text = "Cadastro",
        modifier = modifier
    )
}

// Para vizualizar a tela
@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    MySocialTheme {
        RegisterScreen()
    }
}