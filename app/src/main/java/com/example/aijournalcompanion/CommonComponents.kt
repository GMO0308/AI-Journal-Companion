package com.example.aijournalcompanion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/*
 * Reusable UI pieces shared by every screen, so the app looks consistent.
 */

/**
 * Top bar shown on every screen.
 *  - Back arrow (left): returns to the previous screen (hidden on the main menu)
 *  - Home (right): jumps straight back to the main menu
 *  - Info (right): opens the Help guide
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)?,
    onHome: (() -> Unit)?,
    onHelp: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            if (onHome != null) {
                IconButton(onClick = onHome) {
                    Icon(Icons.Filled.Home, contentDescription = "Home")
                }
            }
            IconButton(onClick = onHelp) {
                Icon(Icons.Filled.Info, contentDescription = "Help")
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

/** Large filled green button for the main action on a screen. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        ButtonContent(text, icon)
    }
}

/** Outlined button for secondary actions (Clear, Back, Show Chart...). */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(50.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        ButtonContent(text, icon)
    }
}

/** Shared icon + label layout used inside the buttons above. */
@Composable
private fun ButtonContent(text: String, icon: ImageVector?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Returns the emoji for an emotion. */
fun emotionEmoji(emotion: String?): String {
    return when (emotion?.trim()?.uppercase()) {
        "JOY" -> "😊"
        "SADNESS" -> "😢"
        "ANGER" -> "😡"
        "FEAR" -> "😨"
        "SURPRISE" -> "😲"
        else -> "😐"
    }
}

/** Returns "EMOTION emoji", e.g. "JOY 😊" (missing emotion shows NEUTRAL). */
fun getEmotionEmoji(emotion: String?): String {
    val label = emotion?.trim()?.uppercase()?.ifEmpty { null } ?: "NEUTRAL"
    return "$label ${emotionEmoji(emotion)}"
}
