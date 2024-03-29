/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 28/3/2024
 */

package aragones.sergio.readercollection.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import aragones.sergio.readercollection.R
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class InformationAlertDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun whenSendTrueToComponent_thenShowDialog() {

        composeTestRule.setContent {
            InformationAlertDialog(show = true, text = "") {}
        }

        composeTestRule.onNodeWithTag("informationAlertDialog").assertExists()
    }

    @Test
    fun whenSendFalseToComponent_thenDoNotShowDialog() {

        composeTestRule.setContent {
            InformationAlertDialog(show = false, text = "") {}
        }

        composeTestRule.onNodeWithTag("informationAlertDialog").assertDoesNotExist()
    }

    @Test
    fun whenShowDialog_thenShowTextAndButton() {

        val text = "Information text"
        composeTestRule.setContent {
            InformationAlertDialog(show = true, text = text) {}
        }

        val acceptText = composeTestRule.activity.getString(R.string.accept)
        composeTestRule.onNodeWithText(text).assertExists()
        composeTestRule.onNodeWithTag("textButtonAlertDialog")
            .assertTextContains(acceptText, ignoreCase = true)
    }

    @Test
    fun whenAcceptDialog_thenCloseDialog() {

        var isClosed = false
        composeTestRule.setContent {
            InformationAlertDialog(show = true, text = "") {
                isClosed = true
            }
        }

        composeTestRule.onNodeWithTag("textButtonAlertDialog").performClick()
        Assert.assertTrue(isClosed)
    }
}