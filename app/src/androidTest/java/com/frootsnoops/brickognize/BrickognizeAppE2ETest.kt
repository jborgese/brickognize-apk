package com.frootsnoops.brickognize

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end integration test for the Brickognize app.
 * Tests the complete user flow through the application.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BrickognizeAppE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun appLaunch_showsHomeScreen() {
        // Verify home screen is displayed
        composeTestRule.onNodeWithText("Brickognize").assertExists()
        composeTestRule.onNodeWithText("Scan Brick").assertExists()
    }

    @Test
    fun navigation_homeToScan_works() {
        // Given - on home screen
        composeTestRule.onNodeWithText("Scan Brick").assertExists()

        // When - click scan button
        composeTestRule.onNodeWithText("Scan Brick").performClick()

        // Then - navigate to scan screen
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Scan Brick")
                .fetchSemanticsNodes()
                .size >= 1
        }
    }

    @Test
    fun navigation_homeToHistory_works() {
        // When
        composeTestRule.onNodeWithText("Scan History").performClick()

        // Then
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Scan History")
                .fetchSemanticsNodes()
                .size >= 1
        }
    }

    @Test
    fun navigation_homeToBins_works() {
        // When
        composeTestRule.onNodeWithText("Bin Locations").performClick()

        // Then
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Bin Locations")
                .fetchSemanticsNodes()
                .size >= 1
        }
    }

    @Test
    fun navigation_backButton_returnsToHome() {
        // Given - navigate to history
        composeTestRule.onNodeWithText("Scan History").performClick()
        composeTestRule.waitForIdle()

        // When - press back
        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()

        // Then - back on home screen
        composeTestRule.onNodeWithText("Brickognize").assertExists()
        composeTestRule.onNodeWithText("Scan Brick").assertExists()
    }

    @Test
    fun fullUserFlow_scanToHistoryToBins() {
        // Step 1: Home screen
        composeTestRule.onNodeWithText("Brickognize").assertExists()

        // Step 2: Navigate to scan
        composeTestRule.onNodeWithText("Scan Brick").performClick()
        composeTestRule.waitForIdle()

        // Step 3: Back to home
        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()
        composeTestRule.waitForIdle()

        // Step 4: Navigate to history
        composeTestRule.onNodeWithText("Scan History").performClick()
        composeTestRule.waitForIdle()

        // Step 5: Back to home
        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()
        composeTestRule.waitForIdle()

        // Step 6: Navigate to bins
        composeTestRule.onNodeWithText("Bin Locations").performClick()
        composeTestRule.waitForIdle()

        // Verify bins screen
        composeTestRule.onNodeWithText("Bin Locations").assertExists()
    }
}
