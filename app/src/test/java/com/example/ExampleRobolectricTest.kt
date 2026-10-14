package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.ByteFormatter
import com.example.util.PreferencesManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("OptiClean", appName)
  }

  @Test
  fun `test byte formatter output`() {
    val formattedMb = ByteFormatter.formatBytes(524288000L) // 500 MB
    assertEquals("500.0 MB", formattedMb)

    val formattedGb = ByteFormatter.formatBytes(2147483648L) // 2 GB
    assertEquals("2.00 GB", formattedGb)
  }

  @Test
  fun `test preferences manager daily cleanup`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = PreferencesManager(context)
    prefs.isDailyCleanupEnabled = true
    assertTrue(prefs.isDailyCleanupEnabled)
  }
}
