package com.mysocial.ui.screen

// Tela onde será feito um novo post, podendo o mesmo ser escrito.

@Composable
fun PostScreen(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

// Para vizualizar a tela
@Preview(showBackground = true)
@Composable
fun PostScreenPreview() {
    MySocialTheme {
        PostScreen("Android")
    }
}