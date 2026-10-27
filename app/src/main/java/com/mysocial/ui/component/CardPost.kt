package com.mysocial.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.mysocial.R
import com.mysocial.ui.theme.DarkBorder
import com.mysocial.ui.theme.Dimens
import com.mysocial.ui.theme.ErrorColor
import com.mysocial.ui.theme.LikeColor
import com.mysocial.ui.theme.Username

// Uma linha de post no feed, no estilo do X: sem card/borda em volta, só uma linha
// divisória fina embaixo separando um post do outro (ver HorizontalDivider no final
// da função). O visual (avatar circular, menu de três pontinhos, curtida) segue o
// jeito do X/Twitter mobile.
@Composable
fun CardPost(
    username: String,
    name: String,
    contentPost: String,
    datePost: String,
    amountComments: Int,
    amountLikes: Int,
    // Cor de fundo do avatar do autor (mesmo "cover_color" usado no perfil dele).
    avatarColor: Color = Username,
    isLiked: Boolean = false,
    onLikeClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    // true só quando o post é do usuário logado, pra mostrar o menu de Editar/Excluir
    // (mesma regra de dono que já existe no PostDAO).
    isOwner: Boolean = false,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    // Clicar em qualquer parte do card (menos os botões) abre o post na tela de detalhe.
    onClick: () -> Unit = {}
) {
    // Controla se o menu de ações (Editar/Excluir) está aberto. Fica escondido atrás do
    // ícone de "⋮" pra não deixar as opções evidentes na tela, só quem é dono as vê.
    var menuAberto by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = Dimens.postHorizontalPadding,
                vertical = Dimens.postVerticalPadding
            ),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall)
        ) {
            // Avatar: círculo colorido (cor do perfil do autor) com o ícone padrão
            // de pessoa dentro, mesmo jeito que o professor usa Image+painterResource.
            Box(
                modifier = Modifier
                    .size(Dimens.avatarSize)
                    .background(avatarColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_profile_placeholder),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.size(Dimens.avatarSize - Dimens.spaceSmall)
                )
            }

            // Nome em destaque, username e data logo depois, mas em texto mais fino
            // (bodyMedium, sem negrito) e na cor secundária, pra não competir com o nome.
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall)
            ) {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "@$username · $datePost",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Username
                )
            }

            // Menu de três pontinhos: só aparece pra quem é dono do post, e esconde
            // Editar/Excluir atrás de um clique, em vez de deixar os botões soltos.
            if (isOwner) {
                IconButton(onClick = { menuAberto = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Mais opções",
                        tint = Username
                    )
                }
            }
        }

        // Conteudo do Post
        Text(contentPost, style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceLarge)
        ) {

            // Botão de Comentário: leva pro detalhe do post, onde dá pra comentar.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall),
                modifier = Modifier.clickable(onClick = onCommentClick)
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Comentário",
                    tint = Username
                )
                Text(amountComments.toString(), style = MaterialTheme.typography.bodyMedium, color = Username)
            }

            // Botão de Curtir: alterna entre curtido/não curtido a cada clique.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall),
                modifier = Modifier.clickable(onClick = onLikeClick)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Curtir",
                    tint = if (isLiked) LikeColor else Username
                )
                Text(
                    amountLikes.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isLiked) LikeColor else Username
                )
            }
        }
    }

    // Linha divisória fina entre um post e outro, no lugar da borda/card antigos.
    HorizontalDivider(color = DarkBorder)

    // Menu de ações do post (Editar/Excluir), aberto pelo ícone de três pontinhos.
    if (menuAberto) {
        AlertDialog(
            onDismissRequest = { menuAberto = false },
            title = { Text("Opções do post") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
                    TextButton(onClick = {
                        menuAberto = false
                        onEditClick()
                    }) { Text("Editar") }
                    TextButton(onClick = {
                        menuAberto = false
                        onDeleteClick()
                    }) { Text("Excluir", color = ErrorColor) }
                }
            },
            confirmButton = {
                TextButton(onClick = { menuAberto = false }) { Text("Fechar") }
            }
        )
    }
}
