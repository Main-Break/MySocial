package com.mysocial.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mysocial.navigation.TypeListFollows
import com.mysocial.ui.theme.Dimens
import com.mysocial.ui.theme.MySocialTheme
import com.mysocial.ui.theme.Username
import com.mysocial.ui.viewmodel.FollowsViewModel

// Tela onde será listado os seguidores (FOLLOWERS) ou quem o usuário está seguindo
// (FOLLOW), dependendo do TypeListFollows recebido pela rota. Clicar num usuário da
// lista leva pro perfil dele.
@Composable
fun FollowsScreen(
    userId: Long,
    type: TypeListFollows,
    modifier: Modifier = Modifier,
    onNavigateToProfile: (Long) -> Unit = {},
    followsViewModel: FollowsViewModel = viewModel()
) {
    // Recarrega sempre que o userId ou o tipo (seguidores/seguindo) mudar.
    LaunchedEffect(userId, type) {
        followsViewModel.load(userId, type)
    }

    val titulo = if (type == TypeListFollows.FOLLOWERS) "Seguidores" else "Seguindo"

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(Dimens.spaceLarge),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
    ) {
        item {
            Text(titulo, style = MaterialTheme.typography.titleLarge)
        }

        if (followsViewModel.loading) {
            item { CircularProgressIndicator() }
        }

        if (!followsViewModel.loading && followsViewModel.users.isEmpty()) {
            item {
                Text(
                    if (type == TypeListFollows.FOLLOWERS) "Ninguém segue esse usuário ainda"
                    else "Esse usuário ainda não segue ninguém",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        items(followsViewModel.users, key = { it.id }) { user ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToProfile(user.id) }
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.spaceMedium),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall)
                ) {
                    Text(user.username, style = MaterialTheme.typography.titleMedium)
                    Text("@${user.username}", color = Username)
                }
            }
        }
    }
}

// Para vizualizar a tela
@Preview(showBackground = true)
@Composable
fun FollowsScreenPreview() {
    MySocialTheme {
        FollowsScreen(userId = 1L, type = TypeListFollows.FOLLOWERS)
    }
}
