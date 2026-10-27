package com.mysocial.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mysocial.navigation.TypeListFollows
import com.mysocial.ui.component.UserRow
import com.mysocial.ui.theme.Dimens
import com.mysocial.ui.theme.MySocialTheme
import com.mysocial.ui.theme.PrimaryColor
import com.mysocial.ui.viewmodel.FollowsViewModel

// Tela de Seguidores/Seguindo de um usuário. Igual ao X: um menu no topo com duas
// abas (Seguidores / Seguindo), e trocar de aba só troca a lista mostrada, sem
// precisar voltar e navegar de novo. "type" define qual aba abre primeiro (a que
// a pessoa clicou lá no perfil).
@Composable
fun FollowsScreen(
    userId: Long,
    type: TypeListFollows,
    modifier: Modifier = Modifier,
    onNavigateToProfile: (Long) -> Unit = {},
    followsViewModel: FollowsViewModel = viewModel()
) {
    var abaSelecionada by remember(userId) { mutableStateOf(type) }

    // Recarrega sempre que o userId ou a aba selecionada (seguidores/seguindo) mudar.
    LaunchedEffect(userId, abaSelecionada) {
        followsViewModel.load(userId, abaSelecionada)
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Menu do topo com as duas opções, igual ao X.
        PrimaryTabRow(
            selectedTabIndex = if (abaSelecionada == TypeListFollows.FOLLOWERS) 0 else 1,
            contentColor = PrimaryColor
        ) {
            Tab(
                selected = abaSelecionada == TypeListFollows.FOLLOWERS,
                onClick = { abaSelecionada = TypeListFollows.FOLLOWERS },
                text = { Text("Seguidores") }
            )
            Tab(
                selected = abaSelecionada == TypeListFollows.FOLLOW,
                onClick = { abaSelecionada = TypeListFollows.FOLLOW },
                text = { Text("Seguindo") }
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (followsViewModel.loading) {
                item { CircularProgressIndicator(modifier = Modifier.padding(Dimens.spaceLarge)) }
            }

            if (!followsViewModel.loading && followsViewModel.users.isEmpty()) {
                item {
                    Text(
                        if (abaSelecionada == TypeListFollows.FOLLOWERS) "Ninguém segue esse usuário ainda"
                        else "Esse usuário ainda não segue ninguém",
                        modifier = Modifier.padding(Dimens.spaceLarge),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            items(followsViewModel.users, key = { it.id }) { user ->
                UserRow(
                    username = user.username,
                    coverColor = user.coverColor,
                    onClick = { onNavigateToProfile(user.id) }
                )
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
