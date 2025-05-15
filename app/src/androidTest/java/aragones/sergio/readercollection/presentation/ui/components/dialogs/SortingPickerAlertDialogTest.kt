/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/12/2024
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
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.components.SortingPickerAlertDialog
import aragones.sergio.readercollection.presentation.components.UiSortingPickerState
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class SortingPickerAlertDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun whenSendTrueToComponent_thenShowDialog() {
        composeTestRule.setContent {
            SortingPickerAlertDialog(
                state = UiSortingPickerState(
                    show = true,
                    sortParam = "readingDate",
                    isSortDescending = false,
                ),
                onCancel = {},
                onAccept = { _, _ -> },
            )
        }

        composeTestRule.onNodeWithTag("sortingPickerAlertDialog").assertExists()
    }

    @Test
    fun whenSendFalseToComponent_thenDoNotShowDialog() {
        composeTestRule.setContent {
            SortingPickerAlertDialog(
                state = UiSortingPickerState(
                    show = false,
                    sortParam = "readingDate",
                    isSortDescending = false,
                ),
                onCancel = {},
                onAccept = { _, _ -> },
            )
        }

        composeTestRule.onNodeWithTag("sortingPickerAlertDialog").assertDoesNotExist()
    }

    @Test
    fun whenShowDialog_thenShowTextAndButtonsAndSelectedOptions() {
        val sortParam = "readingDate"
        composeTestRule.setContent {
            SortingPickerAlertDialog(
                state = UiSortingPickerState(
                    show = true,
                    sortParam = sortParam,
                    isSortDescending = false,
                ),
                onCancel = {},
                onAccept = { _, _ -> },
            )
        }

        val title = composeTestRule.activity.getString(R.string.order_by)
        val acceptText = composeTestRule.activity.getString(R.string.accept)
        val cancelText = composeTestRule.activity.getString(R.string.cancel)
        composeTestRule.onNodeWithText(title).assertExists()
        composeTestRule.onAllNodesWithTag("textButtonAlertDialog").apply {
            onFirst().assertTextContains(cancelText, ignoreCase = true)
            onLast().assertTextContains(acceptText, ignoreCase = true)
        }
        val context = composeTestRule.activity
        val sortParamIndexSelected =
            context.resources.getStringArray(R.array.sorting_param_keys).indexOf(sortParam)
        val sortParamSelected =
            context.resources.getStringArray(R.array.sorting_param_values)[sortParamIndexSelected]
        val sortOrderSelected =
            context.resources.getStringArray(R.array.sorting_order_values).first()
        composeTestRule.onNodeWithText(sortParamSelected).assertExists()
        composeTestRule.onNodeWithText(sortOrderSelected).assertExists()
    }

    @Test
    fun whenCancelDialog_thenCloseDialog() {
        var isClosed = false
        composeTestRule.setContent {
            SortingPickerAlertDialog(
                state = UiSortingPickerState(
                    show = true,
                    sortParam = "readingDate",
                    isSortDescending = false,
                ),
                onCancel = {
                    isClosed = true
                },
                onAccept = { _, _ -> },
            )
        }

        composeTestRule.onAllNodesWithTag("textButtonAlertDialog").onFirst().performClick()
        Assert.assertTrue(isClosed)
    }

    @Test
    fun whenAcceptDialog_thenCloseDialogAndSetSelectedOptions() {
        var isClosed = false
        var sortParam: String? = null
        var isSortDescending = false
        composeTestRule.setContent {
            SortingPickerAlertDialog(
                state = UiSortingPickerState(
                    show = true,
                    sortParam = "readingDate",
                    isSortDescending = true,
                ),
                onCancel = {},
                onAccept = { newSortParam, newIsSortDescending ->
                    isClosed = true
                    sortParam = newSortParam
                    isSortDescending = newIsSortDescending
                },
            )
        }

        composeTestRule.onAllNodesWithTag("textButtonAlertDialog").onLast().performClick()
        Assert.assertTrue(isClosed)
        Assert.assertEquals("readingDate", sortParam)
        Assert.assertEquals(true, isSortDescending)
    }
}