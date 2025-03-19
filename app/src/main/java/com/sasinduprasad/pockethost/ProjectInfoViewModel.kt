package com.sasinduprasad.pockethost

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.sasinduprasad.pockethost.ui.screen.getProjectFolders
import com.sasinduprasad.pockethost.ui.screen.getSubDirectories
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class ProjectInfoViewModel:ViewModel() {

    private val _uiState = MutableStateFlow(ProjectInfoUiState())
    val uiState: StateFlow<ProjectInfoUiState> = _uiState.asStateFlow()

    private val _projects = MutableStateFlow<List<Pair<String, File?>>>(emptyList())
    val projects: StateFlow<List<Pair<String, File?>>> = _projects.asStateFlow()

    private val _subDirArry = MutableStateFlow<Array<File>?>(null)
    val subDirArry: StateFlow<Array<File>?> = _subDirArry.asStateFlow()

    fun updateSubDirArry(context: Context, projectName: String, selectedDir: String) {
        _subDirArry.value = getSubDirectories(context, projectName, selectedDir)
    }


    fun loadProjects(context: Context){
        _projects.value = getProjectFolders(context)
    }

    fun updateProjectName(name: String) {
        _uiState.value = _uiState.value.copy(projectName = name)
    }

    fun updateFavIcon(favIcon: File?) {
        _uiState.value = _uiState.value.copy(favIcon = favIcon)
    }

    fun updateSubDir(subDir: File?) {
        _uiState.value = _uiState.value.copy(subDir = subDir)
    }

    fun updateIsFolder(isFolder:Boolean) {
        _uiState.value = _uiState.value.copy(isFolder = isFolder)
    }

}

data class ProjectInfoUiState(
    val projectName: String = "",
    val favIcon : File? = null,
    val subDir :File? = null,
    val isFolder :Boolean = false,
    val subDirArray: Array<File>? = null
)