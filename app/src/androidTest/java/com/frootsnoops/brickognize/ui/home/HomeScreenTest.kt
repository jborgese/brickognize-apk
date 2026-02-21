package com.frootsnoops.brickognize.ui.home

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.frootsnoops.brickognize.ui.theme.BrickognizeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysAllMainElements() {
        // Given
        var scanClicked = false
        var historyClicked = false
        var binsClicked = false

        // When
        composeTestRule.setContent {
            BrickognizeTheme {
                HomeScreen(
                    onNavigateToScan = { scanClicked = true },
                    onNavigateToHistory = { historyClicked = true },
                    onNavigateToBins = { binsClicked = true }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Brickognize").assertExists()
        composeTestRule.onNodeWithText("Scan Brick").assertExists()
        composeTestRule.onNodeWithText("Scan History").assertExists()
        composeTestRule.onNodeWithText("Bin Locations").assertExists()
        composeTestRule.onNodeWithText("How to use").assertExists()
    }

    @Test
    fun scanButton_clickTriggersNavigation() {
        // Given
        var scanClicked = false

        composeTestRule.setContent {
            BrickognizeTheme {
                HomeScreen(
                    onNavigateToScan = { scanClicked = true },
                    onNavigateToHistory = {},
                    onNavigateToBins = {}
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Scan Brick").performClick()

        // Then
        assert(scanClicked)
    }

    @Test
    fun scanHistoryCard_clickTriggersNavigation() {
        // Given
        var historyClicked = false

        composeTestRule.setContent {
            BrickognizeTheme {
                HomeScreen(
                    onNavigateToScan = {},
                    onNavigateToHistory = { historyClicked = true },
                    onNavigateToBins = {}
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Scan History").performClick()

        // Then
        assert(historyClicked)
    }

    @Test
    fun binLocationsCard_clickTriggersNavigation() {
        // Given
        var binsClicked = false

        composeTestRule.setContent {
            BrickognizeTheme {
                HomeScreen(
                    onNavigateToScan = {},
                    onNavigateToHistory = {},
                    onNavigateToBins = { binsClicked = true }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Bin Locations").performClick()

        // Then
        assert(binsClicked)
    }

    @Test
    fun homeScreen_displaysInstructionsCorrectly() {
        composeTestRule.setContent {
            BrickognizeTheme {
                HomeScreen(
                    onNavigateToScan = {},
                    onNavigateToHistory = {},
                    onNavigateToBins = {}
                )
            }
        }

        // Verify instructions text
        composeTestRule.onNodeWithText("How to use").assertExists()
        composeTestRule.onNode(
            hasText("1. Tap 'Scan Brick' to take a photo", substring = true)
        ).assertExists()
    }

    @Test
    fun homeScreen_hasCorrectContentDescription() {
        composeTestRule.setContent {
            BrickognizeTheme {
                HomeScreen(
                    onNavigateToScan = {},
                    onNavigateToHistory = {},
                    onNavigateToBins = {}
                )
            }
        }

        // Verify accessibility
        composeTestRule.onNodeWithContentDescription("Navigate").assertExists()
    }
}
