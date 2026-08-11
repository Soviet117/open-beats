package com.soviet117.openbeats

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform