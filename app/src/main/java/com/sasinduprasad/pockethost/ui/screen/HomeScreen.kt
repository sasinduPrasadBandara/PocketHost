package com.sasinduprasad.pockethost.ui.screen

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sasinduprasad.androidserver.WebServerService
import com.sasinduprasad.androidserver.WebServerService.ServerViewModelSingleton.viewModel
import com.sasinduprasad.pockethost.ProjectInfoViewModel
import com.sasinduprasad.pockethost.R
import com.sasinduprasad.pockethost.ServerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(context: Context, navigateToProjectInfo:(projectName:String)->Unit) {

    val viewModel = viewModel
    val scope = rememberCoroutineScope()
    val projectViewModel: ProjectInfoViewModel = viewModel()
    val projects = projectViewModel.projects.collectAsState()
    val bottomSheetState = rememberBottomSheetScaffoldState(
        SheetState(
            skipPartiallyExpanded = false,
            density = LocalDensity.current, initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = false
        )
    )

    Scaffold { padding ->
        BottomSheetScaffold(
            sheetShape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            sheetContainerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
                .fillMaxSize()
                .padding(padding),
            sheetPeekHeight = 0.dp,
            scaffoldState = bottomSheetState,
            sheetContent = {
                AddProjectBottomSheetContent(context, onClose = {
                    scope.launch {
                        bottomSheetState.bottomSheetState.hide()
                        withContext(Dispatchers.Main){
                            projectViewModel.loadProjects(context)
                        }
                    }
                })
            },
        ) { innerPadding ->

            val isServerRunning by viewModel.serverStatus.collectAsState()
            val serverUptime by viewModel.serverUptime.collectAsState()


            LaunchedEffect(Unit) {
                projectViewModel.loadProjects(context)
            }


            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                //header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.pockethost_logo),
                        contentDescription = "pockethost logo"
                    )
                }

                //server status
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondary)
                        .border(
                            width = 1.dp,
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        .padding(top = 8.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isServerRunning) "Server is running" else "Server is stopped",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 22.sp
                        )
                        Switch(
                            checked = isServerRunning,
                            onCheckedChange = {
                                toggleServer(
                                    context = context,
                                    viewModel = viewModel
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedBorderColor = MaterialTheme.colorScheme.onSecondary,
                                checkedThumbColor = MaterialTheme.colorScheme.surface,
                                uncheckedTrackColor = MaterialTheme.colorScheme.secondary,
                                uncheckedBorderColor = MaterialTheme.colorScheme.onSecondary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSecondary
                            ),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Port : 8080",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Uptime : $serverUptime",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 16.sp
                        )
                    }
                }


                //Projects
                Column(
                    modifier = Modifier.border(
                        width = 1.dp,
                        shape = RoundedCornerShape(topEnd = 8.dp, topStart = 8.dp),
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondary)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Projects",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 18.sp
                        )
                        Text(
                            modifier = Modifier.clickable {
                                scope.launch {
                                    bottomSheetState.bottomSheetState.expand()
                                }
                            },
                            text = "+",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 24.sp
                        )
                    }

                    LazyColumn {
                        items(projects.value) { (name, faviconFile) ->
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
                                    .padding(16.dp).clickable {
                                        navigateToProjectInfo(name)
                                    }
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
                                        if (faviconFile != null) {
                                            getBitmapFromFile(faviconFile.absolutePath)?.let {
                                                Image(
                                                    bitmap = it.asImageBitmap(),
                                                    contentDescription = name + "favicon",
                                                    modifier = Modifier.size(50.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = name,
                                                color = MaterialTheme.colorScheme.tertiary,
                                                fontSize = 20.sp
                                            )
                                            Text(
                                                text = "/" + name.lowercase(Locale.ROOT),
                                                color = MaterialTheme.colorScheme.tertiary,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            val intent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("http://192.168.8.185:8080/$name" + "/index.html")
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
                    }

                }
            }
        }

    }

}

fun getProjectFolders(context: Context): List<Pair<String, File?>> {
    val filesDir = context.filesDir
    val possibleFaviconFiles =
        listOf("favicon.png", "icon.png", "favicon.jpg", "icon.jpg", "favicon.jpeg", "favicon.webp")

    return filesDir.listFiles()?.filter { it.isDirectory }?.map { dir ->
        val faviconFile = possibleFaviconFiles.firstNotNullOfOrNull { findFile(dir, it) }
        dir.name to faviconFile
    } ?: emptyList()
}

fun findFile(dir: File, fileName: String): File? {
    return dir.walk().find { it.isFile && it.name == fileName }
}

fun getBitmapFromFile(filePath: String): Bitmap? {
    return BitmapFactory.decodeFile(filePath)
}

private fun toggleServer(context: Context, viewModel: ServerViewModel) {
    val action = if (viewModel.serverStatus.value) "STOP" else "START"
    val intent = Intent(context, WebServerService::class.java).apply {
        this.action = action
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}



