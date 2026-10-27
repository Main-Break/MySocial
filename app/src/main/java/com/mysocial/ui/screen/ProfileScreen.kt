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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mysocial.R
import com.mysocial.auth.UserSession
import com.mysocial.data.PostStats
import com.mysocial.navigation.TypeListFollows
import com.mysocial.ui.component.CardPost
import com.mysocial.ui.theme.DarkBackground
import com.mysocial.ui.theme.Dimens
import com.mysocial.ui.theme.ErrorColor
import com.mysocial.ui.theme.MySocialTheme
import com.mysocial.ui.theme.PrimaryColor
import com.mysocial.ui.theme.Username
import com.mysocial.ui.viewmodel.ProfileViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Tela de perfil de um usuário: capa colorida (cor própria do usuário, ver
// User.coverColor), avatar sobreposto à capa, contadores de seguidores/seguindo
// (clicáveis, levam pra FollowsScreen), botão de seguir/deixar de seguir (some no
// próprio perfil) e a lista de posts dele, reaproveitando o mesmo CardPost do Feed.
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen(
    userId: Long,
    modifier: Modifier = Modifier,
    onNavigateToFollows: (TypeListFollows) -> Unit = {},
    onNavigateToEditPost: (Long) -> Unit = {},
    onNavigateToDetail: (Long) -> Unit = {},
    profileViewModel: ProfileViewModel = viewModel()
) {
    // Busca os dados do perfil assim que a tela abre, ou sempre que o userId mudar
    // (ex: ao navegar de um perfil pra outro clicando em outro usuário).
    LaunchedEffect(userId) {
        profileViewModel.load(userId)
    }

    var deleteId by rememberSaveable { mutableStateOf<Long?>(null) }

    val user = profileViewModel.user

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
    ) {
        item {
            if (profileViewModel.loading && user == null) {
                CircularProgressIndicator(modifier = Modifier.padding(Dimens.spaceLarge))
            }

            profileViewModel.message?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(Dimens.spaceLarge))
            }

            if (user != null) {
                val corCapa = Color(android.graphics.Color.parseColor(user.coverColor))

                // Capa colorida com o avatar grande sobreposto na parte de baixo,
                // igual ao cabeçalho de perfil do X.
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.coverHeight)
                            .background(corCapa)
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = Dimens.spaceLarge)
                            .offset(y = Dimens.coverHeight - (Dimens.avatarSizeLarge / 2))
                            .size(Dimens.avatarSizeLarge)
                            .clip(CircleShape)
                            .background(DarkBackground)
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(corCapa),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_profile_placeholder),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.size(Dimens.avatarSizeLarge - Dimens.spaceLarge)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = Dimens.spaceLarge,
                            end = Dimens.spaceLarge,
                            // Espaço reservado pro avatar, que fica sobreposto à capa.
                            top = Dimens.avatarSizeLarge / 2 + Dimens.spaceSmall
                        ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
                ) {
                    Text(user.username, style = MaterialTheme.typography.titleLarge)
                    Text("@${user.username}", color = Username, style = MaterialTheme.typography.titleMedium)

                    // Contadores clicáveis: abrem a lista de seguidores/seguindo.
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceLarge)) {
                        Row(modifier = Modifier.clickable { onNavigateToFollows(TypeListFollows.FOLLOWERS) }) {
                            Text("${profileViewModel.followersCount} ")
                            Text("Seguidores", color = Username)
                        }
                        Row(modifier = Modifier.clickable { onNavigateToFollows(TypeListFollows.FOLLOW) }) {
                            Text("${profileViewModel.followingCount} ")
                            Text("Seguindo", color = Username)
                        }
                    }

                    // Botão de seguir/deixar de seguir só aparece no perfil de outra pessoa,
                    // nunca no próprio perfil.
                    if (UserSession.userId != null && UserSession.userId != userId) {
                        if (profileViewModel.isFollowing) {
                            OutlinedButton(
                                onClick = { profileViewModel.unfollow(userId) },
                                enabled = !profileViewModel.loading
                            ) { Text("Deixar de seguir") }
                        } else {
                            Button(
                                onClick = { profileViewModel.follow(userId) },
                                enabled = !profileViewModel.loading,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                            ) { Text("Seguir") }
                        }
                    }
                }
            }
        }

        if (user != null && profileViewModel.posts.isEmpty()) {
            item {
                Text(
                    "Nenhum post ainda",
                    modifier = Modifier.padding(horizontal = Dimens.spaceLarge),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        items(profileViewModel.posts, key = { it.id }) { post ->
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))
            val date = LocalDateTime.parse(post.createdAt)
            val stats = profileViewModel.stats[post.id] ?: PostStats()

            CardPost(
                username = post.author,
                name = post.author,
                contentPost = post.content,
                datePost = date.format(formatter),
                amountComments = stats.comments,
                amountLikes = stats.likes,
                avatarColor = Color(android.graphics.Color.parseColor(post.authorCoverColor)),
                isLiked = stats.likedByMe,
                onLikeClick = { profileViewModel.toggleLike(post.id) },
                onCommentClick = { onNavigateToDetail(post.id) },
                isOwner = post.userId == UserSession.userId,
                onEditClick = { onNavigateToEditPost(post.id) },
                onDeleteClick = { deleteId = post.id },
                onClick = { onNavigateToDetail(post.id) }
            )
        }
    }

    // Confirmação de exclusão, mesmo padrão do Feed.
    val postParaExcluir = profileViewModel.posts.firstOrNull { it.id == deleteId }
    if (postParaExcluir != null) {
        AlertDialog(
            onDismissRequest = { deleteId = null },
            title = { Text("Excluir post?") },
            text = { Text("Essa ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    deleteId = null
                    profileViewModel.deletePost(postParaExcluir.id)
                }) { Text("Excluir", color = ErrorColor) }
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
fun ProfileScreenPreview() {
    MySocialTheme {
        ProfileScreen(userId = 1L)
    }
}
