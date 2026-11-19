package com.example.aijournalcompanion

/** Sorting methods the user can pick from the dropdown on the History screen. */
enum class SortMethod(val label: String) {
    BUBBLE("Bubble Sort"),
    INSERTION("Insertion Sort"),
    SELECTION("Selection Sort")
}

/**
 * SortAlgorithms contains custom implementations of three sorting algorithms.
 * All three sort entries BY EMOTION (A-Z). Entries with the same emotion are
 * ordered newest first, so every algorithm produces exactly the same result
 * and they can be compared fairly.
 */
object SortAlgorithms {

    /**
     * Compares two entries by emotion (A-Z), then by date (newest first).
     * Returns a negative number if [a] should come before [b].
     */
    fun compareByEmotion(a: JournalEntry, b: JournalEntry): Int {
        val byEmotion = emotionKey(a).compareTo(emotionKey(b))
        if (byEmotion != 0) return byEmotion
        return b.date.compareTo(a.date)
    }

    /** Bubble Sort: repeatedly swaps neighbours that are out of order. O(n^2). */
    fun bubbleSort(list: MutableList<JournalEntry>) {
        val n = list.size
        for (i in 0 until n - 1) {
            var swapped = false
            for (j in 0 until n - i - 1) {
                if (compareByEmotion(list[j], list[j + 1]) > 0) {
                    val temp = list[j]
                    list[j] = list[j + 1]
                    list[j + 1] = temp
                    swapped = true
                }
            }
            // Stop early if the list is already sorted
            if (!swapped) break
        }
    }

    /** Insertion Sort: inserts each entry into the sorted part on its left. O(n^2). */
    fun insertionSort(list: MutableList<JournalEntry>) {
        for (i in 1 until list.size) {
            val key = list[i]
            var j = i - 1
            while (j >= 0 && compareByEmotion(list[j], key) > 0) {
                list[j + 1] = list[j]
                j--
            }
            list[j + 1] = key
        }
    }

    /** Selection Sort: selects the smallest remaining entry each pass. O(n^2). */
    fun selectionSort(list: MutableList<JournalEntry>) {
        val n = list.size
        for (i in 0 until n - 1) {
            var minIdx = i
            for (j in i + 1 until n) {
                if (compareByEmotion(list[j], list[minIdx]) < 0) {
                    minIdx = j
                }
            }
            if (minIdx != i) {
                val temp = list[minIdx]
                list[minIdx] = list[i]
                list[i] = temp
            }
        }
    }
}
