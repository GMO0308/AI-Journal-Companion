package com.example.aijournalcompanion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

/** Emotions offered as quick search chips. */
private val QUICK_EMOTIONS = listOf("JOY", "SADNESS", "ANGER", "FEAR", "SURPRISE")

/**
 * History screen: lists all journal entries and lets the user
 *  - search by emotion (method chosen from a dropdown)
 *  - sort by emotion (algorithm chosen from a dropdown)
 *  - open the pie chart popup
 *  - long-press and drag an entry onto the trash zone to delete it
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNewEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedSearchMethod by remember { mutableStateOf(SearchMethod.HASH_MAP) }
    var selectedSortMethod by remember { mutableStateOf<SortMethod?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showChart by remember { mutableStateOf(false) }

    val allEntries = JournalHistory.entries

    // Re-runs automatically when the query, method or entries change
    val searchResult by remember {
        derivedStateOf {
            SearchAlgorithms.search(selectedSearchMethod, allEntries, searchQuery)
        }
    }
    val visibleEntries = searchResult.entries

    // Drag-and-drop state
    var draggedEntry by remember { mutableStateOf<JournalEntry?>(null) }
    var trashBounds by remember { mutableStateOf<Rect?>(null) }
    var isHoveringTrash by remember { mutableStateOf(false) }

    /** True when the dragged card's centre is inside the trash zone. */
    fun isOverTrash(point: Offset): Boolean = trashBounds?.contains(point) == true

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // ---------- Search ----------
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by emotion, e.g. JOY") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Quick emotion chips fill in the search box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QUICK_EMOTIONS.forEach { emotion ->
                    val selected = normaliseQuery(searchQuery) == emotion
                    FilterChip(
                        selected = selected,
                        onClick = { searchQuery = if (selected) "" else emotion },
                        label = { Text(getEmotionEmoji(emotion)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            // ---------- Dropdowns: search method + sort method ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LabeledDropdown(
                    label = "Search with",
                    selectedText = selectedSearchMethod.label,
                    options = SearchMethod.entries.map { it.label },
                    onSelected = { index -> selectedSearchMethod = SearchMethod.entries[index] },
                    modifier = Modifier.weight(1f)
                )
                LabeledDropdown(
                    label = "Sort by emotion",
                    selectedText = selectedSortMethod?.label ?: "Choose...",
                    options = SortMethod.entries.map { it.label },
                    onSelected = { index ->
                        val method = SortMethod.entries[index]
                        selectedSortMethod = method
                        val nanos = JournalHistory.sort(method)
                        statusMessage = "Sorted A-Z with ${method.label} in ${formatTime(nanos)}"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ---------- Action buttons ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SecondaryButton(
                    text = "Show Chart",
                    onClick = { showChart = true },
                    modifier = Modifier.weight(1f)
                )
                SecondaryButton(
                    text = "New Entry",
                    onClick = onNewEntry,
                    modifier = Modifier.weight(1f)
                )
            }

            // ---------- Status line (results + timing) ----------
            val resultText = if (searchQuery.isBlank()) {
                "${allEntries.size} entries · long-press and drag to delete"
            } else {
                "${visibleEntries.size} found with ${selectedSearchMethod.label} " +
                        "in ${formatTime(searchResult.elapsedNanos)}"
            }
            Text(
                text = statusMessage?.let { "$it\n$resultText" } ?: resultText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            // ---------- Entry list ----------
            if (visibleEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "No entries yet." else "No matches found.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(visibleEntries, key = { it.id }) { entry ->
                        DraggableEntryCard(
                            entry = entry,
                            onDragStart = { draggedEntry = it },
                            onDrag = { centre -> isHoveringTrash = isOverTrash(centre) },
                            onDragEnd = { centre ->
                                if (isOverTrash(centre)) {
                                    JournalHistory.deleteEntry(entry)
                                    statusMessage = "Entry deleted"
                                }
                                draggedEntry = null
                                isHoveringTrash = false
                            },
                            onDragCancel = {
                                draggedEntry = null
                                isHoveringTrash = false
                            }
                        )
                    }
                }
            }
        }

        // ---------- Trash zone (only visible while dragging) ----------
        AnimatedVisibility(
            visible = draggedEntry != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val scale by animateFloatAsState(if (isHoveringTrash) 1.08f else 1f, label = "trashScale")
            val color = MaterialTheme.colorScheme.error

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(96.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .background(
                        color.copy(alpha = if (isHoveringTrash) 0.25f else 0.12f),
                        MaterialTheme.shapes.medium
                    )
                    .border(2.dp, color.copy(alpha = 0.6f), MaterialTheme.shapes.medium)
                    .onGloballyPositioned { trashBounds = it.boundsInRoot() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Delete, contentDescription = "Trash", tint = color)
                    Text(
                        text = if (isHoveringTrash) "Release to delete" else "Drop here to delete",
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Pie chart popup
    if (showChart) {
        PieChartDialog(onDismiss = { showChart = false })
    }
}

/**
 * Outlined button with a small label that opens a dropdown menu.
 * [onSelected] receives the index of the chosen option.
 */
@Composable
private fun LabeledDropdown(
    label: String,
    selectedText: String,
    options: List<String>,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(
                    selectedText,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            expanded = false
                            onSelected(index)
                        }
                    )
                }
            }
        }
    }
}

/**
 * An entry card that can be long-pressed and dragged.
 * Reports the card's CENTRE point (in root coordinates) so the screen can
 * check whether it is over the trash zone.
 */
@Composable
fun DraggableEntryCard(
    entry: JournalEntry,
    onDragStart: (JournalEntry) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: (Offset) -> Unit,
    onDragCancel: () -> Unit
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    var cardBounds by remember { mutableStateOf(Rect.Zero) }

    // Always call the latest callbacks from inside the gesture detector
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnDragCancel by rememberUpdatedState(onDragCancel)

    /** Centre of the card including how far it has been dragged. */
    fun dragCentre(): Offset = cardBounds.center + offset

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 1f else 0f)
            // Measured before .offset, so this is the card's resting position
            .onGloballyPositioned { if (!isDragging) cardBounds = it.boundsInRoot() }
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .pointerInput(entry.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        isDragging = true
                        currentOnDragStart(entry)
                    },
                    onDragEnd = {
                        currentOnDragEnd(dragCentre())
                        offset = Offset.Zero
                        isDragging = false
                    },
                    onDragCancel = {
                        offset = Offset.Zero
                        isDragging = false
                        currentOnDragCancel()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offset += dragAmount
                        currentOnDrag(dragCentre())
                    }
                )
            }
            .graphicsLayer(
                scaleX = if (isDragging) 0.95f else 1f,
                scaleY = if (isDragging) 0.95f else 1f,
                alpha = if (isDragging) 0.85f else 1f
            )
    ) {
        EntryCard(entry)
    }
}

/** Shows one journal entry: date, emotion + emoji, text and advice. */
@Composable
fun EntryCard(entry: JournalEntry) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(entry.date),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = getEmotionEmoji(entry.emotion),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = entry.text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            entry.advice?.let {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

/** Formats nanoseconds as a short readable time, e.g. "0.042 ms". */
private fun formatTime(nanos: Long): String =
    String.format(Locale.US, "%.3f ms", nanos / 1_000_000.0)
