package com.abbasali.aiquran

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.produceState
import kotlinx.coroutines.launch
import com.abbasali.aiquran.data.BookmarkEntity
import com.abbasali.aiquran.data.QuranDatabase
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class Tab { HOME, SURAHS, BOOKMARKS, SETTINGS }
private data class Surah(val number: Int, val arabic: String, val english: String, val bangla: String, val ayahs: Int, val place: String)

private val surahs = """
1|الفاتحة|Al-Fatihah|আল-ফাতিহা|7|মাক্কী
2|البقرة|Al-Baqarah|আল-বাকারা|286|মাদানী
3|آل عمران|Ali 'Imran|আলে ইমরান|200|মাদানী
4|النساء|An-Nisa|আন-নিসা|176|মাদানী
5|المائدة|Al-Ma'idah|আল-মায়িদাহ|120|মাদানী
6|الأنعام|Al-An'am|আল-আনআম|165|মাক্কী
7|الأعراف|Al-A'raf|আল-আ'রাফ|206|মাক্কী
8|الأنفال|Al-Anfal|আল-আনফাল|75|মাদানী
9|التوبة|At-Tawbah|আত-তাওবাহ|129|মাদানী
10|يونس|Yunus|ইউনুস|109|মাক্কী
11|هود|Hud|হুদ|123|মাক্কী
12|يوسف|Yusuf|ইউসুফ|111|মাক্কী
13|الرعد|Ar-Ra'd|আর-রা'দ|43|মাদানী
14|إبراهيم|Ibrahim|ইবরাহিম|52|মাক্কী
15|الحجر|Al-Hijr|আল-হিজর|99|মাক্কী
16|النحل|An-Nahl|আন-নাহল|128|মাক্কী
17|الإسراء|Al-Isra|আল-ইসরা|111|মাক্কী
18|الكهف|Al-Kahf|আল-কাহফ|110|মাক্কী
19|مريم|Maryam|মারইয়াম|98|মাক্কী
20|طه|Ta-Ha|ত্বা-হা|135|মাক্কী
21|الأنبياء|Al-Anbya|আল-আম্বিয়া|112|মাক্কী
22|الحج|Al-Hajj|আল-হজ্জ|78|মাদানী
23|المؤمنون|Al-Mu'minun|আল-মুমিনুন|118|মাক্কী
24|النور|An-Nur|আন-নূর|64|মাদানী
25|الفرقان|Al-Furqan|আল-ফুরকান|77|মাক্কী
26|الشعراء|Ash-Shu'ara|আশ-শু'আরা|227|মাক্কী
27|النمل|An-Naml|আন-নামল|93|মাক্কী
28|القصص|Al-Qasas|আল-কাসাস|88|মাক্কী
29|العنكبوت|Al-Ankabut|আল-আনকাবুত|69|মাক্কী
30|الروم|Ar-Rum|আর-রূম|60|মাক্কী
31|لقمان|Luqman|লুকমান|34|মাক্কী
32|السجدة|As-Sajdah|আস-সাজদাহ|30|মাক্কী
33|الأحزاب|Al-Ahzab|আল-আহযাব|73|মাদানী
34|سبأ|Saba|সাবা|54|মাক্কী
35|فاطر|Fatir|ফাতির|45|মাক্কী
36|يس|Ya-Sin|ইয়া-সীন|83|মাক্কী
37|الصافات|As-Saffat|আস-সাফফাত|182|মাক্কী
38|ص|Sad|সাদ|88|মাক্কী
39|الزمر|Az-Zumar|আয-যুমার|75|মাক্কী
40|غافر|Ghafir|গাফির|85|মাক্কী
41|فصلت|Fussilat|ফুসসিলাত|54|মাক্কী
42|الشورى|Ash-Shura|আশ-শূরা|53|মাক্কী
43|الزخرف|Az-Zukhruf|আয-যুখরুফ|89|মাক্কী
44|الدخان|Ad-Dukhan|আদ-দুখান|59|মাক্কী
45|الجاثية|Al-Jathiyah|আল-জাসিয়াহ|37|মাক্কী
46|الأحقاف|Al-Ahqaf|আল-আহকাফ|35|মাক্কী
47|محمد|Muhammad|মুহাম্মদ|38|মাদানী
48|الفتح|Al-Fath|আল-ফাতহ|29|মাদানী
49|الحجرات|Al-Hujurat|আল-হুজুরাত|18|মাদানী
50|ق|Qaf|কাফ|45|মাক্কী
51|الذاريات|Adh-Dhariyat|আয-যারিয়াত|60|মাক্কী
52|الطور|At-Tur|আত-তূর|49|মাক্কী
53|النجم|An-Najm|আন-নাজম|62|মাক্কী
54|القمر|Al-Qamar|আল-কামার|55|মাক্কী
55|الرحمن|Ar-Rahman|আর-রহমান|78|মাদানী
56|الواقعة|Al-Waqi'ah|আল-ওয়াকিয়াহ|96|মাক্কী
57|الحديد|Al-Hadid|আল-হাদীদ|29|মাদানী
58|المجادلة|Al-Mujadilah|আল-মুজাদালাহ|22|মাদানী
59|الحشر|Al-Hashr|আল-হাশর|24|মাদানী
60|الممتحنة|Al-Mumtahanah|আল-মুমতাহিনাহ|13|মাদানী
61|الصف|As-Saff|আস-সাফ|14|মাদানী
62|الجمعة|Al-Jumu'ah|আল-জুমুআহ|11|মাদানী
63|المنافقون|Al-Munafiqun|আল-মুনাফিকুন|11|মাদানী
64|التغابن|At-Taghabun|আত-তাগাবুন|18|মাদানী
65|الطلاق|At-Talaq|আত-তালাক|12|মাদানী
66|التحريم|At-Tahrim|আত-তাহরীম|12|মাদানী
67|الملك|Al-Mulk|আল-মুলক|30|মাক্কী
68|القلم|Al-Qalam|আল-কলম|52|মাক্কী
69|الحاقة|Al-Haqqah|আল-হাক্কাহ|52|মাক্কী
70|المعارج|Al-Ma'arij|আল-মাআরিজ|44|মাক্কী
71|نوح|Nuh|নূহ|28|মাক্কী
72|الجن|Al-Jinn|আল-জিন|28|মাক্কী
73|المزمل|Al-Muzzammil|আল-মুযযাম্মিল|20|মাক্কী
74|المدثر|Al-Muddaththir|আল-মুদ্দাসসির|56|মাক্কী
75|القيامة|Al-Qiyamah|আল-কিয়ামাহ|40|মাক্কী
76|الإنسان|Al-Insan|আল-ইনসান|31|মাদানী
77|المرسلات|Al-Mursalat|আল-মুরসালাত|50|মাক্কী
78|النبأ|An-Naba|আন-নাবা|40|মাক্কী
79|النازعات|An-Nazi'at|আন-নাযিআত|46|মাক্কী
80|عبس|Abasa|আবাসা|42|মাক্কী
81|التكوير|At-Takwir|আত-তাকভীর|29|মাক্কী
82|الانفطار|Al-Infitar|আল-ইনফিতার|19|মাক্কী
83|المطففين|Al-Mutaffifin|আল-মুতাফফিফীন|36|মাক্কী
84|الانشقاق|Al-Inshiqaq|আল-ইনশিকাক|25|মাক্কী
85|البروج|Al-Buruj|আল-বুরুজ|22|মাক্কী
86|الطارق|At-Tariq|আত-তারিক|17|মাক্কী
87|الأعلى|Al-A'la|আল-আ'লা|19|মাক্কী
88|الغاشية|Al-Ghashiyah|আল-গাশিয়াহ|26|মাক্কী
89|الفجر|Al-Fajr|আল-ফজর|30|মাক্কী
90|البلد|Al-Balad|আল-বালাদ|20|মাক্কী
91|الشمس|Ash-Shams|আশ-শামস|15|মাক্কী
92|الليل|Al-Layl|আল-লাইল|21|মাক্কী
93|الضحى|Ad-Duha|আদ-দুহা|11|মাক্কী
94|الشرح|Ash-Sharh|আশ-শারহ|8|মাক্কী
95|التين|At-Tin|আত-তীন|8|মাক্কী
96|العلق|Al-Alaq|আল-আলাক|19|মাক্কী
97|القدر|Al-Qadr|আল-কদর|5|মাক্কী
98|البينة|Al-Bayyinah|আল-বাইয়িনাহ|8|মাদানী
99|الزلزلة|Az-Zalzalah|আয-যিলযাল|8|মাদানী
100|العاديات|Al-Adiyat|আল-আদিয়াত|11|মাক্কী
101|القارعة|Al-Qari'ah|আল-কারিআহ|11|মাক্কী
102|التكاثر|At-Takathur|আত-তাকাসুর|8|মাক্কী
103|العصر|Al-Asr|আল-আসর|3|মাক্কী
104|الهمزة|Al-Humazah|আল-হুমাযাহ|9|মাক্কী
105|الفيل|Al-Fil|আল-ফীল|5|মাক্কী
106|قريش|Quraysh|কুরাইশ|4|মাক্কী
107|الماعون|Al-Ma'un|আল-মাউন|7|মাক্কী
108|الكوثر|Al-Kawthar|আল-কাওসার|3|মাক্কী
109|الكافرون|Al-Kafirun|আল-কাফিরুন|6|মাক্কী
110|النصر|An-Nasr|আন-নাসর|3|মাদানী
111|المسد|Al-Masad|আল-মাসাদ|5|মাক্কী
112|الإخلاص|Al-Ikhlas|আল-ইখলাস|4|মাক্কী
113|الفلق|Al-Falaq|আল-ফালাক|5|মাক্কী
114|الناس|An-Nas|আন-নাস|6|মাক্কী
""".trimIndent().lines().map { line ->
    val p = line.split("|")
    Surah(p[0].toInt(), p[1], p[2], p[3], p[4].toInt(), p[5])
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { QuranApp() }
            }
        }
    }
}

@Composable
private fun QuranApp() {
    var tab by remember { mutableStateOf(Tab.HOME) }
    var openedSurah by remember { mutableStateOf<Surah?>(null) }
    var query by remember { mutableStateOf("") }
    var fontSize by remember { mutableFloatStateOf(30f) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bookmarks by produceState(initialValue = emptyList<BookmarkEntity>(), context) {
        QuranDatabase.getInstance(context).quranDao().observeBookmarks().collect { value = it }
    }
    val savedSurahs = remember(bookmarks) {
        surahs.filter { s -> bookmarks.any { it.verseKey == "surah:${s.number}" } }
    }
    fun toggleSurahBookmark(surah: Surah) {
        scope.launch {
            val dao = QuranDatabase.getInstance(context).quranDao()
            val key = "surah:${surah.number}"
            if (bookmarks.any { it.verseKey == key }) {
                dao.deleteBookmark(key)
            } else {
                dao.upsertBookmark(
                    BookmarkEntity(
                        verseKey = key,
                        createdAtEpochMillis = System.currentTimeMillis(),
                        note = "saved-surah"
                    )
                )
            }
        }
    }
    Scaffold(bottomBar = {
        NavigationBar {
            NavigationBarItem(tab == Tab.HOME, { tab = Tab.HOME; openedSurah = null }, icon = { Text("⌂") }, label = { Text("হোম") })
            NavigationBarItem(tab == Tab.SURAHS, { tab = Tab.SURAHS; openedSurah = null }, icon = { Text("☷") }, label = { Text("সূরা") })
            NavigationBarItem(tab == Tab.BOOKMARKS, { tab = Tab.BOOKMARKS; openedSurah = null }, icon = { Text("☆") }, label = { Text("বুকমার্ক") })
            NavigationBarItem(tab == Tab.SETTINGS, { tab = Tab.SETTINGS; openedSurah = null }, icon = { Text("⚙") }, label = { Text("সেটিংস") })
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("القرآن الكريم", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("AI Quran • কুরআন পাঠক", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("بِسْمِ اللَّهِ", fontSize = 20.sp)
            }
            Spacer(Modifier.height(10.dp))
            if (openedSurah != null) {
                SurahDetails(openedSurah!!, fontSize, bookmarks.any { it.verseKey == "surah:${openedSurah!!.number}" }, { toggleSurahBookmark(openedSurah!!) }) { openedSurah = null }
            } else when (tab) {
                Tab.HOME -> HomeScreen(fontSize, { tab = Tab.SURAHS }, { openedSurah = it })
                Tab.SURAHS -> {
                    OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("সূরা খুঁজুন") }, placeholder = { Text("বাংলা/ইংরেজি নাম বা নম্বর") }, singleLine = true)
                    val filtered = surahs.filter { it.bangla.contains(query, true) || it.english.contains(query, true) || it.number.toString() == query.trim() }
                    Text("মোট " + surahs.size + "টি সূরা", Modifier.padding(vertical = 8.dp), style = MaterialTheme.typography.titleMedium)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp)) { items(filtered, key = { it.number }) { SurahRow(it) { openedSurah = it } } }
                }
                Tab.BOOKMARKS -> {
                    if (savedSurahs.isEmpty()) {
                        EmptyFeature("আপনার বুকমার্ক", "সূরার পৃষ্ঠা খুলে বুকমার্ক বোতাম চাপুন। সংরক্ষণ এই ফোনের স্থানীয় ডেটাবেসে থাকবে।")
                    } else {
                        Column(Modifier.fillMaxSize()) {
                            Text("সংরক্ষিত সূরা (${savedSurahs.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                items(savedSurahs, key = { it.number }) { s -> SurahRow(s) { openedSurah = s } }
                            }
                        }
                    }
                }
                Tab.SETTINGS -> Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("পাঠের সেটিংস", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("আরবি ফন্টের আকার: " + fontSize.toInt() + " sp")
                    Slider(value = fontSize, onValueChange = { fontSize = it }, valueRange = 24f..44f)
                    Text("অ্যাপের বর্তমান অবস্থা", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("সূরা তালিকা ও অনুসন্ধান প্রস্তুত। পূর্ণ আয়াত, অনুবাদ, অফলাইন সিঙ্ক ও বুকমার্কের কাজ পরবর্তী ধাপে হবে।")
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(fontSize: Float, onAllSurahs: () -> Unit, onOpen: (Surah) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = fontSize.sp)
                Text("আসসালামু আলাইকুম", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("আপনার কুরআন পাঠ শুরু করুন। সূরা তালিকা থেকে যেকোনো সূরা বেছে নিন।")
                Button(onClick = onAllSurahs, Modifier.fillMaxWidth()) { Text("সব ১১৪টি সূরা দেখুন") }
            }
        }
        Text("দ্রুত শুরু", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        listOf(surahs[0], surahs[111], surahs[112], surahs[113]).forEach { SurahRow(it) { onOpen(it) } }
        Divider()
        Text("নোট: এই সংস্করণে সূরার নাম ও তথ্য আছে; পূর্ণ আরবি আয়াত ও অনুবাদ ডেটা এখনো যুক্ত হয়নি।", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SurahRow(surah: Surah, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(surah.number.toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)) {
                Text(surah.bangla, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(surah.english + " • " + surah.ayahs + " আয়াত • " + surah.place, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(surah.arabic, fontSize = 20.sp, textAlign = TextAlign.End)
        }
    }
}

@Composable
private fun SurahDetails(surah: Surah, fontSize: Float, isBookmarked: Boolean, onToggleBookmark: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TextButton(onClick = onBack) { Text("← সূরা তালিকায় ফিরে যান") }
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(surah.arabic, fontSize = (fontSize + 6).sp)
                Text(surah.bangla, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(surah.english)
                Text(surah.number.toString() + " নম্বর সূরা • " + surah.ayahs + " আয়াত • " + surah.place)
            }
        }
        Button(onClick = onToggleBookmark, modifier = Modifier.fillMaxWidth()) { Text(if (isBookmarked) "★ বুকমার্ক থেকে সরান" else "☆ সূরা বুকমার্ক করুন") }
        Text("আয়াতের পাঠ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("এই সূরার পূর্ণ আরবি আয়াত ও বাংলা অনুবাদ পরবর্তী ডেটা-সিঙ্ক ধাপে যুক্ত হবে। ভুল বা অসম্পূর্ণ কুরআনের পাঠ দেখানোর পরিবর্তে এখানে স্পষ্টভাবে জানানো হচ্ছে যে ডেটা এখনো যোগ হয়নি।", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun EmptyFeature(title: String, body: String) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Card(Modifier.fillMaxWidth()) { Text(body, Modifier.padding(18.dp), style = MaterialTheme.typography.bodyLarge) }
    }
}
