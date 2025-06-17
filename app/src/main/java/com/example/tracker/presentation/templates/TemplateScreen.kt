package com.example.tracker.presentation.template

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TemplateScreen(viewModel: TemplateViewModel = TemplateViewModel()) {
    val mode by viewModel.templateMode
    val templates by viewModel.templates

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Templates",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ModeToggle(mode = mode, onModeChange = viewModel::onModeChange)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            items(templates) { template ->
                TemplateCard(
                    template = template,
                    mode = mode,
                    onApply = viewModel::onApplyTemplate,
                    onEdit = viewModel::onEditTemplate,
                    onDelete = viewModel::onDeleteTemplate,
                    modifier = Modifier.fillMaxWidth(1f)
                )
            }
        }
    }
}

@Composable
fun ModeToggle(mode: TemplateMode, onModeChange: (TemplateMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        TemplateMode.values().forEach {
            Button(
                onClick = { onModeChange(it) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (it == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(it.name)
            }
        }
    }
}

@Composable
fun TemplateCard(
    template: Template,
    mode: TemplateMode,
    onApply: (Template, ApplyTarget) -> Unit,
    onEdit: (Template) -> Unit,
    onDelete: (Template) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(template.name, style = MaterialTheme.typography.titleMedium)
                Row {
                    IconButton(onClick = { onEdit(template) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { onDelete(template) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (mode) {
                    TemplateMode.DAY -> {
                        Button(onClick = { onApply(template, ApplyTarget.TODAY) }) { Text("Apply Today") }
                        Button(onClick = { onApply(template, ApplyTarget.TOMORROW) }) { Text("Apply Tomorrow") }
                    }
                    TemplateMode.WEEK -> {
                        Button(onClick = { onApply(template, ApplyTarget.FROM_NOW) }) { Text("From Now") }
                        Button(onClick = { onApply(template, ApplyTarget.UPCOMING_WEEK) }) { Text("Upcoming Week") }
                    }
                }
            }
        }
    }
}

// ------ Supporting classes ------

data class Template(val name: String)

enum class TemplateMode { DAY, WEEK }

enum class ApplyTarget { TODAY, TOMORROW, FROM_NOW, UPCOMING_WEEK }

class TemplateViewModel {
    private val _templateMode = mutableStateOf(TemplateMode.DAY)
    val templateMode: State<TemplateMode> = _templateMode

    private val _templates = mutableStateOf(
        listOf(
            Template("Focused Work Routine"),
            Template("Relax & Recovery Plan"),
            Template("Balanced Day")
        )
    )
    val templates: State<List<Template>> = _templates

    fun onModeChange(newMode: TemplateMode) {
        _templateMode.value = newMode
    }

    fun onApplyTemplate(template: Template, target: ApplyTarget) {
        println("Applying ${template.name} to $target")
    }

    fun onEditTemplate(template: Template) {
        println("Editing template: ${template.name}")
    }

    fun onDeleteTemplate(template: Template) {
        _templates.value = _templates.value.filterNot { it == template }
    }
}

@Preview(showBackground = true)
@Composable
fun TemplateScreenPreview() {
    TemplateScreen()
}
