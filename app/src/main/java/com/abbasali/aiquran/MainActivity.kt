package com.abbasali.aiquran

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class AppLanguage { BANGLA, ARABIC, ENGLISH }

private data class Copy(
    val title: String,
    val subtitle: String,
    val searchLabel: String,
    val searchHint: String,
    val offlineTitle: String,
    val offlineBody: String,
    val phaseTitle: String,
    val phaseBody: String,
    val sourceLabel: String
)

private fun copyFor(language: AppLanguage) = when (language) {
    AppLanguage.BANGLA -> Copy(
        title = "AI কুরআন",
        subtitle = "বাংলা • العربية • English",
        searchLabel = "কুরআন অনুসন্ধান",
        searchHint = "সূরা বা আয়াত খুঁজুন",
        offlineTitle = "অফলাইন কুরআন ডেটা এখনো যোগ করা হয়নি",
        offlineBody = "পূর্ণ আরবি পাঠ ও নির্বাচিত অনুবাদ যুক্ত করার আগে উৎসের লাইসেন্স এবং Content Sync নীতি যাচাই করা হবে। এই বিল্ডটি অ্যাপের প্রাথমিক কাঠামো।",
        phaseTitle = "পরবর্তী ধাপ",
        phaseBody = "লাইসেন্স-সম্মত ডেটা সিঙ্ক, Room ডেটাবেস, বুকমার্ক ও অফলাইন সার্চ। AI সহকারী পরবর্তী সংস্করণে যুক্ত হবে।",
        sourceLabel = "কনটেন্ট নীতি"
    )
    AppLanguage.ARABIC -> Copy(
        title = "القرآن بالذكاء الاصطناعي",
        subtitle = "বাংলা • العربية • English",
        searchLabel = "البحث في القرآن",
        searchHint = "ابحث عن سورة أو آية",
        offlineTitle = "لم تتم إضافة بيانات القرآن دون اتصال بعد",
        offlineBody = "سيتم التحقق من الترخيص وسياسة مزامنة المحتوى قبل إضافة النص العربي الكامل والترجمات المختارة. هذه نسخة أولية من التطبيق.",
        phaseTitle = "الخطوة التالية",
        phaseBody = "مزامنة المحتوى المرخّص، وقاعدة بيانات Room، والإشارات المرجعية والبحث دون اتصال. سيُضاف مساعد الذكاء الاصطناعي لاحقاً.",
        sourceLabel = "سياسة المحتوى"
    )
    AppLanguage.ENGLISH -> Copy(
        title = "AI Quran",
        subtitle = "বাংলা • العربية • English",
        searchLabel = "Search Quran",
        searchHint = "Find a surah or verse",
        offlineTitle = "Offline Quran data is not installed yet",
        offlineBody = "The source license and Content Sync policy will be verified before adding the complete Arabic text and selected translations. This build is the initial app foundation.",
        phaseTitle = "Next milestone",
        phaseBody = "Licensed content sync, Room database, bookmarks, and offline search. The AI assistant will be added in a later release.",
        sourceLabel = "Content policy"
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    QuranHome()
                }
            }
        }
    }
}

@Composable
private fun QuranHome() {
    var language by remember { mutableStateOf(AppLanguage.BANGLA) }
    var languageMenuOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val copy = copyFor(language)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(copy.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(copy.subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                TextButton(onClick = { languageMenuOpen = true }) {
                    Text(
                        when (language) {
                            AppLanguage.BANGLA -> "বাংলা ▾"
                            AppLanguage.ARABIC -> "العربية ▾"
                            AppLanguage.ENGLISH -> "English ▾"
                        }
                    )
                }
                DropdownMenu(
                    expanded = languageMenuOpen,
                    onDismissRequest = { languageMenuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("বাংলা") },
                        onClick = { language = AppLanguage.BANGLA; languageMenuOpen = false }
                    )
                    DropdownMenuItem(
                        text = { Text("العربية") },
                        onClick = { language = AppLanguage.ARABIC; languageMenuOpen = false }
                    )
                    DropdownMenuItem(
                        text = { Text("English") },
                        onClick = { language = AppLanguage.ENGLISH; languageMenuOpen = false }
                    )
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(copy.searchLabel) },
            placeholder = { Text(copy.searchHint) },
            singleLine = true,
            enabled = false
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("﷽", fontSize = 36.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Text(copy.offlineTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(copy.offlineBody, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(copy.phaseTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(copy.phaseBody, style = MaterialTheme.typography.bodyMedium)
                Button(onClick = { languageMenuOpen = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(copy.sourceLabel)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "Android 11+ • Offline-first foundation • AI planned for a later milestone",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
