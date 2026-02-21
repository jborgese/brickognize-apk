package com.frootsnoops.brickognize.ui.bins

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.pressBack
import com.frootsnoops.brickognize.MainActivity
import com.frootsnoops.brickognize.data.local.BrickDatabase
import com.frootsnoops.brickognize.data.local.entity.BinLocationEntity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BinsBackNavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var database: BrickDatabase

    private val firstBinLabel = "BIN-001"
    private val targetBinLabel = "BIN-020"

    @Before
    fun setup() {
        hiltRule.inject()

        runBlocking {
            withContext(Dispatchers.IO) {
                database.clearAllTables()
                repeat(25) { index ->
                    val label = "BIN-${(index + 1).toString().padStart(3, '0')}"
                    database.binLocationDao().insertBinLocation(
                        BinLocationEntity(
                            label = label,
                            description = "Seeded bin $label"
                        )
                    )
                }
            }
        }
    }

    @Test
    fun systemBackFromBinDetails_returnsToBinsListAndKeepsScrollPosition() {
        composeTestRule.onNodeWithText("Bin Locations").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText(firstBinLabel).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(targetBinLabel))
        composeTestRule.onNodeWithText(targetBinLabel).assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText(targetBinLabel).assertExists()

        pressBack()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Sort by:").assertExists()
        composeTestRule.onNodeWithText("Brickognize").assertDoesNotExist()
        composeTestRule.onNodeWithText(targetBinLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText(firstBinLabel).assertDoesNotExist()
    }
}
