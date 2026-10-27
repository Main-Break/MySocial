package com.mysocial.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mysocial.auth.UserSession
import com.mysocial.ui.component.CardPost
import com.mysocial.ui.theme.Dimens
import com.mysocial.ui.theme.MySocialTheme
import com.mysocial.ui.theme.Username
import com.mysocial.ui.viewmodel.PostViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Tela onde será exibido os posts do programa

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    onNavigateToEditPost: (Long) -> Unit = {},
    postViewModel: PostViewModel = viewModel()
) {
    // Busca os posts assim que a tela abre.
    LaunchedEffect(Unit) {
        postViewModel.list()
    }

    // Guarda o id do post que o usuário pediu pra excluir, só pra mostrar o AlertDialog
    // de confirmação (mesmo padrão do UsuarioScreen do professor). Null = nenhum post
    // aguardando confirmação.
    var deleteId by rememberSaveable { mutableStateOf<Long?>(null) }

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
                        .fillMaxSize()
                        .padding(Dimens.spaceLarge),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
                ) {
                    Text("Nenhum post por aqui ainda", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Quando você postar algo ou seguir alguém que postou, os posts vão aparecer aqui.",
                        style = MaterialTheme.typography.titleMedium,
                        color = Username
                    )
                }
            }
        }

        // Fomatação de Data do Java/Kotlin

        items(postViewModel.posts, key = { it.id }) { post ->

            // Formata data para exibição no Post.
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))
            val date = LocalDateTime.parse(post.createdAt)

            // amountComments/amountLikes ainda ficam fixos em 0: o banco ainda não tem
            // tabela de curtidas/comentários, só de posts.
            CardPost(
                username = post.author,
                name = post.author,
                contentPost = post.content,
                datePost = date.format(formatter),
                amountComments = 0,
                amountLikes = 0,
                isOwner = post.userId == UserSession.userId,
                onEditClick = { onNavigateToEditPost(post.id) },
                onDeleteClick = { deleteId = post.id }
            )
        }
    }

    // Confirmação de exclusão: só aparece quando "deleteId" está preenchido.
    val postParaExcluir = postViewModel.posts.firstOrNull { it.id == deleteId }
    if (postParaExcluir != null) {
        AlertDialog(
            onDismissRequest = { deleteId = null },
            title = { Text("Excluir post?") },
            text = { Text("Essa ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteId = null
                        postViewModel.delete(postParaExcluir.id)
                    }
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { deleteId = null }) { Text("Cancelar") }
            }
        )
    }
}

// Para vizualizar a tela
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun FeedScreenPreview() {
    MySocialTheme {
        FeedScreen()
    }
}
