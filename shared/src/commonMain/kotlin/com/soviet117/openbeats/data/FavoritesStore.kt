package com.soviet117.openbeats.data

interface FavoritesStore {
    suspend fun load(): Set<String>
    suspend fun save(ids: Set<String>)
}
