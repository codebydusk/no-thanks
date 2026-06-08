package com.github.codebydusk.nothanks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
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
                    if (currentCornerStyle == ExcuseRepository.CORNER_SHARP) {
                        Text(
                            text = "*Sharp corners may appear rounded on some launchers due to system-level widget rounding.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
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
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
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
            OutlinedButton(
                onClick = { onSelect(value) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ThemeOptionRow(label: String, selected: Boolean, theme: String, onClick: () -> Unit) {
    // Resolve preview colors for the theme chip
    val (bgColor, fgColor) = when (theme) {
        ExcuseRepository.THEME_NOTHING -> {
            androidx.compose.ui.graphics.Color(0xFF1B1B1D) to androidx.compose.ui.graphics.Color(0xFFD71921)
        }
        ExcuseRepository.THEME_GOLDEN -> {
            androidx.compose.ui.graphics.Color(0xFF1E1E24) to androidx.compose.ui.graphics.Color(0xFFFFCB47)
        }
        else -> {
            // System — use neutral M3 preview
            androidx.compose.ui.graphics.Color(0xFF1C1B1F) to androidx.compose.ui.graphics.Color(0xFFE6E1E5)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .selectable(selected = selected, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            label,
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        // Color preview chip
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(bgColor)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "No",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = fgColor
            )
        }
    }
}
