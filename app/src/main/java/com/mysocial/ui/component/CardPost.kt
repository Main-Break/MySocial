package com.mysocial.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.mysocial.ui.theme.DarkBackground
import com.mysocial.ui.theme.Dimens
import com.mysocial.ui.theme.OnPrimaryColor
import com.mysocial.ui.theme.Username

// Card de um post no feed. Mesmo padrão de lista do professor (UsuarioScreen): um Card
// por item, com Column(verticalArrangement = spacedBy(...)) por dentro.
@Composable
fun CardPost(
    username: String,
    name: String,
    contentPost: String,
    datePost: String,
    amountComments: Int,
    amountLikes: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .padding(Dimens.spaceSmall),
        colors = CardDefaults.cardColors(containerColor = DarkBackground),

        // Define o estilo da borda.
        border = BorderStroke(0.5.dp, OnPrimaryColor),

        // Define o arredondamento da borda.
        shape = RoundedCornerShape(10.dp)

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Dimens.postHorizontalPadding,
                    vertical = Dimens.postVerticalPadding
                ),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall)
            ) {
                // Nome do usuário
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "@$username · $datePost",
                    style = MaterialTheme.typography.titleMedium,
                    color = Username
                )
            }

            // Conteudo do Post
            Text(contentPost, style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceLarge)
            ) {

                // Botão de Comentário
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall)
                ) {
                    // Icone (Não Mexer)
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comentário",
                        tint = Username
                    )
                    // Quantidade de Comentários
                    Text(amountComments.toString(), style = MaterialTheme.typography.titleMedium, color = Username)
                }

                // Botão de Curtir
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall)
                ) {
                    // Icone (Não Mexer)
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Curtir",
                        tint = Username
                    )
                    // Quantidade de Likes no post
                    Text(amountLikes.toString(), style = MaterialTheme.typography.titleMedium, color = Username)
                }
            }
        }
    }
}
