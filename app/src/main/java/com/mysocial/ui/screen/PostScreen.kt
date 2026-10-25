package com.mysocial.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.mysocial.ui.theme.Dimens
import com.mysocial.ui.theme.MySocialTheme
import com.mysocial.ui.theme.PrimaryColor
import com.mysocial.ui.viewmodel.PostViewModel
import com.mysocial.validation.PostValidator

// Tela onde será feito um novo post, podendo o mesmo ser escrito.

@Composable
fun PostScreen(
    modifier: Modifier = Modifier,
    voltar: () -> Unit = { },
    postViewModel: PostViewModel = viewModel()
) {
    var conteudo by remember { mutableStateOf("") }

    // Quando o post é publicado com sucesso, volta pro feed sozinho.
    LaunchedEffect(postViewModel.publicado) {
        if (postViewModel.publicado) {
            postViewModel.consumirPublicado()
            voltar()
        }
    }

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.spaceLarge),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Novo post",
                style = MaterialTheme.typography.titleMedium
            )

            // Espaçamento entre as seções.
            Spacer(modifier = Modifier.height(Dimens.spaceLarge))

            OutlinedTextField(
                value = conteudo,
                // Já trava a digitação em 280 caracteres aqui na tela (mesmo limite do
                // PostValidator), pra nem deixar passar do limite ao digitar.
                onValueChange = { if (it.length <= PostValidator.TAMANHO_MAXIMO) conteudo = it },
                label = { Text("O que está acontecendo?") },
                modifier = Modifier.fillMaxWidth()
            )

            // Espaçamento entre as seções.
            Spacer(modifier = Modifier.height(Dimens.spaceExtraSmall))

            // Contador "123/280" alinhado à direita, igual ao Twitter/X.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    "${conteudo.length}/${PostValidator.TAMANHO_MAXIMO}",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // Mensagem de erro do PostViewModel (ex: post vazio).
            postViewModel.mensagem?.let {
                Spacer(modifier = Modifier.height(Dimens.spaceSmall))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            // Espaçamento entre as seções.
            Spacer(modifier = Modifier.height(Dimens.spaceMedium))

            Button(
                // Dispara a criação do post de verdade (valida + grava no banco).
                onClick = { postViewModel.criar(conteudo) },
                enabled = !postViewModel.carregando && conteudo.isNotBlank(),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (postViewModel.carregando) "Publicando..." else "Postar")
            }
        }
    }
}

// Para vizualizar a tela
@Preview(showBackground = true)
@Composable
fun PostScreenPreview() {
    MySocialTheme {
        PostScreen()
    }
}
