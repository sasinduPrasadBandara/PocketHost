package com.sasinduprasad.pockethost

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sasinduprasad.pockethost.ui.screen.HomeScreen
import com.sasinduprasad.pockethost.ui.screen.ProjectInfoScreen
import kotlinx.serialization.Serializable

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    context: Context
) {

    NavHost(
        navController = navController,
        startDestination = HomeScreen
    ) {
        composable<HomeScreen> {
            HomeScreen(
                context,
                navigateToProjectInfo = {name->
                    navController.navigate(ProjectInfoScreen(projectName = name))
                }
            )
        }
        composable<ProjectInfoScreen> {
            ProjectInfoScreen(navController, onBackPress = {
                navController.popBackStack()
            })
        }

    }

}



@Serializable
object HomeScreen

@Serializable
data class ProjectInfoScreen(
    val projectName:String
)


