package com.soviet117.openbeats

import com.soviet117.openbeats.ui.data.greetingForHour
import kotlin.test.Test
import kotlin.test.assertEquals

class GreetingTest {

    @Test
    fun greetingIsMorningBetweenSixAndEleven() {
        assertEquals("Buenos días", greetingForHour(6))
        assertEquals("Buenos días", greetingForHour(11))
    }

    @Test
    fun greetingIsAfternoonBetweenTwelveAndNineteen() {
        assertEquals("Buenas tardes", greetingForHour(12))
        assertEquals("Buenas tardes", greetingForHour(19))
    }

    @Test
    fun greetingIsNightOtherwise() {
        assertEquals("Buenas noches", greetingForHour(20))
        assertEquals("Buenas noches", greetingForHour(0))
        assertEquals("Buenas noches", greetingForHour(5))
    }
}
