package com.frootsnoops.brickognize.ui.history

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.frootsnoops.brickognize.ui.theme.BrickognizeTheme
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun historyScreen_displaysTitle() {
        val mockViewModel: HistoryViewModel = mockk(relaxed = true)

        composeTestRule.setContent {
            BrickognizeTheme {
                HistoryScreen(
                    onNavigateBack = {},
                    viewModel = mockViewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Scan History").assertExists()
    }

    @Test
    fun historyScreen_backButton_triggersNavigation() {
        var backClicked = false
        val mockViewModel: HistoryViewModel = mockk(relaxed = true)

        composeTestRule.setContent {
            BrickognizeTheme {
                HistoryScreen(
                    onNavigateBack = { backClicked = true },
                    viewModel = mockViewModel
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()

        assert(backClicked)
    }
}
