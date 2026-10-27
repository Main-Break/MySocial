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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.mysocial.R
import com.mysocial.ui.theme.Dimens
import com.mysocial.ui.theme.Username

// Uma linha de usuário (avatar + nome + username), clicável, terminando numa linha
// divisória. Mesmo visual do CardPost (sem card/borda, só a linha embaixo), usado
// tanto na busca de usuários (UsersScreen) quanto na lista de seguidores/seguindo
// (FollowsScreen), pra manter os dois com a mesma cara.
@Composable
fun UserRow(
    username: String,
    coverColor: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.postHorizontalPadding, vertical = Dimens.postVerticalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
    ) {
        val corAvatar = Color(android.graphics.Color.parseColor(coverColor))
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

        Column {
            Text(username, style = MaterialTheme.typography.titleMedium)
            Text("@$username", style = MaterialTheme.typography.bodyMedium, color = Username)
        }
    }
    HorizontalDivider()
}
