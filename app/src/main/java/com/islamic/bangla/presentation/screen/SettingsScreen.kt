package com.islamic.bangla.presentation.screen

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.islamic.bangla.data.preferences.ThemeMode
import com.islamic.bangla.presentation.theme.ScaledArabicTextStyle
import com.islamic.bangla.presentation.theme.ScaledBanglaTextStyle
import com.islamic.bangla.presentation.viewmodel.SettingsViewModel

private data class ThemeOption(
    val mode: ThemeMode,
    val label: String,
    val icon: ImageVector
)

private val banglaDigits = arrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

private fun Int.toBanglaDigits(): String =
    toString().map { if (it in '0'..'9') banglaDigits[it - '0'] else it }.joinToString("")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val azanEnabled by viewModel.azanEnabled.collectAsStateWithLifecycle()
    val arabicFontScale by viewModel.arabicFontScale.collectAsStateWithLifecycle()
    val banglaFontScale by viewModel.banglaFontScale.collectAsStateWithLifecycle()
    val prayerToggles by viewModel.prayerToggles.collectAsStateWithLifecycle()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.setNotificationsEnabled(true)
    }

    val options = listOf(
        ThemeOption(ThemeMode.SYSTEM, "সিস্টেম অনুযায়ী", Icons.Filled.PhoneAndroid),
        ThemeOption(ThemeMode.LIGHT, "লাইট", Icons.Filled.LightMode),
        ThemeOption(ThemeMode.DARK, "ডার্ক", Icons.Filled.DarkMode)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("সেটিংস") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "ফিরে যান")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "নামাজের নোটিফিকেশন",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (notificationsEnabled) {
                                    viewModel.setNotificationsEnabled(false)
                                } else {
                                    enableNotifications(context, viewModel) {
                                        notificationPermissionLauncher.launch(
                                            Manifest.permission.POST_NOTIFICATIONS
                                        )
                                    }
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ওয়াক্তের নোটিফিকেশন",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "প্রতি ওয়াক্তে নোটিফিকেশন পান",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { on ->
                                if (!on) {
                                    viewModel.setNotificationsEnabled(false)
                                } else {
                                    enableNotifications(context, viewModel) {
                                        notificationPermissionLauncher.launch(
                                            Manifest.permission.POST_NOTIFICATIONS
                                        )
                                    }
                                }
                            }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = notificationsEnabled) {
                                viewModel.setAzanEnabled(!azanEnabled)
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VolumeUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "আজান বাজান",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (notificationsEnabled) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Text(
                                text = "ওয়াক্তে আজানের শব্দ (প্রথমবার ডাউনলোড হবে)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = azanEnabled,
                            enabled = notificationsEnabled,
                            onCheckedChange = viewModel::setAzanEnabled
                        )
                    }
                    viewModel.prayerList.forEach { prayer ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = notificationsEnabled) {
                                    viewModel.setPrayerEnabled(
                                        prayer.key,
                                        !prayerToggles.forKey(prayer.key)
                                    )
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${prayer.banglaName} নামাজ",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 36.dp),
                                color = if (notificationsEnabled) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Switch(
                                checked = prayerToggles.forKey(prayer.key),
                                enabled = notificationsEnabled,
                                onCheckedChange = { on ->
                                    viewModel.setPrayerEnabled(prayer.key, on)
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "থিম",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setThemeMode(option.mode) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            RadioButton(
                                selected = themeMode == option.mode,
                                onClick = { viewModel.setThemeMode(option.mode) }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "ফন্ট সাইজ",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FontSliderRow(
                        label = "আরবি লেখা",
                        scale = arabicFontScale,
                        onScaleChange = viewModel::setArabicFontScale
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FontSliderRow(
                        label = "বাংলা অনুবাদ",
                        scale = banglaFontScale,
                        onScaleChange = viewModel::setBanglaFontScale
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                        style = ScaledArabicTextStyle(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "পরম করুণাময়, অসীম দয়ালু আল্লাহর নামে",
                        style = ScaledBanglaTextStyle(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = viewModel::resetFontScales) {
                            Text("ডিফল্ট করুন")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "অ্যাপ সম্পর্কে",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ইসলামিক বাংলা",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "সংস্করণ ১.০.০",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "বাংলা ভাষায় ইসলামিক জ্ঞান — অফলাইনে পড়ুন",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "আজান অডিও: Wikimedia Commons (CC BY-SA 4.0)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FontSliderRow(
    label: String,
    scale: Float,
    onScaleChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${(scale * 100).toInt().toBanglaDigits()}%",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
    Slider(
        value = scale,
        onValueChange = onScaleChange,
        valueRange = 0.8f..1.4f,
        steps = 5
    )
}

/**
 * Enables notifications, requesting POST_NOTIFICATIONS (Android 13+) and
 * exact-alarm permission (Android 12+) as needed.
 */
private fun enableNotifications(
    context: Context,
    viewModel: SettingsViewModel,
    requestPostNotifications: () -> Unit
) {
    if (Build.VERSION.SDK_INT >= 33 &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        requestPostNotifications()
        return
    }
    if (Build.VERSION.SDK_INT >= 31) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
            context.startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
            return
        }
    }
    viewModel.setNotificationsEnabled(true)
}
