package com.frootsnoops.brickognize.ui.scan

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.frootsnoops.brickognize.ui.theme.BrickognizeTheme
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScanScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun scanScreen_displaysTitle() {
        val mockViewModel: ScanViewModel = mockk(relaxed = true)

        composeTestRule.setContent {
            BrickognizeTheme {
                ScanScreen(
                    onNavigateBack = {},
                    onNavigateToResults = {},
                    viewModel = mockViewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Scan Brick").assertExists()
    }

    @Test
    fun scanScreen_backButton_triggersNavigation() {
        var backClicked = false
        val mockViewModel: ScanViewModel = mockk(relaxed = true)

        composeTestRule.setContent {
            BrickognizeTheme {
                ScanScreen(
                    onNavigateBack = { backClicked = true },
                    onNavigateToResults = {},
                    viewModel = mockViewModel
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()

        assert(backClicked)
    }
}
