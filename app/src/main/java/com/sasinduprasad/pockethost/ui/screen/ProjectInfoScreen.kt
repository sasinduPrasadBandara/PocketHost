package com.sasinduprasad.pockethost.ui.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.sasinduprasad.androidserver.WebServerService.ServerViewModelSingleton
import com.sasinduprasad.pockethost.ProjectInfoViewModel
import com.sasinduprasad.pockethost.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectInfoScreen(navController: NavController, onBackPress: () -> Unit) {

    val context = LocalContext.current
    val backStackEntry: NavBackStackEntry? = navController.currentBackStackEntry
    val projectName = backStackEntry?.arguments?.getString("projectName")

    val scope = rememberCoroutineScope()
    val scaffoldSheetState = rememberBottomSheetScaffoldState(
        SheetState(
            skipPartiallyExpanded = false,
            density = LocalDensity.current, initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = false
        )
    )
    val viewModel = ServerViewModelSingleton.viewModel
    val projectViewModel: ProjectInfoViewModel = viewModel()
    val uiState by projectViewModel.uiState.collectAsState()
    val subDirArry by projectViewModel.subDirArry.collectAsState()
    val selectedDir = remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val totalRequests = viewModel.totalRequests.collectAsState()

    LaunchedEffect(projectName) {
        projectViewModel.updateProjectName(projectName.toString())
        projectViewModel.updateFavIcon(projectName?.let {
            getFavIcon(
                context = context,
                projectName = it
            )
        })
    }

    LaunchedEffect(Unit) {
        projectViewModel.updateSubDirArry(context, projectName.toString(), selectedDir.value)
    }


    Scaffold { padding ->
        BottomSheetScaffold(
            containerColor = MaterialTheme.colorScheme.primary,
            sheetContainerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
                .fillMaxSize()
                .padding(padding),
            sheetPeekHeight = 0.dp,
            scaffoldState = scaffoldSheetState,
            sheetContent = {
                AddProjectFilesBottomSheetContent(context, onClose = {
                    scope.launch {
                        scaffoldSheetState.bottomSheetState.hide()
                        withContext(Dispatchers.Main) {
                            projectViewModel.updateSubDirArry(
                                context,
                                projectName.toString(),
                                selectedDir.value
                            )
                        }
                    }
                })
            },
        ) { innderpadding ->
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(innderpadding)
                    .padding(16.dp)
            ) {

                //header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onBackPress() },
                        painter = painterResource(R.drawable.pockethost_back_icon),
                        contentDescription = "back icon"
                    )
                    Text(
                        text = "Project Info",
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 20.sp
                    )
                }

                //project card
                if (projectName != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondary)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSecondary,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (uiState.favIcon != null) {
                                    getBitmapFromFile(uiState.favIcon!!.absolutePath)?.let {
                                        Image(
                                            bitmap = it.asImageBitmap(),
                                            contentDescription = uiState.projectName + "favicon",
                                            modifier = Modifier.size(50.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = uiState.projectName,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        text = "/" + uiState.projectName.lowercase(),
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("http://192.168.8.185:8080/${uiState.projectName}" + "/index.html")
                                    )
                                    context.startActivity(intent)
                                }, colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.tertiary,
                                ),
                                modifier = Modifier
                                    .border(
                                        width = 1.dp, shape = RoundedCornerShape(100),
                                        color = MaterialTheme.colorScheme.onSecondary
                                    )
                            ) {
                                Text(
                                    text = "Visit  ->",
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontSize = 16.sp
                                )
                            }

                        }
                    }
                }

                //Performance Metrics
                Column(
                    modifier = Modifier.border(
                        width = 1.dp,
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondary)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Performance Metrics",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 18.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondary)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSecondary,
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Total requests",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 18.sp
                        )
                        Text(
                            text = totalRequests.value.toString(),
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 20.sp
                        )
                    }
                }

                //file manager
                Column(
                    modifier = Modifier.border(
                        width = 1.dp,
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                ) {


                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondary)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "File Manager",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 18.sp
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedDir.value != "") {
                                Image(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { selectedDir.value = "" },
                                    painter = painterResource(R.drawable.pockethost_back_icon),
                                    contentDescription = "back icon"
                                )
                            }
                            Text(
                                text = if (selectedDir.value == "") "/$projectName" else "/${selectedDir.value}",
                                color = MaterialTheme.colorScheme.tertiary,
                                fontSize = 18.sp
                            )
                        }
                    }

                    LazyColumn {
                        if (projectName != null) {

                            if (subDirArry != null) {
                                items(subDirArry!!.toList()) { dir ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.primary)
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.onSecondary,
                                            )
                                            .padding(16.dp)
                                            .clickable {
                                                if (dir.isDirectory) {
                                                    projectViewModel.updateSubDir(dir)
                                                    selectedDir.value = dir.name
                                                }
                                            }
                                    ) {
                                        Text(
                                            text = dir.name,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.align(alignment = Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    if (showDialog) {
                        AlertDialog(
                            containerColor = MaterialTheme.colorScheme.primary,
                            onDismissRequest = { showDialog = false },
                            title = { Text("Delete Project") },
                            text = { Text("Are you sure you want to delete this project? This action cannot be undone.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    scope.launch {
                                        deleteProject(context, uiState.projectName)
                                        withContext(Dispatchers.Main) {
                                            onBackPress()
                                            showDialog = false
                                        }
                                    }
                                }) {
                                    Text("Confirm", color = MaterialTheme.colorScheme.tertiary)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDialog = false }) {
                                    Text("Cancel",color = MaterialTheme.colorScheme.tertiary)
                                }
                            }
                        )
                    }

                    Button(
                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                           showDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.tertiary
                        ), modifier = Modifier
                            .border(
                                width = 1.dp,
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                    ) {
                        Text(
                            text = "Delete Project",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 16.sp
                        )
                    }

                    Button(
                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            projectViewModel.updateIsFolder(false)
                            scope.launch {
                                scaffoldSheetState.bottomSheetState.expand()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.tertiary
                        ), modifier = Modifier
                            .border(
                                width = 1.dp,
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                    ) {
                        Text(
                            text = "Add File",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 16.sp
                        )
                    }
                    Button(
                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            projectViewModel.updateIsFolder(true)
                            scope.launch {
                                scaffoldSheetState.bottomSheetState.expand()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.tertiary
                        ), modifier = Modifier
                            .border(
                                width = 1.dp,
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                    ) {
                        Text(
                            text = "Add Folder",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 16.sp
                        )
                    }
                }

            }
        }
    }

}


fun getFavIcon(context: Context, projectName: String): File? {
    val filesDir = context.filesDir
    val projectDir = File(filesDir, projectName.lowercase())


    if (!projectDir.exists() || !projectDir.isDirectory) {
        println("Project directory not found: $projectName")
        return null
    }


    val possibleFaviconFiles = listOf(
        "favicon.png", "icon.png", "favicon.jpg", "icon.jpg", "favicon.jpeg", "favicon.webp"
    )

    for (faviconName in possibleFaviconFiles) {
        return findFile(projectDir, faviconName)
    }

    println("No favicon found in project folder")
    return null
}

fun getSubDirectories(context: Context, projectName: String, subDirName: String): Array<File>? {
    val filesDir = context.filesDir
    val projectDir = File(filesDir, projectName.lowercase())

    if (subDirName == "") {
        return projectDir.listFiles()
    } else {
        val subDir = File(projectDir, subDirName)
        return subDir.listFiles()
    }

}

private fun deleteProject(context: Context, projectName: String) {
    val projectDir = File(context.filesDir, projectName.lowercase())

    if (projectDir.exists()) {
        projectDir.deleteRecursively()
    }
}