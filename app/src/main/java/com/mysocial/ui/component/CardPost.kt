package com.mysocial.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.mysocial.R
import com.mysocial.ui.theme.Dimens
import com.mysocial.ui.theme.Username

@Composable
fun CardPost(
    username: String,
    name: String,
    contentPost: String,
    datePost: String,
    qtdComments: Int,
    qtdLikes: Int
) {
    Card(modifier = Modifier.fillMaxWidth()
        .padding(Dimens.spaceSmall)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Dimens.postHorizontalPadding,
                    vertical = Dimens.postVerticalPadding
                )
        ) {
            Row(verticalAlignment = Alignment.Top) {
                // Placeholder: foto padrão do Android até o cadastro de avatar ser implementado
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = "Foto de perfil de $name",
                    modifier = Modifier
                        .size(Dimens.avatarSize)
                        .clip(CircleShape)
                )

                Spacer(Modifier.width(Dimens.spaceSmall))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(name, style = MaterialTheme.typography.titleSmall)

                        Spacer(Modifier.width(Dimens.spaceExtraSmall))

                        Text(
                            "@$username · $datePost",
                            style = MaterialTheme.typography.labelSmall,
                            color = Username
                        )
                    }

                    Spacer(Modifier.height(Dimens.spaceSmall))
                    Text(
                        contentPost,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Justify
                    )

                    Spacer(Modifier.height(Dimens.spaceSmall))
                    // Placeholder: foto padrão do Android até o post ter imagem real anexada

                    Spacer(Modifier.height(Dimens.spaceSmall))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Responder",
                                tint = Username,
                                modifier = Modifier.size(Dimens.iconSize)
                            )
                            Spacer(Modifier.width(Dimens.spaceExtraSmall))
                            Text(qtdRespostas, style = MaterialTheme.typography.labelSmall, color = Username)
                        }


                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = "Curtir",
                                tint = Username,
                                modifier = Modifier.size(Dimens.iconSize)
                            )
                            Spacer(Modifier.width(Dimens.spaceExtraSmall))
                            Text(qtdCurtidas, style = MaterialTheme.typography.labelSmall, color = Username)
                        }


                    }
                }
            }
        }
    }
}