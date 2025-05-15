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
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.components.TextFieldAlertDialog
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class TextFieldAlertDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun whenSendTrueToComponent_thenShowDialog() {
        composeTestRule.setContent {
            TextFieldAlertDialog(
                show = true,
                titleTextId = R.string.enter_valid_url,
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
                show = false,
                titleTextId = R.string.enter_valid_url,
                type = KeyboardType.Text,
                onCancel = {},
                onAccept = {},
            )
        }

        composeTestRule.onNodeWithTag("textFieldAlertDialog").assertDoesNotExist()
    }

    @Test
    fun whenShowDialog_thenShowTextFieldAndButton() {
        composeTestRule.setContent {
            TextFieldAlertDialog(
                show = true,
                titleTextId = R.string.enter_valid_url,
                type = KeyboardType.Text,
                onCancel = {},
                onAccept = {},
            )
        }

        val acceptText = composeTestRule.activity.getString(R.string.accept)
        val cancelText = composeTestRule.activity.getString(R.string.cancel)
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
                show = true,
                titleTextId = R.string.enter_valid_url,
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
                show = true,
                titleTextId = R.string.enter_valid_url,
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
        Assert.assertTrue(isClosed)
        Assert.assertEquals("New text to return", text)
    }
}