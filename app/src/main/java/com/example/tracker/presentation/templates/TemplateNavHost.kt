package com.example.tracker.presentation.templates

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.tracker.domain.model.DayTemplate
import com.example.tracker.domain.model.WeekTemplate

@Composable
fun TemplateNavHost(
    userId: String
) {
    val navController = rememberNavController()
    val viewModel: TemplateViewModel = viewModel(factory = TemplateViewModelFactory(userId))

    NavHost(navController = navController, startDestination = "template_list") {
        composable("template_list") {
            TemplateScreen(
                userId = userId,
                viewModel = viewModel,
                onAddTemplate = { navController.navigate("add_template") },
                onEditTemplate = { templateId ->
                    navController.navigate("edit_template/$templateId")
                }
            )
        }

        composable("add_template") {
            TemplateInputScreen(
                userId = userId,
                viewModelKey = "add_template",
                onSaveDayTemplate = {
                    viewModel.updateTemplate(it) // Adding new day template
                    navController.popBackStack()
                },
                onSaveWeekTemplate = {
                    viewModel.updateTemplate(it) // Adding new week template
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "edit_template/{templateId}",
            arguments = listOf(navArgument("templateId") { type = NavType.StringType })
        ) { backStackEntry ->

            val templateId = backStackEntry.arguments?.getString("templateId") ?: return@composable

            // Trigger loading of the template when the screen enters
            LaunchedEffect(templateId) {
                viewModel.loadTemplateById(templateId)
            }

            // Observe the selected template from viewmodel
            val selectedTemplate by viewModel.selectedTemplate.collectAsState()

            TemplateInputScreen(
                userId = userId,
                viewModelKey = templateId,
                templateId = templateId,
                onSaveDayTemplate = {
                    viewModel.updateTemplate(it)
                    navController.popBackStack()
                },
                onSaveWeekTemplate = {
                    viewModel.updateTemplate(it)
                    navController.popBackStack()
                }
            )
        }
    }
}
