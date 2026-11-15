package com.mysocial.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
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
import com.mysocial.ui.component.PostImage
import com.mysocial.ui.theme.Dimens
import com.mysocial.ui.theme.MySocialTheme
import com.mysocial.ui.theme.PrimaryColor
import com.mysocial.ui.theme.Username
import com.mysocial.ui.viewmodel.PostViewModel
import com.mysocial.validation.PostValidator

@Composable
fun PostScreen(
    modifier: Modifier = Modifier,
    postId: Long? = null,
    voltar: () -> Unit = { },
    postViewModel: PostViewModel = viewModel()
) {
    var content by remember { mutableStateOf("") }

    val camera = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap -> postViewModel.setImageFromCamera(bitmap) }

    val galeria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> postViewModel.setImageFromGallery(uri) }

    LaunchedEffect(postId) {
        if (postId != null) postViewModel.loadForEdit(postId)
    }
    LaunchedEffect(postViewModel.editTarget) {
        postViewModel.editTarget?.let { content = it.content }
    }

    LaunchedEffect(postViewModel.published) {
        if (postViewModel.published) {
            postViewModel.consumePublished()
            voltar()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.spaceLarge)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = voltar) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
            }

            Button(
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

        Spacer(modifier = Modifier.height(Dimens.spaceMedium))

        TextField(
            value = content,
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

        Text(
            "${content.length}/${PostValidator.MAX_LENGTH}",
            color = Username,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium
        )

        if (postId == null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    try {
                        camera.launch(null)
                    } catch (_: Exception) {
                        postViewModel.reportCameraFailure()
                    }
                }) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "Tirar foto", tint = PrimaryColor)
                }
                IconButton(onClick = {
                    galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Escolher da galeria", tint = PrimaryColor)
                }
            }

            postViewModel.draftImage?.let { image ->
                Box {
                    PostImage(image)
                    IconButton(
                        onClick = { postViewModel.clearImage() },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remover imagem")
                    }
                }
            }
        }

        postViewModel.message?.let {
            Spacer(modifier = Modifier.height(Dimens.spaceSmall))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PostScreenPreview() {
    MySocialTheme {
        PostScreen()
    }
}
