package com.example.aijournalcompanion

/**
 * Custom data structures used for emotion-based searching.
 *
 *  - EmotionBinaryTree   : Binary Search Tree keyed by emotion
 *  - EmotionHashMap      : Hash-based index (emotion -> entries)
 *  - JournalLinkedList   : Doubly Linked List of entries
 *
 * All emotion keys are normalised with [emotionKey] so every structure
 * matches entries in exactly the same way.
 */

/** Returns the upper-case emotion key for an entry (missing emotion = NEUTRAL). */
fun emotionKey(entry: JournalEntry): String =
    entry.emotion?.trim()?.uppercase()?.ifEmpty { null } ?: "NEUTRAL"

/** Normalises a search query the same way as [emotionKey]. */
fun normaliseQuery(query: String): String = query.trim().uppercase()

// -------------------------------------------------------------------------
// 1. BINARY SEARCH TREE
// -------------------------------------------------------------------------

/**
 * Binary Search Tree where each node holds one emotion and every entry
 * with that emotion. Smaller emotions (alphabetically) go left, larger go right.
 *
 * Search follows a single path from the root, so it is O(log n) for a
 * balanced tree instead of checking every entry.
 */
class EmotionBinaryTree {

    /** One node of the tree. */
    private class Node(val emotion: String) {
        val entries = mutableListOf<JournalEntry>()
        var left: Node? = null
        var right: Node? = null
    }

    private var root: Node? = null

    /** Number of entries stored in the tree. */
    var size: Int = 0
        private set

    /** Inserts an entry into the correct node (iterative, no recursion limit). */
    fun insert(entry: JournalEntry) {
        val key = emotionKey(entry)
        size++

        if (root == null) {
            root = Node(key).also { it.entries.add(entry) }
            return
        }

        var current = root!!
        while (true) {
            val comparison = key.compareTo(current.emotion)
            when {
                comparison == 0 -> {
                    current.entries.add(entry)
                    return
                }
                comparison < 0 -> {
                    val left = current.left
                    if (left == null) {
                        current.left = Node(key).also { it.entries.add(entry) }
                        return
                    }
                    current = left
                }
                else -> {
                    val right = current.right
                    if (right == null) {
                        current.right = Node(key).also { it.entries.add(entry) }
                        return
                    }
                    current = right
                }
            }
        }
    }

    /** Finds all entries with the given emotion by walking down the tree. */
    fun search(emotion: String): List<JournalEntry> {
        val key = normaliseQuery(emotion)
        var current = root
        while (current != null) {
            val comparison = key.compareTo(current.emotion)
            current = when {
                comparison == 0 -> return current.entries.toList()
                comparison < 0 -> current.left
                else -> current.right
            }
        }
        return emptyList()
    }

    companion object {
        /** Builds a tree from a list of entries. */
        fun from(entries: List<JournalEntry>): EmotionBinaryTree =
            EmotionBinaryTree().apply { entries.forEach { insert(it) } }
    }
}

// -------------------------------------------------------------------------
// 2. HASH-BASED INDEX (HashMap)
// -------------------------------------------------------------------------

/**
 * Hash-based index from emotion to entries. Lookup is O(1) on average.
 */
class EmotionHashMap {

    private val table = HashMap<String, MutableList<JournalEntry>>()

    /** Adds an entry to the bucket for its emotion. */
    fun put(entry: JournalEntry) {
        table.getOrPut(emotionKey(entry)) { mutableListOf() }.add(entry)
    }

    /** Returns all entries with the given emotion. */
    fun get(emotion: String): List<JournalEntry> =
        table[normaliseQuery(emotion)]?.toList() ?: emptyList()

    companion object {
        /** Builds the index from a list of entries. */
        fun from(entries: List<JournalEntry>): EmotionHashMap =
            EmotionHashMap().apply { entries.forEach { put(it) } }
    }
}

// -------------------------------------------------------------------------
// 3. DOUBLY LINKED LIST
// -------------------------------------------------------------------------

/**
 * Doubly Linked List of journal entries. Each node links to the next and
 * previous node. Searching walks the list from head to tail: O(n).
 */
class JournalLinkedList {

    /** One node of the list. */
    private class Node(val entry: JournalEntry) {
        var next: Node? = null
        var prev: Node? = null
    }

    private var head: Node? = null
    private var tail: Node? = null

    /** Number of nodes in the list. */
    var size: Int = 0
        private set

    /** Adds an entry to the end of the list. */
    fun addLast(entry: JournalEntry) {
        val node = Node(entry)
        val oldTail = tail
        if (oldTail == null) {
            head = node
        } else {
            oldTail.next = node
            node.prev = oldTail
        }
        tail = node
        size++
    }

    /** Traverses from head to tail and collects entries with the given emotion. */
    fun searchByEmotion(emotion: String): List<JournalEntry> {
        val key = normaliseQuery(emotion)
        val results = mutableListOf<JournalEntry>()
        var current = head
        while (current != null) {
            if (emotionKey(current.entry) == key) {
                results.add(current.entry)
            }
            current = current.next
        }
        return results
    }

    companion object {
        /** Builds a list from entries, keeping their order. */
        fun from(entries: List<JournalEntry>): JournalLinkedList =
            JournalLinkedList().apply { entries.forEach { addLast(it) } }
    }
}
