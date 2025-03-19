package com.sasinduprasad.pockethost.ui.screen

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sasinduprasad.androidserver.WebServerService.ServerViewModelSingleton.viewModel
import com.sasinduprasad.pockethost.ProjectInfoViewModel
import com.sasinduprasad.pockethost.R
import java.io.File
import java.io.FileOutputStream
import java.util.Locale


@Composable
fun AddProjectFilesBottomSheetContent(context: Context, onClose: () -> Unit) {

    val projectViewModel: ProjectInfoViewModel = viewModel()
    val uiState by projectViewModel.uiState.collectAsState()
    val serverUiState = viewModel.uiState.collectAsState()

    val filePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                viewModel.updateProjectFile(it)
            }
        }

    val folderPickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            uri?.let { folderUri ->
                viewModel.updateProjectFolder(folderUri)
            }
        }


    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier
            .background(MaterialTheme.colorScheme.secondary)
            .padding(16.dp)
    ) {
        Text(
            text = if (uiState.subDir != null) "Add files to ${uiState.subDir?.name}" else "Add files to /${uiState.projectName}",
            color = MaterialTheme.colorScheme.tertiary,
            fontSize = 20.sp
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .border(
                    width = 1.dp,
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.onSecondary
                )
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {
                    if (uiState.isFolder) {
                        folderPickerLauncher.launch(null)
                    } else {
                        filePickerLauncher.launch("*/*")
                    }
                }
        ) {
            Image(
                painter = painterResource(R.drawable.pockethost_upload_icon),
                contentDescription = "file upload icon"
            )
            Text(
                text = serverUiState.value.file?.let { getFileName(context, it) }
                    ?: serverUiState.value.folder?.let {
                        DocumentFile.fromTreeUri(
                            context,
                            it
                        )?.name
                    }
                    ?: "Select project Folder",
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 16.sp
            )

        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Button(
            onClick = {
                onClose()
                if (uiState.isFolder) {
                    serverUiState.value.folder?.let {
                        uploadFolder(
                            context,
                            it,
                            uiState.projectName,
                            uiState.subDir?.name ?: ""
                        )
                    }
                } else {
                    serverUiState.value.file?.let {
                        uploadFile(context, it, uiState.projectName, uiState.subDir?.name ?: "")
                    }
                }

            }, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface
            ), modifier = Modifier.width(200.dp)
        ) {
            Text(
                text = "Upload",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 16.sp
            )
        }
    }
    Spacer(modifier = Modifier.height(54.dp))

}



