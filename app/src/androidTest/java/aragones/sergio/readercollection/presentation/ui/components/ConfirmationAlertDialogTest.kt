/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 28/3/2024
 */

package aragones.sergio.readercollection.presentation.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.ui.components.ConfirmationAlertDialog
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class ConfirmationAlertDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun whenSendTrueToComponent_thenShowDialog() {

        composeTestRule.setContent {
            ConfirmationAlertDialog(
                show = true,
                textId = R.string.profile_logout_confirmation,
                onCancel = {},
                onAccept = {})
        }

        composeTestRule.onNodeWithTag("confirmationAlertDialog").assertExists()
    }

    @Test
    fun whenSendFalseToComponent_thenDoNotShowDialog() {

        composeTestRule.setContent {
            ConfirmationAlertDialog(
                show = false,
                textId = R.string.profile_logout_confirmation,
                onCancel = {}) {}
        }

        composeTestRule.onNodeWithTag("confirmationAlertDialog").assertDoesNotExist()
    }

    @Test
    fun whenShowDialog_thenShowTextAndButtons() {

        val textId = R.string.profile_logout_confirmation
        composeTestRule.setContent {
            ConfirmationAlertDialog(show = true, textId = textId, onCancel = {}) {}
        }

        val text = composeTestRule.activity.getString(textId)
        val acceptText = composeTestRule.activity.getString(R.string.accept)
        val cancelText = composeTestRule.activity.getString(R.string.cancel)
        composeTestRule.onNodeWithText(text).assertExists()
        composeTestRule.onAllNodesWithTag("textButtonAlertDialog").apply {
            onFirst().assertTextContains(cancelText, ignoreCase = true)
            onLast().assertTextContains(acceptText, ignoreCase = true)
        }
    }

    @Test
    fun whenCancelDialog_thenCloseDialog() {

        var isClosed = false
        composeTestRule.setContent {
            ConfirmationAlertDialog(
                show = true,
                textId = R.string.profile_logout_confirmation,
                onCancel = {
                    isClosed = true
                },
                onAccept = {})
        }

        composeTestRule.onAllNodesWithTag("textButtonAlertDialog").onFirst().performClick()
        Assert.assertTrue(isClosed)
    }

    @Test
    fun whenAcceptDialog_thenCloseDialog() {

        var isClosed = false
        composeTestRule.setContent {
            ConfirmationAlertDialog(
                show = true,
                textId = R.string.profile_logout_confirmation,
                onCancel = {},
                onAccept = {
                    isClosed = true
                })
        }

        composeTestRule.onAllNodesWithTag("textButtonAlertDialog").onLast().performClick()
        Assert.assertTrue(isClosed)
    }
}