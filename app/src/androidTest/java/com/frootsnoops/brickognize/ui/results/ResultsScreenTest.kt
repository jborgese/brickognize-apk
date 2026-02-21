package com.frootsnoops.brickognize.ui.results

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.frootsnoops.brickognize.ui.theme.BrickognizeTheme
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResultsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun resultsScreen_displaysTitle() {
        val mockViewModel: ResultsViewModel = mockk(relaxed = true)

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
        val mockViewModel: ResultsViewModel = mockk(relaxed = true)

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

        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()

        assert(backClicked)
    }
}
