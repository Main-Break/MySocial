package com.mysocial.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mysocial.auth.UserSession
import com.mysocial.ui.theme.Dimens
import com.mysocial.ui.theme.ErrorColor
import com.mysocial.ui.theme.MySocialTheme
import com.mysocial.ui.theme.Username
import com.mysocial.ui.viewmodel.PostViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Ao clicar em um post ele abre nessa tela, mostrando ele por completo. Se o post for
// do usuário logado, aparecem os botões de Editar/Excluir (sistema de comentários ainda
// não foi pedido, por isso não está aqui).
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DetailPostScreen(
    postId: Long,
    modifier: Modifier = Modifier,
    onNavigateToProfile: (Long) -> Unit = {},
    onNavigateToEditPost: (Long) -> Unit = {},
    voltar: () -> Unit = {},
    postViewModel: PostViewModel = viewModel()
) {
    LaunchedEffect(postId) {
        postViewModel.loadForView(postId)
    }

    // Quando a exclusão termina com sucesso, volta sozinho pro feed.
    LaunchedEffect(postViewModel.published) {
        if (postViewModel.published) {
            postViewModel.consumePublished()
            voltar()
        }
    }

    var confirmandoExclusao by remember { mutableStateOf(false) }
    val post = postViewModel.viewingPost

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.spaceLarge),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
    ) {
        if (postViewModel.loading && post == null) {
            CircularProgressIndicator()
        }

        postViewModel.message?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        if (post != null) {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
            val date = LocalDateTime.parse(post.createdAt)

            Row(
                modifier = Modifier.clickable { onNavigateToProfile(post.userId) }
            ) {
                Text(post.author, style = MaterialTheme.typography.titleMedium)
                Text(" @${post.author}", color = Username, style = MaterialTheme.typography.titleMedium)
            }

            Text(post.content, style = MaterialTheme.typography.titleMedium)

            Text(date.format(formatter), color = Username)

            if (post.userId == UserSession.userId) {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
                    TextButton(onClick = { onNavigateToEditPost(post.id) }) {
                        Text("Editar")
                    }
                    TextButton(onClick = { confirmandoExclusao = true }) {
                        Text("Excluir", color = ErrorColor)
                    }
                }
            }
        }
    }

    if (confirmandoExclusao && post != null) {
        AlertDialog(
            onDismissRequest = { confirmandoExclusao = false },
            title = { Text("Excluir post?") },
            text = { Text("Essa ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmandoExclusao = false
                    postViewModel.delete(post.id)
                }) { Text("Excluir", color = ErrorColor) }
            },
            dismissButton = {
                TextButton(onClick = { confirmandoExclusao = false }) { Text("Cancelar") }
            }
        )
    }
}

// Para vizualizar a tela
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun DetailPostScreenPreview() {
    MySocialTheme {
        DetailPostScreen(postId = 1L)
    }
}
