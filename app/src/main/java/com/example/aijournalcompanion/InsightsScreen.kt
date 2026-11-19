package com.example.aijournalcompanion

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Insights screen: shows the emotion distribution as a 2D pie chart
 * drawn with Compose Canvas.
 */
@Composable
fun InsightsScreen(
    onNewEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emotionCounts = JournalHistory.emotionCounts()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Emotion Distribution",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        if (emotionCounts.isEmpty()) {
            Text(
                text = "No data yet. Write a journal entry to see your chart.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 32.dp)
            )
            PrimaryButton(
                text = "Write an Entry",
                onClick = onNewEntry,
                icon = Icons.Filled.Create
            )
        } else {
            EmotionChartContent(emotionCounts, chartSize = 240.dp)
        }
    }
}

/**
 * Pie chart + legend. Shared by the Insights screen and the
 * "Show Chart" popup on the History screen.
 */
@Composable
fun EmotionChartContent(emotionCounts: Map<String, Int>, chartSize: Dp) {
    val total = emotionCounts.values.sum()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(chartSize), contentAlignment = Alignment.Center) {
            EmotionPieChart(emotionCounts)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Legend",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Largest emotion first
        emotionCounts.entries
            .sortedByDescending { it.value }
            .forEach { (emotion, count) -> LegendItem(emotion, count, total) }
    }
}

/** Draws the pie chart: one slice per emotion, sized by how often it appears. */
@Composable
fun EmotionPieChart(data: Map<String, Int>) {
    val total = data.values.sum().toFloat()
    val sliceBorder = MaterialTheme.colorScheme.surface

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (total <= 0f) return@Canvas
        var startAngle = -90f // Start from the top

        data.entries.forEach { entry ->
            val sweepAngle = (entry.value / total) * 360f
            // Filled slice
            drawArc(
                color = getEmotionColor(entry.key),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(size.width, size.height)
            )
            // Thin border between slices for a clean look
            drawArc(
                color = sliceBorder,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(size.width, size.height),
                style = Stroke(width = 3f)
            )
            startAngle += sweepAngle
        }
    }
}

/** One row of the legend: colour box, emotion with emoji, count and percentage. */
@Composable
fun LegendItem(emotion: String, count: Int, total: Int) {
    val percentage = if (total > 0) (count.toFloat() / total * 100).toInt() else 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(getEmotionColor(emotion), shape = MaterialTheme.shapes.extraSmall)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = getEmotionEmoji(emotion), modifier = Modifier.weight(1f))
        Text(text = "$count ($percentage%)", fontWeight = FontWeight.Bold)
    }
}

/** Pie chart popup (AlertDialog) used by the "Show Chart" button. */
@Composable
fun PieChartDialog(onDismiss: () -> Unit) {
    val emotionCounts = JournalHistory.emotionCounts()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Emotion Distribution") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (emotionCounts.isEmpty()) {
                    Text("No entries yet. Write a journal entry first.")
                } else {
                    EmotionChartContent(emotionCounts, chartSize = 200.dp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

/** Colour used for each emotion in the chart and legend. */
fun getEmotionColor(emotion: String): Color {
    return when (emotion.uppercase()) {
        "JOY" -> Color(0xFFFACC15)      // yellow
        "SADNESS" -> Color(0xFF60A5FA)  // blue
        "ANGER" -> Color(0xFFF97316)    // orange-red
        "FEAR" -> Color(0xFFA78BFA)     // purple
        "SURPRISE" -> Color(0xFF22C55E) // green
        else -> Color(0xFF9CA3AF)       // grey (neutral)
    }
}
