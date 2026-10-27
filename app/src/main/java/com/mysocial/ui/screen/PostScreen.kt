package com.mysocial.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

// Tela onde será feito um novo post (postId nulo) ou editado um post existente
// (postId informado), podendo o mesmo ser escrito.

@Composable
fun PostScreen(
    modifier: Modifier = Modifier,
    postId: Long? = null,
    voltar: () -> Unit = { },
    postViewModel: PostViewModel = viewModel()
) {
    var content by remember { mutableStateOf("") }

    // Modo edição: assim que a tela abre, busca o post no banco (só funciona se for do
    // usuário logado) e preenche o campo de texto com o conteúdo atual dele.
    LaunchedEffect(postId) {
        if (postId != null) postViewModel.loadForEdit(postId)
    }
    LaunchedEffect(postViewModel.editTarget) {
        postViewModel.editTarget?.let { content = it.content }
    }

    // Quando o post é publicado (ou a edição é salva) com sucesso, volta pro feed sozinho.
    LaunchedEffect(postViewModel.published) {
        if (postViewModel.published) {
            postViewModel.consumePublished()
            voltar()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.spaceLarge),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (postId == null) "Novo post" else "Editar post",
            style = MaterialTheme.typography.titleMedium
        )

        // Espaçamento entre as seções.
        Spacer(modifier = Modifier.height(Dimens.spaceLarge))

        OutlinedTextField(
            value = content,
            // Já trava a digitação em 280 caracteres aqui na tela (mesmo limite do
            // PostValidator), pra nem deixar passar do limite ao digitar.
            onValueChange = { if (it.length <= PostValidator.MAX_LENGTH) content = it },
            label = { Text("O que está acontecendo?") },
            modifier = Modifier.fillMaxWidth()
        )

        // Espaçamento entre as seções.
        Spacer(modifier = Modifier.height(Dimens.spaceExtraSmall))

        // Contador "123/280".
        Text(
            "${content.length}/${PostValidator.MAX_LENGTH}",
            style = MaterialTheme.typography.titleMedium
        )

        // Mensagem de erro do PostViewModel (ex: post vazio).
        postViewModel.message?.let {
            Spacer(modifier = Modifier.height(Dimens.spaceSmall))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        // Espaçamento entre as seções.
        Spacer(modifier = Modifier.height(Dimens.spaceMedium))

        Button(
            // Dispara a criação (ou a edição, se postId foi informado) de verdade,
            // validando o conteúdo e gravando no banco.
            onClick = {
                if (postId == null) postViewModel.create(content)
                else postViewModel.update(postId, content)
            },
            enabled = !postViewModel.loading && content.isNotBlank(),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            val textoBotao = when {
                postViewModel.loading && postId == null -> "Publicando..."
                postViewModel.loading -> "Salvando..."
                postId == null -> "Postar"
                else -> "Salvar"
            }
            Text(textoBotao)
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
