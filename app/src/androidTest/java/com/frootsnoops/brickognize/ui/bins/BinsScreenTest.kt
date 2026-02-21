package com.frootsnoops.brickognize.ui.bins

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.frootsnoops.brickognize.ui.theme.BrickognizeTheme
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BinsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun binsScreen_displaysTitle() {
        // Given - mock ViewModel
        val mockViewModel: BinsViewModel = mockk(relaxed = true)

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
        val mockViewModel: BinsViewModel = mockk(relaxed = true)

        composeTestRule.setContent {
            BrickognizeTheme {
                BinsScreen(
                    onNavigateBack = {},
                    viewModel = mockViewModel
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Navigate back").assertExists()
    }
}
