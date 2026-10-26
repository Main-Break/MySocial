package com.mysocial.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mysocial.ui.theme.Dimens
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
    Card(modifier = Modifier.fillMaxWidth().padding(Dimens.spaceSmall)) {
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
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "@$username · $datePost",
                    style = MaterialTheme.typography.titleMedium,
                    color = Username
                )
            }

            Text(contentPost, style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceLarge)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Responder",
                        tint = Username
                    )
                    Text(amountComments.toString(), style = MaterialTheme.typography.titleMedium, color = Username)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall)
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Curtir",
                        tint = Username
                    )
                    Text(amountLikes.toString(), style = MaterialTheme.typography.titleMedium, color = Username)
                }
            }
        }
    }
}
