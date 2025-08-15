
package dev.notifyninja

import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun weeklyInterval() {
        val weekMs = 7L*24*60*60*1000
        assertEquals(604800000L, weekMs)
    }
}
