/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 28/3/2024
 */

package aragones.sergio.readercollection.presentation.ui.components.dialogs

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.input.KeyboardType
import aragones.sergio.readercollection.presentation.components.TextFieldAlertDialog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.jetbrains.compose.resources.stringResource
import org.junit.Assert
import org.junit.Rule
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.accept
import reader_collection.app.generated.resources.cancel
import reader_collection.app.generated.resources.enter_valid_url

class TextFieldAlertDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun whenSendTrueToComponent_thenShowDialog() {
        composeTestRule.setContent {
            TextFieldAlertDialog(
                titleTextId = Res.string.enter_valid_url,
                type = KeyboardType.Text,
                onCancel = {},
                onAccept = {},
            )
        }

        composeTestRule.onNodeWithTag("textFieldAlertDialog").assertExists()
    }

    @Test
    fun whenSendFalseToComponent_thenDoNotShowDialog() {
        composeTestRule.setContent {
            TextFieldAlertDialog(
                titleTextId = null,
                type = KeyboardType.Text,
                onCancel = {},
                onAccept = {},
            )
        }

        composeTestRule.onNodeWithTag("textFieldAlertDialog").assertDoesNotExist()
    }

    @Test
    fun whenShowDialog_thenShowTextFieldAndButton() {
        lateinit var acceptText: String
        lateinit var cancelText: String
        composeTestRule.setContent {
            acceptText = stringResource(Res.string.accept)
            cancelText = stringResource(Res.string.cancel)
            TextFieldAlertDialog(
                titleTextId = Res.string.enter_valid_url,
                type = KeyboardType.Text,
                onCancel = {},
                onAccept = {},
            )
        }

        composeTestRule.onNodeWithTag("textField").assertExists()
        composeTestRule.onAllNodesWithTag("textButtonAlertDialog").apply {
            onFirst().assertTextContains(cancelText, ignoreCase = true)
            onLast().assertTextContains(acceptText, ignoreCase = true)
        }
    }

    @Test
    fun whenCancelDialog_thenCloseDialog() {
        var isClosed = false
        composeTestRule.setContent {
            TextFieldAlertDialog(
                titleTextId = Res.string.enter_valid_url,
                type = KeyboardType.Text,
                onCancel = {
                    isClosed = true
                },
                onAccept = {},
            )
        }

        composeTestRule.onAllNodesWithTag("textButtonAlertDialog").onFirst().performClick()
        Assert.assertTrue(isClosed)
    }

    @Test
    fun whenAcceptDialog_thenCloseDialogAndReturnText() {
        var isClosed = false
        var text = ""
        composeTestRule.setContent {
            TextFieldAlertDialog(
                titleTextId = Res.string.enter_valid_url,
                type = KeyboardType.Text,
                onCancel = {},
                onAccept = {
                    isClosed = true
                    text = it
                },
            )
        }

        composeTestRule.onNodeWithTag("textField").performTextReplacement("New text to return")
        composeTestRule.onAllNodesWithTag("textButtonAlertDialog").onLast().performClick()
        assertTrue(isClosed)
        assertEquals("New text to return", text)
    }
}