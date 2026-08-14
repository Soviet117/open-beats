package com.soviet117.openbeats.ui.data

fun greetingForHour(hour: Int): String = when (hour) {
    in 6..11 -> "Buenos días"
    in 12..19 -> "Buenas tardes"
    else -> "Buenas noches"
}
