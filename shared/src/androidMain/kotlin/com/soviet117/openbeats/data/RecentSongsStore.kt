package com.soviet117.openbeats.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecentSongsStore(context: Context) : RecentStore {

    private val helper = RecentDbHelper(context)

    override suspend fun load(): List<String> = withContext(Dispatchers.IO) {
        val ids = mutableListOf<String>()
        helper.readableDatabase.query(
            TABLE_RECENTS,
            arrayOf(COL_SONG_ID),
            null,
            null,
            null,
            null,
            "$COL_PLAYED_AT DESC",
        ).use { cursor ->
            while (cursor.moveToNext()) {
                ids += cursor.getString(0)
            }
        }
        ids
    }

    override suspend fun add(songId: String) = withContext(Dispatchers.IO) {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            val values = ContentValues().apply {
                put(COL_SONG_ID, songId)
                put(COL_PLAYED_AT, System.currentTimeMillis())
            }
            db.insertWithOnConflict(TABLE_RECENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            db.execSQL(
                "DELETE FROM $TABLE_RECENTS WHERE $COL_SONG_ID NOT IN (" +
                    "SELECT $COL_SONG_ID FROM $TABLE_RECENTS ORDER BY $COL_PLAYED_AT DESC LIMIT $MAX_RECENTS)",
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private class RecentDbHelper(context: Context) :
        SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE $TABLE_RECENTS (" +
                    "$COL_SONG_ID TEXT PRIMARY KEY, " +
                    "$COL_PLAYED_AT INTEGER NOT NULL)",
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_RECENTS")
            onCreate(db)
        }
    }

    private companion object {
        const val DB_NAME = "open_beats_recents.db"
        const val DB_VERSION = 1
        const val TABLE_RECENTS = "recents"
        const val COL_SONG_ID = "song_id"
        const val COL_PLAYED_AT = "played_at"
        const val MAX_RECENTS = 12
    }
}
