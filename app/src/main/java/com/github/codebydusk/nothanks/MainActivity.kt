package com.github.codebydusk.nothanks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            val currentDarkMode by repository.darkModeFlow.collectAsState(initial = ExcuseRepository.DARK_MODE_SYSTEM)
            val isDark = when (currentDarkMode) {
                ExcuseRepository.DARK_MODE_DARK -> true
                ExcuseRepository.DARK_MODE_LIGHT -> false
                else -> isSystemInDarkTheme()
            }

            NoThanksTheme(darkTheme = isDark) {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("No, thanks!") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
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
    
    val currentTheme by repository.themeFlow.collectAsState(initial = ExcuseRepository.THEME_NOTHING)
    val currentCornerStyle by repository.cornerStyleFlow.collectAsState(initial = ExcuseRepository.CORNER_ROUND)
    val currentDarkMode by repository.darkModeFlow.collectAsState(initial = ExcuseRepository.DARK_MODE_SYSTEM)
    val currentCopyMechanism by repository.copyMechanismFlow.collectAsState(initial = ExcuseRepository.COPY_TAP)
    val showPrevButton by repository.showPrevButtonFlow.collectAsState(initial = true)
    val includeCopyPrefix by repository.copyPrefixFlow.collectAsState(initial = false)
    val currentTextSize by repository.textSizeFlow.collectAsState(initial = ExcuseRepository.TEXT_SIZE_NORMAL)
    val currentFontStyle by repository.fontStyleFlow.collectAsState(initial = ExcuseRepository.FONT_STYLE_MONOSPACE)

    val context = androidx.compose.ui.platform.LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // App header / tagline
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = "No, Thanks! logo",
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ever needed a graceful way to say no?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Dark Mode / Appearance section
        item {
            SettingsSection(title = "Appearance") {
                val darkModeOptions = listOf(
                    ExcuseRepository.DARK_MODE_SYSTEM to "System",
                    ExcuseRepository.DARK_MODE_LIGHT to "Light",
                    ExcuseRepository.DARK_MODE_DARK to "Dark"
                )
                SegmentedOptions(
                    options = darkModeOptions,
                    selectedValue = currentDarkMode,
                    onSelect = { value ->
                        scope.launch {
                            repository.updateSetting(ExcuseRepository.DARK_MODE_KEY, value)
                            NoThanksWidget().updateAll(context)
                        }
                    }
                )
            }
        }

        // Widget Theme section — 3 themes only
        item {
            SettingsSection(title = "Widget Theme") {
                val themeOptions = listOf(
                    ExcuseRepository.THEME_NOTHING to "Nothing OS",
                    ExcuseRepository.THEME_GOLDEN to "Golden Silence",
                    ExcuseRepository.THEME_OLED to "OLED",
                    ExcuseRepository.THEME_SYSTEM to "System"
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    themeOptions.forEach { (value, label) ->
                        ThemeOptionRow(
                            label = label,
                            selected = currentTheme == value,
                            theme = value,
                            onClick = {
                                scope.launch {
                                    repository.updateSetting(ExcuseRepository.THEME_KEY, value)
                                    NoThanksWidget().updateAll(context)
                                }
                            }
                        )
                    }
                    Text(
                        text = "*Widget themes only apply to the home screen widget.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // Corner Style section
        item {
            SettingsSection(title = "Corner Style") {
                val cornerOptions = listOf(
                    ExcuseRepository.CORNER_ROUND to "Pill",
                    ExcuseRepository.CORNER_SQUARE to "Rounded",
                    ExcuseRepository.CORNER_SHARP to "Sharp"
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SegmentedOptions(
                        options = cornerOptions,
                        selectedValue = currentCornerStyle,
                        onSelect = { value ->
                            scope.launch {
                                repository.updateSetting(ExcuseRepository.CORNER_STYLE_KEY, value)
                                NoThanksWidget().updateAll(context)
                            }
                        }
                    )
                    Text(
                        text = "*Sharp corners may appear rounded on some launchers due to system-level widget rounding.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Text Size section
        item {
            SettingsSection(title = "Text Size") {
                val textSizeOptions = listOf(
                    ExcuseRepository.TEXT_SIZE_SMALL to "Small",
                    ExcuseRepository.TEXT_SIZE_NORMAL to "Normal",
                    ExcuseRepository.TEXT_SIZE_LARGE to "Large"
                )
                SegmentedOptions(
                    options = textSizeOptions,
                    selectedValue = currentTextSize,
                    onSelect = { value ->
                        scope.launch {
                            repository.updateSetting(ExcuseRepository.TEXT_SIZE_KEY, value)
                            NoThanksWidget().updateAll(context)
                        }
                    }
                )
            }
        }

        // Font Style section
        item {
            SettingsSection(title = "Font Style") {
                val fontStyleOptions = listOf(
                    ExcuseRepository.FONT_STYLE_SYSTEM to "System Default",
                    ExcuseRepository.FONT_STYLE_MONOSPACE to "Monospace"
                )
                SegmentedOptions(
                    options = fontStyleOptions,
                    selectedValue = currentFontStyle,
                    onSelect = { value ->
                        scope.launch {
                            repository.updateSetting(ExcuseRepository.FONT_STYLE_KEY, value)
                            NoThanksWidget().updateAll(context)
                        }
                    }
                )
            }
        }

        // Copy Mechanism section
        item {
            SettingsSection(title = "Copy Mechanism") {
                val copyOptions = listOf(
                    ExcuseRepository.COPY_TAP to "Tap Text",
                    ExcuseRepository.COPY_BUTTON to "Copy Button"
                )
                SegmentedOptions(
                    options = copyOptions,
                    selectedValue = currentCopyMechanism,
                    onSelect = { value ->
                        scope.launch {
                            repository.updateSetting(ExcuseRepository.COPY_MECHANISM_KEY, value)
                            NoThanksWidget().updateAll(context)
                        }
                    }
                )
            }
        }

        // Copy Prefix toggle
        item {
            SettingsSection(title = "Copy Prefix") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Include \"No, thanks!\" Prefix",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Include \"No, thanks!\" in copied text when enabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = includeCopyPrefix,
                        onCheckedChange = { checked ->
                            scope.launch {
                                repository.updateSetting(ExcuseRepository.COPY_PREFIX_KEY, checked)
                                NoThanksWidget().updateAll(context)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = MaterialTheme.colorScheme.background,
                            uncheckedTrackColor = MaterialTheme.colorScheme.background,
                            checkedBorderColor = MaterialTheme.colorScheme.primary,
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }

        // Show Previous Button toggle
        item {
            SettingsSection(title = "Navigation") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Show Previous Button",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Show ← button to browse excuse history",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = showPrevButton,
                        onCheckedChange = { checked ->
                            scope.launch {
                                repository.updateSetting(ExcuseRepository.SHOW_PREV_BUTTON_KEY, checked)
                                NoThanksWidget().updateAll(context)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = MaterialTheme.colorScheme.background,
                            uncheckedTrackColor = MaterialTheme.colorScheme.background,
                            checkedBorderColor = MaterialTheme.colorScheme.primary,
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }

        // Bottom spacer for edge-to-edge
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun SegmentedOptions(
    options: List<Pair<String, String>>,
    selectedValue: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (value, label) ->
            val isSelected = selectedValue == value
            Button(
                onClick = { onSelect(value) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ThemeOptionRow(label: String, selected: Boolean, theme: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .selectable(selected = selected, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(start = 8.dp, end = 12.dp)
                .size(18.dp)
                .background(
                    color = if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                    shape = androidx.compose.foundation.shape.CircleShape
                )
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )
        Text(
            label,
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
