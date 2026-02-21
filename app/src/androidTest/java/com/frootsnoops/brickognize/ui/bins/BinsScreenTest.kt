package com.frootsnoops.brickognize.ui.bins

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.frootsnoops.brickognize.domain.model.BinLocation
import com.frootsnoops.brickognize.ui.theme.BrickognizeTheme
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BinsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun binsScreen_displaysTitle() {
        val mockViewModel = createViewModel(
            BinsUiState(
                bins = listOf(
                    BinWithCount(
                        binLocation = BinLocation(
                            id = 1L,
                            label = "A1",
                            description = "Top shelf",
                            createdAt = 1_000L
                        ),
                        partCount = 3
                    )
                )
            )
        )

        composeTestRule.setContent {
            BrickognizeTheme {
                BinsScreen(
                    onNavigateBack = {},
                    viewModel = mockViewModel
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Bin Locations").assertExists()
    }

    @Test
    fun binsScreen_backButton_exists() {
        val mockViewModel = createViewModel(
            BinsUiState(
                bins = listOf(
                    BinWithCount(
                        binLocation = BinLocation(
                            id = 1L,
                            label = "A1",
                            description = "Top shelf",
                            createdAt = 1_000L
                        ),
                        partCount = 3
                    )
                )
            )
        )

        composeTestRule.setContent {
            BrickognizeTheme {
                BinsScreen(
                    onNavigateBack = {},
                    viewModel = mockViewModel
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").assertExists()
    }

    @Test
    fun binCard_deleteButton_exists() {
        val bin = BinLocation(
            id = 1L,
            label = "A1",
            description = "Top shelf",
            createdAt = 1_000L
        )

        composeTestRule.setContent {
            BrickognizeTheme {
                BinCard(
                    bin = bin,
                    partCount = 2,
                    onClick = {},
                    onDelete = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Delete bin A1")
            .assertExists()
            .assertHasClickAction()
    }

    @Test
    fun binCard_deleteButton_requiresConfirmation() {
        val bin = BinLocation(
            id = 1L,
            label = "A1",
            description = "Top shelf",
            createdAt = 1_000L
        )
        var deleted = false

        composeTestRule.setContent {
            BrickognizeTheme {
                BinCard(
                    bin = bin,
                    partCount = 2,
                    onClick = {},
                    onDelete = { deleted = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Delete bin A1").performClick()
        composeTestRule.onNodeWithText("Delete Bin?").assertExists()
        composeTestRule.onNodeWithText("Delete").performClick()

        assertThat(deleted).isTrue()
    }

    private fun createViewModel(initialState: BinsUiState): BinsViewModel {
        val mockViewModel: BinsViewModel = mockk(relaxed = true)
        every { mockViewModel.uiState } returns MutableStateFlow(initialState)
        every { mockViewModel.clearSelection() } just runs
        every { mockViewModel.clearExportMessage() } just runs
        every { mockViewModel.clearImportMessage() } just runs
        return mockViewModel
    }
}
