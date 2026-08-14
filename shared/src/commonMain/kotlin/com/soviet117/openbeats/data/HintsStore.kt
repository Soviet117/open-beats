package com.soviet117.openbeats.data

interface HintsStore {
    suspend fun genreTipSeen(): Boolean
    suspend fun markGenreTipSeen()
}
