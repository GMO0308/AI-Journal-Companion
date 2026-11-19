package com.example.aijournalcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.aijournalcompanion.ui.theme.AIJournalCompanionTheme

/**
 * Entry point of the app. Handles navigation between screens with a simple
 * back stack, so Back returns to the previous screen and Home returns to
 * the main menu.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AIJournalCompanionTheme {
                // Back stack of visited screens; the last item is the current screen
                val backStack = remember { mutableStateListOf(AppScreen.MENU) }
                var showHelp by remember { mutableStateOf(false) }
                val currentScreen = backStack.last()

                /**
                 * Opens a screen. If the screen is already in the back stack,
                 * returns to it instead of adding a duplicate.
                 */
                fun navigateTo(screen: AppScreen) {
                    val existingIndex = backStack.indexOf(screen)
                    if (existingIndex >= 0) {
                        while (backStack.lastIndex > existingIndex) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    } else {
                        backStack.add(screen)
                    }
                }

                /** Returns to the previous screen. */
                fun goBack() {
                    if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                }

                /** Clears the back stack and returns to the main menu. */
                fun goHome() {
                    backStack.clear()
                    backStack.add(AppScreen.MENU)
                }

                // Phone's system Back button follows the same back stack
                BackHandler(enabled = backStack.size > 1) { goBack() }

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        AppTopBar(
                            title = currentScreen.title,
                            onBack = if (backStack.size > 1) ({ goBack() }) else null,
                            onHome = if (currentScreen != AppScreen.MENU) ({ goHome() }) else null,
                            onHelp = { showHelp = true }
                        )
                    }
                ) { innerPadding ->
                    val screenModifier = Modifier.padding(innerPadding)

                    when (currentScreen) {
                        AppScreen.MENU -> MainMenu(
                            onNavigate = { navigateTo(it) },
                            onHelp = { showHelp = true },
                            modifier = screenModifier
                        )

                        AppScreen.JOURNAL -> JournalScreen(
                            onViewHistory = { navigateTo(AppScreen.HISTORY) },
                            modifier = screenModifier
                        )

                        AppScreen.HISTORY -> HistoryScreen(
                            onNewEntry = { navigateTo(AppScreen.JOURNAL) },
                            modifier = screenModifier
                        )

                        AppScreen.INSIGHTS -> InsightsScreen(
                            onNewEntry = { navigateTo(AppScreen.JOURNAL) },
                            modifier = screenModifier
                        )
                    }
                }

                // Help popup (WebView with local help.html), available on every screen
                if (showHelp) {
                    HelpDialog(onDismiss = { showHelp = false })
                }
            }
        }
    }
}
