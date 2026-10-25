package com.mysocial.ui.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mysocial.ui.theme.MySocialTheme

class RegisterScreen {
}

@Composable
fun RegisterScreen(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

// Para vizualizar a tela
@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    MySocialTheme {
        RegisterScreen("Android")
    }
}