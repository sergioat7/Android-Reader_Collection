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
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import aragones.sergio.readercollection.presentation.components.ConfirmationAlertDialog
import kotlin.test.Test
import kotlin.test.assertTrue
import org.jetbrains.compose.resources.stringResource
import org.junit.Rule
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.accept
import reader_collection.app.generated.resources.cancel
import reader_collection.app.generated.resources.profile_logout_confirmation

class ConfirmationAlertDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun whenSendTextToComponent_thenShowDialog() {
        composeTestRule.setContent {
            ConfirmationAlertDialog(
                textId = Res.string.profile_logout_confirmation,
                onCancel = {},
                onAccept = {},
            )
        }

        composeTestRule.onNodeWithTag("confirmationAlertDialog").assertExists()
    }

    @Test
    fun whenSendNullToComponent_thenDoNotShowDialog() {
        composeTestRule.setContent {
            ConfirmationAlertDialog(
                textId = null,
                onCancel = {},
            ) {}
        }

        composeTestRule.onNodeWithTag("confirmationAlertDialog").assertDoesNotExist()
    }

    @Test
    fun whenShowDialog_thenShowTextAndButtons() {
        val textId = Res.string.profile_logout_confirmation
        lateinit var text: String
        lateinit var acceptText: String
        lateinit var cancelText: String
        composeTestRule.setContent {
            text = stringResource(textId)
            acceptText = stringResource(Res.string.accept)
            cancelText = stringResource(Res.string.cancel)
            ConfirmationAlertDialog(textId = textId, onCancel = {}) {}
        }

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
                textId = Res.string.profile_logout_confirmation,
                onCancel = {
                    isClosed = true
                },
                onAccept = {},
            )
        }

        composeTestRule.onAllNodesWithTag("textButtonAlertDialog").onFirst().performClick()
        assertTrue(isClosed)
    }

    @Test
    fun whenAcceptDialog_thenCloseDialog() {
        var isClosed = false
        composeTestRule.setContent {
            ConfirmationAlertDialog(
                textId = Res.string.profile_logout_confirmation,
                onCancel = {},
                onAccept = {
                    isClosed = true
                },
            )
        }

        composeTestRule.onAllNodesWithTag("textButtonAlertDialog").onLast().performClick()
        assertTrue(isClosed)
    }
}