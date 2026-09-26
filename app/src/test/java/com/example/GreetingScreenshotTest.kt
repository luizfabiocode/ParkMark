package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.ParkMarkUiState
import com.example.ui.screens.ParkScreen
import com.example.ui.theme.ParkMarkTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun park_screen_screenshot() {
    composeTestRule.setContent {
      ParkMarkTheme {
        ParkScreen(
          uiState = ParkMarkUiState(),
          onSaveSpot = { _, _ -> },
          onRefreshLocation = {},
          onEnableGpsClick = {},
          onRequestPermissions = {},
          hasLocationPermission = true
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
