package com.mysocial.ui.screen

// Tela onde será exibido os posts do programa

@Composable
fun FeedScreen(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

// Para vizualizar a tela
@Preview(showBackground = true)
@Composable
fun FeedScreenPreview() {
    MySocialTheme {
        FeedScreen("Android")
    }
}