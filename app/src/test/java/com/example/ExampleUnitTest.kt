package com.example

import com.example.location.LocationClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testDistanceFormatting() {
    assertEquals("45 m", LocationClient.formatDistance(45f))
    assertEquals("250 m", LocationClient.formatDistance(250f))
    assertEquals("1.2 km", LocationClient.formatDistance(1200f))
  }

  @Test
  fun testWalkingTimeFormatting() {
    assertEquals("< 1 min a pé", LocationClient.formatWalkingTime(40f))
    assertEquals("~3 min a pé", LocationClient.formatWalkingTime(240f))
    assertEquals("~10 min a pé", LocationClient.formatWalkingTime(800f))
  }
}
