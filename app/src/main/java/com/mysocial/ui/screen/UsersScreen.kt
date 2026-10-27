package com.mysocial.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mysocial.auth.UserSession
import com.mysocial.ui.component.UserRow
import com.mysocial.ui.theme.Dimens
import com.mysocial.ui.theme.MySocialTheme
import com.mysocial.ui.viewmodel.UsersViewModel

// Tela de busca/lista de usuários cadastrados. Dá pra encontrar qualquer pessoa pelo
// username e abrir o perfil dela, pra então seguir e acompanhar os posts (o botão de
// seguir já fica na própria tela de perfil, ver ProfileScreen).
@Composable
fun UsersScreen(
    modifier: Modifier = Modifier,
    onNavigateToProfile: (Long) -> Unit = {},
    usersViewModel: UsersViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        usersViewModel.load()
    }

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = usersViewModel.query,
            onValueChange = { usersViewModel.search(it) },
            label = { Text("Pesquisar por username") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceLarge)
        )

        if (usersViewModel.loading) {
            CircularProgressIndicator(modifier = Modifier.padding(Dimens.spaceLarge))
        }

        // Nunca mostra o próprio usuário logado na lista: não faz sentido seguir
        // a si mesmo.
        val usuarios = usersViewModel.users.filter { it.id != UserSession.userId }

        if (!usersViewModel.loading && usuarios.isEmpty()) {
            Text(
                "Nenhum usuário encontrado",
                modifier = Modifier.padding(Dimens.spaceLarge),
                style = MaterialTheme.typography.titleMedium
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(usuarios, key = { it.id }) { user ->
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
fun UsersScreenPreview() {
    MySocialTheme {
        UsersScreen()
    }
}
