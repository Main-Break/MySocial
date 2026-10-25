package com.mysocial.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mysocial.ui.component.CardPost
import com.mysocial.ui.theme.Dimens
import com.mysocial.ui.theme.MySocialTheme
import com.mysocial.ui.theme.Username
import com.mysocial.ui.viewmodel.PostViewModel

// Tela onde será exibido os posts do programa

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    postViewModel: PostViewModel = viewModel()
) {
    // Busca os posts assim que a tela abre.
    LaunchedEffect(Unit) {
        postViewModel.list()
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        if (postViewModel.loading) {
            item { CircularProgressIndicator() }
        }

        // Placeholder pra quando não tem post nenhum pra mostrar (feed vazio porque o
        // usuário ainda não postou nada nem segue ninguém que postou).
        if (!postViewModel.loading && postViewModel.posts.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillParentMaxSize()
                        .padding(Dimens.spaceLarge),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Nenhum post por aqui ainda",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(Dimens.spaceSmall))

                    Text(
                        "Quando você postar algo ou seguir alguém que postou, os posts vão aparecer aqui.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Username,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        items(postViewModel.posts, key = { it.id }) { post ->
            // amountComments/amountLikes ainda ficam fixos em 0: o banco ainda não tem
            // tabela de curtidas/comentários, só de posts.
            CardPost(
                username = post.author,
                name = post.author,
                contentPost = post.content,
                datePost = post.createdAt,
                amountComments = 0,
                amountLikes = 0
            )
        }
    }
}

// Para vizualizar a tela
@Preview(showBackground = true)
@Composable
fun FeedScreenPreview() {
    MySocialTheme {
        FeedScreen()
    }
}
