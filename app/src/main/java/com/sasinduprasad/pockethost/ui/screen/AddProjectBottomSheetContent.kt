package com.sasinduprasad.pockethost.ui.screen

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.sasinduprasad.androidserver.WebServerService.ServerViewModelSingleton
import com.sasinduprasad.androidserver.WebServerService.ServerViewModelSingleton.viewModel
import com.sasinduprasad.pockethost.R
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@Composable
fun AddProjectBottomSheetContent(context: Context, onClose: () -> Unit) {

    val viewModel = viewModel
    val uiState by viewModel.uiState.collectAsState()

    var projectName by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(uiState.projectName))
    }


    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier
            .padding(16.dp)
    ) {
        Text(
            text = "Create New Project",
            color = MaterialTheme.colorScheme.tertiary,
            fontSize = 20.sp
        )
        TextField(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.onSecondary
                )
                .fillMaxWidth(),
            value = projectName,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.primary,
                unfocusedTextColor = MaterialTheme.colorScheme.tertiary,
                focusedContainerColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.tertiary,
                ),
            onValueChange = {
                projectName = it
                viewModel.updateProjectName(it.text)
            },
            placeholder = {
                Text("Enter project name")
            })
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.End) {
        Button(
            onClick = {
                if (projectName.text.isNotEmpty()) {
                    createFolder(context, projectName = projectName.text)
                    onClose()
                    projectName =TextFieldValue("")
                }else{
                    Toast.makeText(context,"Please enter name for your project",Toast.LENGTH_LONG).show()
                }
            }, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface
            ), modifier = Modifier.width(200.dp)
        ) {
            Text(
                text = "Save Project",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 16.sp
            )
        }
    }
    Spacer(modifier = Modifier.height(54.dp))

}


fun createFolder(context: Context, projectName: String) {
    val projectDir = File(context.filesDir, projectName.lowercase())
    if (!projectDir.exists()) {
        projectDir.mkdirs()
    }
}

fun uploadFile(context: Context, fileUri: Uri, projectName: String, subDir: String) {
    val fileName = getFileName(context, fileUri) ?: "uploaded.html"

    val projectDir = File(context.filesDir, projectName.lowercase())
    if (!projectDir.exists()) {
        projectDir.mkdirs()
    }

    val destinationDir = if (subDir.isEmpty()) {
        projectDir
    } else {
        val subDirectory = File(projectDir, subDir)
        if (!subDirectory.exists()) {
            subDirectory.mkdirs()
        }
        subDirectory
    }

    val destFile = File(destinationDir, fileName)

    context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
        FileOutputStream(destFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }
}

fun uploadFolder(context: Context, folderUri: Uri, projectName: String, subDir: String) {
    val projectDir = File(context.filesDir, projectName.lowercase())
    if (!projectDir.exists()) {
        projectDir.mkdirs()
    }

    val destinationDir = if (subDir.isEmpty()) {
        projectDir
    } else {
        val subDirectory = File(projectDir, subDir)
        if (!subDirectory.exists()) {
            subDirectory.mkdirs()
        }
        subDirectory
    }


    val folderDocument = DocumentFile.fromTreeUri(context, folderUri)
    if (folderDocument != null && folderDocument.isDirectory) {
        val folderName = folderDocument.name ?: "uploaded_folder"
        val newFolder = File(destinationDir, folderName)
        if (!newFolder.exists()) {
            newFolder.mkdirs()
        }

        for (file in folderDocument.listFiles()) {
            if (file.isFile) {
                val fileName = file.name ?: "unknown_file"
                val destFile = File(newFolder, fileName)

                context.contentResolver.openInputStream(file.uri)?.use { inputStream ->
                    FileOutputStream(destFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }
    }
}

fun getFileName(context: Context, uri: Uri): String? {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val columnIndex = cursor.getColumnIndex("_display_name")
        return if (columnIndex != -1 && cursor.moveToFirst()) {
            cursor.getString(columnIndex)
        } else null
    }
    return null
}