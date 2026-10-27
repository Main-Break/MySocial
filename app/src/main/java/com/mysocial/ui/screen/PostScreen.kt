package com.mysocial.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mysocial.ui.theme.Dimens
import com.mysocial.ui.theme.MySocialTheme
import com.mysocial.ui.theme.PrimaryColor
import com.mysocial.ui.theme.Username
import com.mysocial.ui.viewmodel.PostViewModel
import com.mysocial.validation.PostValidator

// Tela onde será feito um novo post (postId nulo) ou editado um post existente
// (postId informado). O campo de texto é grande e sem caixa visível, igual ao
// compositor de post do X: o "X" no canto superior esquerdo volta pra tela anterior
// sem fazer nada com o que foi digitado (o rascunho só é perdido se a pessoa
// publicar ou realmente sair da tela, não só por tocar sem querer).

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
            // Empurra o conteúdo pra cima do teclado quando ele abre, em vez de deixar
            // o Compose redimensionar a tela inteira a cada tecla (isso, combinado com
            // windowSoftInputMode="adjustResize" no Manifest, é a causa mais comum de
            // acento/composição de teclado "sumir" no meio da digitação).
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.spaceLarge)
    ) {
        // Cabeçalho: "X" pra voltar sem publicar nada, e o botão de publicar/salvar
        // do outro lado, igual ao topo do compositor do X.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = voltar) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
            }

            Button(
                // Dispara a criação (ou a edição, se postId foi informado) de verdade,
                // validando o conteúdo e gravando no banco.
                onClick = {
                    if (postId == null) postViewModel.create(content)
                    else postViewModel.update(postId, content)
                },
                enabled = !postViewModel.loading && content.isNotBlank(),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
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

        // Espaçamento entre as seções.
        Spacer(modifier = Modifier.height(Dimens.spaceMedium))

        // Campo de texto grande, sem caixa visível (fundo e indicador transparentes),
        // ocupando o resto da tela, igual ao compositor do X.
        TextField(
            value = content,
            // Já trava a digitação em 280 caracteres aqui na tela (mesmo limite do
            // PostValidator), pra nem deixar passar do limite ao digitar.
            onValueChange = { if (it.length <= PostValidator.MAX_LENGTH) content = it },
            placeholder = { Text("O que está acontecendo?") },
            minLines = 6,
            textStyle = MaterialTheme.typography.titleMedium,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 220.dp)
        )

        // Contador "123/280", alinhado à direita, igual ao X.
        Text(
            "${content.length}/${PostValidator.MAX_LENGTH}",
            color = Username,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium
        )

        // Mensagem de erro do PostViewModel (ex: post vazio).
        postViewModel.message?.let {
            Spacer(modifier = Modifier.height(Dimens.spaceSmall))
            Text(it, color = MaterialTheme.colorScheme.error)
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
