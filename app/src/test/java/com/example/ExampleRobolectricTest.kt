package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.understanding.OfflineUnderstandingEngine
import com.example.domain.model.GlanceCategory
import com.example.domain.model.GlancePriority
import com.example.domain.model.SourceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("GlanceFlow", appName)
    }

    @Test
    fun `test offline understanding engine on blackboard text`() {
        val blackboardText = """
            AI Assignment
            Submit: Friday
            Implement CNN classifier
            Dataset: CIFAR-10
            Bring printed report
        """.trimIndent()

        val parsed = OfflineUnderstandingEngine.parse(blackboardText, SourceType.CAMERA)
        assertEquals(GlanceCategory.ASSIGNMENT, parsed.category)
        assertEquals(GlancePriority.HIGH, parsed.priority)
        assertTrue(parsed.tasks.any { it.contains("Implement CNN classifier", ignoreCase = true) })
        assertTrue(parsed.tasks.any { it.contains("Bring printed report", ignoreCase = true) })
        assertTrue(parsed.entities.any { it.contains("CIFAR-10", ignoreCase = true) })
    }
}
