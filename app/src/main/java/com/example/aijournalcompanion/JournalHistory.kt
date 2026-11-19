package com.example.aijournalcompanion

import androidx.compose.runtime.mutableStateListOf

/**
 * JournalHistory manages the in-memory collection of journal entries.
 * The list is Compose state, so every screen updates automatically.
 */
object JournalHistory {
    private val _entries = mutableStateListOf<JournalEntry>()
    val entries: List<JournalEntry> get() = _entries

    /** Adds a new entry to the top of the list. */
    fun addEntry(entry: JournalEntry) {
        _entries.add(0, entry)
    }

    /** Removes an entry (used by drag-and-drop delete). */
    fun deleteEntry(entry: JournalEntry) {
        _entries.remove(entry)
    }

    /**
     * Sorts the entries by emotion with the chosen algorithm.
     * The algorithm runs on a copy, then the result replaces the list in one
     * step so the UI redraws once. Returns the time taken in nanoseconds.
     */
    fun sort(method: SortMethod): Long {
        val working = _entries.toMutableList()
        val start = System.nanoTime()
        when (method) {
            SortMethod.BUBBLE -> SortAlgorithms.bubbleSort(working)
            SortMethod.INSERTION -> SortAlgorithms.insertionSort(working)
            SortMethod.SELECTION -> SortAlgorithms.selectionSort(working)
        }
        val elapsed = System.nanoTime() - start

        _entries.clear()
        _entries.addAll(working)
        return elapsed
    }

    /** Counts how many entries there are for each emotion (used by the pie chart). */
    fun emotionCounts(): Map<String, Int> =
        _entries.groupBy { emotionKey(it) }.mapValues { it.value.size }
}
