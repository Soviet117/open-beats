package com.soviet117.openbeats.data

interface RecentStore {
    suspend fun load(): List<String>
    suspend fun add(songId: String)
}
