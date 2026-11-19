package com.example.aijournalcompanion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aijournalcompanion.ui.JournalViewModel

/**
 * Journal screen: the user writes an entry and presses Analyze (or the
 * keyboard's Enter/Done key) to send it to the AI.
 */
@Composable
fun JournalScreen(
    onViewHistory: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: JournalViewModel = viewModel()
) {
    val focusManager = LocalFocusManager.current

    // Start with an empty form every time this screen opens
    LaunchedEffect(Unit) {
        viewModel.reset()
    }

    /** Hides the keyboard and sends the entry for analysis. */
    fun submit() {
        focusManager.clearFocus()
        viewModel.analyzeJournal()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "How are you feeling today?",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        Text(
            text = "Write freely, then tap Analyze or press Enter.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Journal text input. The keyboard's Enter key is "Done" and submits the entry.
        OutlinedTextField(
            value = viewModel.journalText,
            onValueChange = { viewModel.journalText = it },
            label = { Text("Your thoughts...") },
            placeholder = { Text("e.g. I feel tired and overwhelmed today.") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            minLines = 5,
            enabled = !viewModel.isAnalyzing,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        if (viewModel.isAnalyzing) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "AI is reflecting on your thoughts...",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            // Main action: Analyze (Enter)
            PrimaryButton(
                text = "Analyze Emotion",
                onClick = { submit() },
                enabled = viewModel.journalText.isNotBlank(),
                icon = Icons.AutoMirrored.Filled.Send
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary action: Clear the text box
            SecondaryButton(
                text = "Clear",
                onClick = { viewModel.clearText() },
                enabled = viewModel.journalText.isNotEmpty(),
                icon = Icons.Filled.Clear,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // AI result card with emoji and advice
        viewModel.analysisResult?.let { result ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(emotionEmoji(result.emotion), fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dominant emotion",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = getEmotionEmoji(result.emotion),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "💡 ${result.advice ?: ""}",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Next steps after a result
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SecondaryButton(
                    text = "New Entry",
                    onClick = { viewModel.reset() },
                    icon = Icons.Filled.Refresh,
                    modifier = Modifier.weight(1f)
                )
                SecondaryButton(
                    text = "History",
                    onClick = onViewHistory,
                    icon = Icons.AutoMirrored.Filled.List,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Error message (e.g. backend or Ollama not running)
        viewModel.errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.size(24.dp))
    }
}
