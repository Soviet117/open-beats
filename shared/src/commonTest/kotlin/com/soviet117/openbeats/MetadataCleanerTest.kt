package com.soviet117.openbeats

import com.soviet117.openbeats.ui.data.MetadataCleaner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MetadataCleanerTest {

    @Test
    fun realTagsAreKeptUntouched() {
        val parsed = MetadataCleaner.infer(
            mediaTitle = "Thriller",
            mediaArtist = "Michael Jackson",
            mediaAlbum = "Thriller",
            fileName = "michael_jackson_-_thriller.mp3",
        )
        assertEquals("Thriller", parsed.title)
        assertEquals("Michael Jackson", parsed.artist)
        assertEquals("Thriller", parsed.album)
    }

    @Test
    fun garbageTagsFallbackToFileNameArtistTitle() {
        val parsed = MetadataCleaner.infer(
            mediaTitle = "829374623",
            mediaArtist = "",
            mediaAlbum = "",
            fileName = "Michael Jackson - Thriller.mp3",
        )
        assertEquals("Thriller", parsed.title)
        assertEquals("Michael Jackson", parsed.artist)
        assertEquals("", parsed.album)
    }

    @Test
    fun garbageTagsFallbackToFileNameTrackPrefix() {
        val parsed = MetadataCleaner.infer(
            mediaTitle = null,
            mediaArtist = null,
            mediaAlbum = null,
            fileName = "01 - Song Name.mp3",
        )
        assertEquals("Song Name", parsed.title)
        assertEquals(MetadataCleaner.UNKNOWN_ARTIST, parsed.artist)
    }

    @Test
    fun fileNameWithArtistAlbumAndTrack() {
        val parsed = MetadataCleaner.infer(
            mediaTitle = null,
            mediaArtist = null,
            mediaAlbum = null,
            fileName = "Michael Jackson - Thriller - 01 - Wanna Be Startin' Somethin'.mp3",
        )
        assertEquals("Wanna Be Startin' Somethin'", parsed.title)
        assertEquals("Michael Jackson", parsed.artist)
        assertEquals("Thriller", parsed.album)
    }

    @Test
    fun y2matePrefixAndOfficialVideoSuffixAreStripped() {
        val parsed = MetadataCleaner.infer(
            mediaTitle = null,
            mediaArtist = null,
            mediaAlbum = null,
            fileName = "y2mate.com - Song Name (Official Video).mp3",
        )
        assertEquals("Song Name", parsed.title)
    }

    @Test
    fun bitrateSuffixIsStripped() {
        val parsed = MetadataCleaner.infer(
            mediaTitle = null,
            mediaArtist = null,
            mediaAlbum = null,
            fileName = "Song Name (128 kbps).mp3",
        )
        assertEquals("Song Name", parsed.title)
    }

    @Test
    fun underscoreSeparatedFileNameIsParsed() {
        val parsed = MetadataCleaner.infer(
            mediaTitle = null,
            mediaArtist = null,
            mediaAlbum = null,
            fileName = "Neon_Harbor_-_Midnight_Drive.mp3",
        )
        assertEquals("Midnight Drive", parsed.title)
        assertEquals("Neon Harbor", parsed.artist)
    }

    @Test
    fun pureDigitsFileNameIsKeptAsTitle() {
        val parsed = MetadataCleaner.infer(
            mediaTitle = null,
            mediaArtist = null,
            mediaAlbum = null,
            fileName = "829374623.mp3",
        )
        assertEquals("829374623", parsed.title)
        assertEquals(MetadataCleaner.UNKNOWN_ARTIST, parsed.artist)
    }

    @Test
    fun unknownArtistTagFallsBackToFileNameArtist() {
        val parsed = MetadataCleaner.infer(
            mediaTitle = "Some Song",
            mediaArtist = "<unknown>",
            mediaAlbum = "Unknown Album",
            fileName = "Cool Artist - Some Song.mp3",
        )
        assertEquals("Some Song", parsed.title)
        assertEquals("Cool Artist", parsed.artist)
        assertEquals("", parsed.album)
    }

    @Test
    fun garbageDetection() {
        assertTrue(MetadataCleaner.isGarbage(null))
        assertTrue(MetadataCleaner.isGarbage(""))
        assertTrue(MetadataCleaner.isGarbage("123456"))
        assertTrue(MetadataCleaner.isGarbage("<unknown>"))
        assertTrue(MetadataCleaner.isGarbage("Unknown Artist"))
        assertTrue(MetadataCleaner.isGarbage("track01"))
        assertTrue(MetadataCleaner.isGarbage("j3k2l9x8z4a1b5c2m7n9q"))
        assertFalse(MetadataCleaner.isGarbage("Thriller"))
        assertFalse(MetadataCleaner.isGarbage("Michael Jackson"))
        assertFalse(MetadataCleaner.isGarbage("50 Cent"))
    }

    @Test
    fun normalizeKeyMergesCaseAndPadding() {
        assertEquals("michael jackson", MetadataCleaner.normalizeKey("Michael Jackson"))
        assertEquals("michael jackson", MetadataCleaner.normalizeKey("  michael  jackson "))
        assertEquals("thriller", MetadataCleaner.normalizeKey("Thriller"))
    }
}
