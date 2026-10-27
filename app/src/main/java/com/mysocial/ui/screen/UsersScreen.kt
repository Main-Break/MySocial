package com.mysocial.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mysocial.R
import com.mysocial.auth.UserSession
import com.mysocial.ui.theme.Dimens
import com.mysocial.ui.theme.MySocialTheme
import com.mysocial.ui.theme.Username
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToProfile(user.id) }
                        .padding(Dimens.spaceLarge),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
                ) {
                    val corAvatar = Color(android.graphics.Color.parseColor(user.coverColor))
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
                        Text(user.username, style = MaterialTheme.typography.titleMedium)
                        Text("@${user.username}", color = Username)
                    }
                }
                HorizontalDivider()
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
