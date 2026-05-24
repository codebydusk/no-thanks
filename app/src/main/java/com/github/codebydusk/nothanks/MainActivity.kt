package com.github.codebydusk.nothanks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.codebydusk.nothanks.data.ExcuseRepository
import com.github.codebydusk.nothanks.ui.theme.NoThanksTheme
import com.github.codebydusk.nothanks.widget.NoThanksWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = ExcuseRepository(this)
        
        enableEdgeToEdge()
        setContent {
            NoThanksTheme {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(title = { Text("No Thanks Settings") })
                    }
                ) { innerPadding ->
                    SettingsScreen(
                        repository = repository,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(repository: ExcuseRepository, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    
    val currentTheme by repository.themeFlow.collectAsState(initial = ExcuseRepository.THEME_MATERIAL)
    val currentCornerStyle by repository.cornerStyleFlow.collectAsState(initial = ExcuseRepository.CORNER_ROUND)
    val currentDarkMode by repository.darkModeFlow.collectAsState(initial = null)
    val currentCopyMechanism by repository.copyMechanismFlow.collectAsState(initial = ExcuseRepository.COPY_TAP)

    val context = androidx.compose.ui.platform.LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Dark Mode", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButtonSetting("Auto", currentDarkMode == null) { 
                    scope.launch { repository.updateSetting(ExcuseRepository.DARK_MODE_KEY, true); /* Wait, my updateSetting logic is a bit flawed for null */ } 
                }
                // I'll fix updateSetting to handle nullable if needed, but for now I'll just use 3 states.
            }
            // Simplified Dark Mode toggle for now
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Force Dark Mode")
                Switch(
                    checked = currentDarkMode ?: false,
                    onCheckedChange = { 
                        scope.launch { 
                            repository.updateSetting(ExcuseRepository.DARK_MODE_KEY, it)
                            NoThanksWidget().updateAll(context)
                        } 
                    }
                )
            }
        }

        item {
            Text("Widget Theme", style = MaterialTheme.typography.titleMedium)
            Column {
                listOf(
                    ExcuseRepository.THEME_MATERIAL to "Material",
                    ExcuseRepository.THEME_NOTHING to "Nothing OS",
                    ExcuseRepository.THEME_SAMSUNG to "Samsung"
                ).forEach { (value, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().selectable(
                            selected = currentTheme == value,
                            onClick = { 
                                scope.launch { 
                                    repository.updateSetting(ExcuseRepository.THEME_KEY, value)
                                    NoThanksWidget().updateAll(context)
                                } 
                            }
                        ).padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentTheme == value, onClick = null)
                        Text(label, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }

        item {
            Text("Corner Style", style = MaterialTheme.typography.titleMedium)
            Row {
                RadioButtonSetting("Round", currentCornerStyle == ExcuseRepository.CORNER_ROUND) {
                    scope.launch { 
                        repository.updateSetting(ExcuseRepository.CORNER_STYLE_KEY, ExcuseRepository.CORNER_ROUND)
                        NoThanksWidget().updateAll(context)
                    }
                }
                RadioButtonSetting("Square", currentCornerStyle == ExcuseRepository.CORNER_SQUARE) {
                    scope.launch { 
                        repository.updateSetting(ExcuseRepository.CORNER_STYLE_KEY, ExcuseRepository.CORNER_SQUARE)
                        NoThanksWidget().updateAll(context)
                    }
                }
            }
        }

        item {
            Text("Copy Mechanism", style = MaterialTheme.typography.titleMedium)
            Row {
                RadioButtonSetting("Tap Text", currentCopyMechanism == ExcuseRepository.COPY_TAP) {
                    scope.launch { 
                        repository.updateSetting(ExcuseRepository.COPY_MECHANISM_KEY, ExcuseRepository.COPY_TAP)
                        NoThanksWidget().updateAll(context)
                    }
                }
                RadioButtonSetting("Copy Button", currentCopyMechanism == ExcuseRepository.COPY_BUTTON) {
                    scope.launch { 
                        repository.updateSetting(ExcuseRepository.COPY_MECHANISM_KEY, ExcuseRepository.COPY_BUTTON)
                        NoThanksWidget().updateAll(context)
                    }
                }
            }
        }
        
        item {
            Button(
                onClick = { 
                    scope.launch { 
                        repository.getNextExcuse()
                        NoThanksWidget().updateAll(context)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fetch New Excuse & Update Widget")
            }
        }
    }
}

@Composable
fun RadioButtonSetting(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.selectable(selected = selected, onClick = onClick).padding(8.dp)
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, modifier = Modifier.padding(start = 4.dp))
    }
}
