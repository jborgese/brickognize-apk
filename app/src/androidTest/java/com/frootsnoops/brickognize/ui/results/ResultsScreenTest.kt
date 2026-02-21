package com.frootsnoops.brickognize.ui.results

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.frootsnoops.brickognize.ui.theme.BrickognizeTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResultsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun resultsScreen_displaysTitle() {
        val mockViewModel = createMockViewModel()

        composeTestRule.setContent {
            BrickognizeTheme {
                ResultsScreen(
                    onNavigateBack = {},
                    onNavigateHome = {},
                    onNavigateToScan = {},
                    viewModel = mockViewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Recognition Results").assertExists()
    }

    @Test
    fun resultsScreen_backButton_triggersNavigation() {
        var backClicked = false
        val mockViewModel = createMockViewModel()

        composeTestRule.setContent {
            BrickognizeTheme {
                ResultsScreen(
                    onNavigateBack = { backClicked = true },
                    onNavigateHome = {},
                    onNavigateToScan = {},
                    viewModel = mockViewModel
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assert(backClicked)
    }

    @Test
    fun resultsScreen_homeButton_triggersNavigation() {
        var homeClicked = false
        val mockViewModel = createMockViewModel()

        composeTestRule.setContent {
            BrickognizeTheme {
                ResultsScreen(
                    onNavigateBack = {},
                    onNavigateHome = { homeClicked = true },
                    onNavigateToScan = {},
                    viewModel = mockViewModel
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Home").performClick()

        assert(homeClicked)
    }

    private fun createMockViewModel(): ResultsViewModel {
        val mockViewModel: ResultsViewModel = mockk(relaxed = true)
        every { mockViewModel.uiState } returns MutableStateFlow(ResultsUiState())
        return mockViewModel
    }
}
