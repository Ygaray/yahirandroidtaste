package io.github.ygaray.yahirandroidtaste.component

import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.security.MessageDigest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * POL-03: [purgePlaceholderTiles] against a real SQLite file shaped like osmdroid's `SqlTileWriter`
 * cache (`tiles (key INTEGER, provider TEXT, tile BLOB, expires INTEGER, PRIMARY KEY (key,
 * provider))`), loaded with the real blocked placeholder tile fetched from the OSM server.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlaceMapTilePurgeTest {

    @get:Rule val tmp = TemporaryFolder()

    private val placeholder: ByteArray by lazy {
        checkNotNull(javaClass.classLoader!!.getResourceAsStream("osm-blocked-placeholder.png")) {
            "fixture osm-blocked-placeholder.png missing"
        }.use { it.readBytes() }
    }

    private fun newDb(name: String = "cache.db", createTable: Boolean = true): File {
        val file = File(tmp.root, name)
        val db = SQLiteDatabase.openOrCreateDatabase(file, null)
        try {
            if (createTable) db.execSQL(TILES_DDL)
        } finally {
            db.close()
        }
        return file
    }

    private fun insert(file: File, key: Long, provider: String, tile: ByteArray) {
        val db = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READWRITE)
        try {
            db.execSQL(
                "INSERT INTO tiles (key, provider, tile, expires) VALUES (?, ?, ?, ?)",
                arrayOf<Any>(key, provider, tile, 4102444800000L),
            )
        } finally {
            db.close()
        }
    }

    private fun keys(file: File): List<Long> {
        val db = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY)
        try {
            db.rawQuery("SELECT key FROM tiles ORDER BY key", null).use { c ->
                val out = mutableListOf<Long>()
                while (c.moveToNext()) out += c.getLong(0)
                return out
            }
        } finally {
            db.close()
        }
    }

    private fun md5Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("MD5").digest(bytes).joinToString("") { "%02x".format(it) }

    private fun realTile(): ByteArray = ByteArray(4472) { (it * 31 + 7).toByte() }

    @Test
    fun placeholderFixture_matchesThePinnedSizeAndMd5() {
        assertEquals(PLACEHOLDER_TILE_SIZE, placeholder.size)
        assertEquals(PLACEHOLDER_TILE_MD5, md5Hex(placeholder))
    }

    @Test
    fun purge_deletesOnlyTheFingerprintMatchedRow_andReturnsOne() {
        val db = newDb()
        insert(db, 1, "Mapnik", placeholder)
        insert(db, 2, "Mapnik", ByteArray(PLACEHOLDER_TILE_SIZE) { 0x5A })
        insert(db, 3, "Mapnik", realTile())
        assertEquals(1, purgePlaceholderTiles(db))
        assertEquals(listOf(2L, 3L), keys(db))
    }

    @Test
    fun manyPlaceholderRows_areAllDeleted_inOnePass_andRealTilesSurvive() {
        val db = newDb()
        val conn = SQLiteDatabase.openDatabase(db.path, null, SQLiteDatabase.OPEN_READWRITE)
        try {
            conn.beginTransaction()
            for (key in 1L..600L) {
                val tile = if (key % 100L == 0L) realTile() else placeholder
                conn.execSQL(
                    "INSERT INTO tiles (key, provider, tile, expires) VALUES (?, ?, ?, ?)",
                    arrayOf<Any>(key, "Mapnik", tile, 4102444800000L),
                )
            }
            conn.setTransactionSuccessful()
            conn.endTransaction()
        } finally {
            conn.close()
        }
        assertEquals(594, purgePlaceholderTiles(db))
        assertEquals(listOf(100L, 200L, 300L, 400L, 500L, 600L), keys(db))
    }

    @Test
    fun sizeOnlyMatch_isKept() {
        val db = newDb()
        insert(db, 1, "Mapnik", ByteArray(PLACEHOLDER_TILE_SIZE))
        assertEquals(0, purgePlaceholderTiles(db))
        assertEquals(listOf(1L), keys(db))
    }

    @Test
    fun realTile_isKept() {
        val db = newDb()
        insert(db, 1, "Mapnik", realTile())
        assertEquals(0, purgePlaceholderTiles(db))
        assertEquals(listOf(1L), keys(db))
    }

    @Test
    fun secondRun_returnsZero() {
        val db = newDb()
        insert(db, 1, "Mapnik", placeholder)
        assertEquals(1, purgePlaceholderTiles(db))
        assertEquals(0, purgePlaceholderTiles(db))
    }

    @Test
    fun missingFile_isANoOp_andIsNotCreated() {
        val missing = File(tmp.root, "absent/cache.db")
        assertEquals(0, purgePlaceholderTiles(missing))
        assertFalse(missing.exists())
        assertFalse(missing.parentFile!!.exists())
    }

    @Test
    fun garbageFile_returnsMinusOne_withoutThrowing() {
        val file = File(tmp.root, "garbage.db")
        val bytes = ByteArray(4096) { (it % 251).toByte() }
        file.writeBytes(bytes)
        assertEquals(-1, purgePlaceholderTiles(file))
        assertArrayEquals(bytes, file.readBytes())
    }

    @Test
    fun databaseWithoutTilesTable_returnsMinusOne() {
        val db = newDb(createTable = false)
        assertEquals(-1, purgePlaceholderTiles(db))
    }

    @Test
    fun emptyTilesTable_returnsZero() {
        val db = newDb()
        assertEquals(0, purgePlaceholderTiles(db))
    }

    @Test
    fun otherProviderRows_withTheSameBytes_areAlsoPurged() {
        val db = newDb()
        insert(db, 1, "Mapnik", placeholder)
        insert(db, 1, "OpenTopo", placeholder)
        assertEquals(2, purgePlaceholderTiles(db))
        assertTrue(keys(db).isEmpty())
    }

    @Test
    fun providerWithAnApostrophe_isDeletedSafely() {
        val db = newDb()
        insert(db, 1, "O'Brien'; DROP TABLE tiles; --", placeholder)
        insert(db, 2, "Mapnik", realTile())
        assertEquals(1, purgePlaceholderTiles(db))
        assertEquals(listOf(2L), keys(db))
    }

    private companion object {
        const val TILES_DDL =
            "CREATE TABLE IF NOT EXISTS tiles (key INTEGER , provider TEXT, tile BLOB, " +
                "expires INTEGER, PRIMARY KEY (key, provider));"
    }
}
