package com.islamic.bangla.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private fun formatTaka(value: Double): String = "৳ ${"%,.0f".format(value)}"

private fun String.toAmount(): Double = toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZakatScreen(onBack: () -> Unit) {
    var cash by rememberSaveable { mutableStateOf("") }
    var goldVori by rememberSaveable { mutableStateOf("") }
    var goldPrice by rememberSaveable { mutableStateOf("") }
    var silverVori by rememberSaveable { mutableStateOf("") }
    var silverPrice by rememberSaveable { mutableStateOf("") }
    var business by rememberSaveable { mutableStateOf("") }
    var debts by rememberSaveable { mutableStateOf("") }

    val goldValue = goldVori.toAmount() * goldPrice.toAmount()
    val silverValue = silverVori.toAmount() * silverPrice.toAmount()
    val netWealth = cash.toAmount() + goldValue + silverValue + business.toAmount() - debts.toAmount()

    // Nisab: value of 7.5 vori gold or 52.5 vori silver (whichever price is given).
    val nisabs = listOfNotNull(
        (7.5 * goldPrice.toAmount()).takeIf { goldPrice.toAmount() > 0 },
        (52.5 * silverPrice.toAmount()).takeIf { silverPrice.toAmount() > 0 }
    )
    val threshold = nisabs.minOrNull()
    val zakatDue = threshold != null && netWealth >= threshold && netWealth > 0
    val zakatAmount = if (zakatDue) netWealth * 0.025 else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("যাকাত ক্যালকুলেটর") },
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
                text = "নিসাব: ৭.৫ ভরি স্বর্ণ অথবা ৫২.৫ ভরি রৌপ্যের মূল্য। সম্পদ নিসাব পূর্ণ করে এক বছর থাকলে ২.৫% যাকাত ফরজ।",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            ZakatField(
                value = cash,
                onValueChange = { cash = it },
                label = "নগদ টাকা ও ব্যাংক ব্যালেন্স (৳)"
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                ZakatField(
                    value = goldVori,
                    onValueChange = { goldVori = it },
                    label = "স্বর্ণ (ভরি)",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ZakatField(
                    value = goldPrice,
                    onValueChange = { goldPrice = it },
                    label = "প্রতি ভরি দাম (৳)",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                ZakatField(
                    value = silverVori,
                    onValueChange = { silverVori = it },
                    label = "রৌপ্য (ভরি)",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ZakatField(
                    value = silverPrice,
                    onValueChange = { silverPrice = it },
                    label = "প্রতি ভরি দাম (৳)",
                    modifier = Modifier.weight(1f)
                )
            }
            ZakatField(
                value = business,
                onValueChange = { business = it },
                label = "ব্যবসায়িক পণ্যের মূল্য (৳)"
            )
            ZakatField(
                value = debts,
                onValueChange = { debts = it },
                label = "ঋণ / দেনা (৳)"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    ResultRow(label = "মোট সম্পদ", value = formatTaka(netWealth))
                    Spacer(modifier = Modifier.height(8.dp))
                    if (threshold != null) {
                        ResultRow(label = "নিসাব সীমা", value = formatTaka(threshold))
                        Spacer(modifier = Modifier.height(8.dp))
                        if (zakatDue) {
                            ResultRow(
                                label = "আদায়যোগ্য যাকাত (২.৫%)",
                                value = formatTaka(zakatAmount),
                                highlight = true
                            )
                        } else {
                            Text(
                                text = "নিসাব পূর্ণ হয়নি — যাকাত ফরজ নয়",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    } else {
                        Text(
                            text = "নিসাব হিসাব করতে স্বর্ণ বা রৌপ্যের দাম দিন",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "এটি আনুমানিক হিসাব; সঠিক মাসআলার জন্য আলেমের পরামর্শ নিন।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ZakatField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    )
}

@Composable
private fun ResultRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = if (highlight) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = if (highlight) {
                MaterialTheme.typography.titleLarge
            } else {
                MaterialTheme.typography.titleMedium
            },
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
