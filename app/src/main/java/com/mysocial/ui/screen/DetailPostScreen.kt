package com.mysocial.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview
import com.mysocial.R
import com.mysocial.auth.UserSession
import com.mysocial.data.PostStats
import com.mysocial.ui.theme.Dimens
import com.mysocial.ui.theme.ErrorColor
import com.mysocial.ui.theme.LikeColor
import com.mysocial.ui.theme.MySocialTheme
import com.mysocial.ui.theme.PrimaryColor
import com.mysocial.ui.theme.Username
import com.mysocial.ui.viewmodel.CommentViewModel
import com.mysocial.ui.viewmodel.PostViewModel
import com.mysocial.validation.CommentValidator
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DetailPostScreen(
    postId: Long,
    modifier: Modifier = Modifier,
    onNavigateToProfile: (Long) -> Unit = {},
    onNavigateToEditPost: (Long) -> Unit = {},
    voltar: () -> Unit = {},
    postViewModel: PostViewModel = viewModel(),
    commentViewModel: CommentViewModel = viewModel()
) {
    LaunchedEffect(postId) {
        postViewModel.loadForView(postId)
        commentViewModel.load(postId)
    }

    LaunchedEffect(postViewModel.published) {
        if (postViewModel.published) {
            postViewModel.consumePublished()
            voltar()
        }
    }

    var confirmandoExclusao by remember { mutableStateOf(false) }
    var menuAberto by remember { mutableStateOf(false) }
    var novoComentario by remember { mutableStateOf("") }
    val post = postViewModel.viewingPost
    val stats = postViewModel.stats[postId] ?: PostStats()

    LazyColumn(modifier = modifier.fillMaxSize().imePadding()) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                    val corAvatar = Color(android.graphics.Color.parseColor(post.authorCoverColor))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(Dimens.avatarSize)
                                .background(corAvatar, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_profile_placeholder),
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.size(Dimens.avatarSize - Dimens.spaceSmall)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToProfile(post.userId) }
                        ) {
                            Text(post.author, style = MaterialTheme.typography.titleMedium)
                            Text("@${post.author}", color = Username)
                        }

                        if (post.userId == UserSession.userId) {
                            IconButton(onClick = { menuAberto = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Mais opções",
                                    tint = Username
                                )
                            }
                        }
                    }

                    Text(post.content, style = MaterialTheme.typography.titleMedium)
                    Text(date.format(formatter), color = Username)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceLarge),
                        modifier = Modifier.padding(top = Dimens.spaceSmall)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Comentário",
                                tint = Username
                            )
                            Text(commentViewModel.comments.size.toString(), color = Username)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall),
                            modifier = Modifier.clickable { postViewModel.toggleLike(postId) }
                        ) {
                            Icon(
                                imageVector = if (stats.likedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Curtir",
                                tint = if (stats.likedByMe) LikeColor else Username
                            )
                            Text(stats.likes.toString(), color = if (stats.likedByMe) LikeColor else Username)
                        }
                    }
                }
            }
            HorizontalDivider()
        }

        if (post != null) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spaceLarge),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
                ) {
                    TextField(
                        value = novoComentario,
                        onValueChange = { if (it.length <= CommentValidator.MAX_LENGTH) novoComentario = it },
                        placeholder = { Text("Comente algo") },
                        minLines = 3,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    commentViewModel.message?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = {
                            commentViewModel.create(postId, novoComentario)
                            novoComentario = ""
                        },
                        enabled = !commentViewModel.loading && novoComentario.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) { Text("Comentar") }
                }
                HorizontalDivider()
            }

            items(commentViewModel.comments, key = { it.id }) { comment ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spaceLarge),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall)) {
                        Text(comment.author, style = MaterialTheme.typography.titleMedium)
                        if (comment.userId == UserSession.userId) {
                            TextButton(onClick = { commentViewModel.delete(comment.id) }) {
                                Text("Excluir", color = ErrorColor)
                            }
                        }
                    }
                    Text(comment.content)
                }
            }
        }
    }

    if (menuAberto) {
        AlertDialog(
            onDismissRequest = { menuAberto = false },
            title = { Text("Opções do post") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
                    TextButton(onClick = {
                        menuAberto = false
                        onNavigateToEditPost(postId)
                    }) { Text("Editar") }
                    TextButton(onClick = {
                        menuAberto = false
                        confirmandoExclusao = true
                    }) { Text("Excluir", color = ErrorColor) }
                }
            },
            confirmButton = {
                TextButton(onClick = { menuAberto = false }) { Text("Fechar") }
            }
        )
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

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun DetailPostScreenPreview() {
    MySocialTheme {
        DetailPostScreen(postId = 1L)
    }
}
