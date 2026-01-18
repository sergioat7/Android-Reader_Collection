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
import aragones.sergio.readercollection.presentation.components.SortingPickerAlertDialog
import aragones.sergio.readercollection.presentation.components.UiSortingPickerState
import kotlin.test.Test
import kotlin.test.assertEquals
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.junit.Assert
import org.junit.Rule
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.accept
import reader_collection.app.generated.resources.cancel
import reader_collection.app.generated.resources.order_by
import reader_collection.app.generated.resources.sorting_order_values
import reader_collection.app.generated.resources.sorting_param_keys
import reader_collection.app.generated.resources.sorting_param_values

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
        lateinit var title: String
        lateinit var acceptText: String
        lateinit var cancelText: String
        var sortParamIndexSelected = -1
        lateinit var sortParamSelected: String
        lateinit var sortOrderSelected: String
        composeTestRule.setContent {
            title = stringResource(Res.string.order_by)
            acceptText = stringResource(Res.string.accept)
            cancelText = stringResource(Res.string.cancel)
            sortParamIndexSelected = stringArrayResource(
                Res.array.sorting_param_keys,
            ).indexOf(sortParam)
            sortParamSelected = stringArrayResource(
                Res.array.sorting_param_values,
            )[sortParamIndexSelected]
            sortOrderSelected = stringArrayResource(Res.array.sorting_order_values).first()
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

        composeTestRule.onNodeWithText(title).assertExists()
        composeTestRule.onAllNodesWithTag("textButtonAlertDialog").apply {
            onFirst().assertTextContains(cancelText, ignoreCase = true)
            onLast().assertTextContains(acceptText, ignoreCase = true)
        }
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
        assertEquals("readingDate", sortParam)
        assertEquals(true, isSortDescending)
    }
}