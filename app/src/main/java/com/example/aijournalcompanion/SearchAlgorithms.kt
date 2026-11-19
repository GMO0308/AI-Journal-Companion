package com.example.aijournalcompanion

/** Search methods the user can pick from the dropdown on the History screen. */
enum class SearchMethod(val label: String) {
    HASH_MAP("HashMap search"),
    BINARY_TREE("Binary Tree search"),
    DOUBLY_LINKED_LIST("Doubly Linked List search")
}

/** Result of a search plus how long it took (used for performance evaluation). */
data class SearchResult(
    val entries: List<JournalEntry>,
    val elapsedNanos: Long
)

/**
 * SearchAlgorithms searches journal entries by EMOTION using one of three
 * custom data structures (see DataStructures.kt).
 *
 * All three methods use an exact, case-insensitive emotion match
 * (e.g. "joy" finds JOY entries), so they always return the same entries
 * and can be compared fairly.
 */
object SearchAlgorithms {

    /** Runs the chosen search method and times it. An empty query returns every entry. */
    fun search(method: SearchMethod, entries: List<JournalEntry>, query: String): SearchResult {
        val snapshot = entries.toList()
        if (query.isBlank()) return SearchResult(snapshot, 0L)

        val start = System.nanoTime()
        val results = when (method) {
            SearchMethod.HASH_MAP -> searchUsingMap(snapshot, query)
            SearchMethod.BINARY_TREE -> searchUsingBinaryTree(snapshot, query)
            SearchMethod.DOUBLY_LINKED_LIST -> searchUsingDLL(snapshot, query)
        }
        return SearchResult(results, System.nanoTime() - start)
    }

    // --- 1. HASH-BASED (MAP) SEARCH: average O(1) lookup ---
    fun searchUsingMap(entries: List<JournalEntry>, query: String): List<JournalEntry> {
        if (query.isBlank()) return entries.toList()
        return EmotionHashMap.from(entries).get(query)
    }

    // --- 2. BINARY SEARCH TREE SEARCH: O(log n) walk down the tree ---
    fun searchUsingBinaryTree(entries: List<JournalEntry>, query: String): List<JournalEntry> {
        if (query.isBlank()) return entries.toList()
        return EmotionBinaryTree.from(entries).search(query)
    }

    // --- 3. DOUBLY LINKED LIST SEARCH: O(n) traversal head -> tail ---
    fun searchUsingDLL(entries: List<JournalEntry>, query: String): List<JournalEntry> {
        if (query.isBlank()) return entries.toList()
        return JournalLinkedList.from(entries).searchByEmotion(query)
    }
}
