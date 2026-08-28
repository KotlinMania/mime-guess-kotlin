// port-lint: tests lib.rs
package io.github.kotlinmania.mimeguess

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MimeGuessTest {
    @Test
    fun checkTypeBounds() {
        assertTypeBounds()
    }

    @Test
    fun assertTypeBounds() {
        val guess = fromExt("gif")
        assertNotNull(guess)
        val iter = guess.iter()
        assertNotNull(iter)
        val rawIter = guess.iterRaw()
        assertNotNull(rawIter)
    }

    @Test
    fun testMimeTypeGuessing() {
        assertEquals("image/gif", fromExt("gif").firstOrOctetStream())
        assertEquals("text/plain", fromExt("TXT").firstOrOctetStream())
        assertEquals("application/octet-stream", fromExt("blahblah").firstOrOctetStream())

        assertEquals("image/gif", fromPath("/path/to/file.gif").firstOrOctetStream())
        assertEquals("image/gif", fromPath("C:\\path\\to\\file.gif").firstOrOctetStream())
    }

    @Test
    fun testMimeTypeGuessingOpt() {
        assertEquals("image/gif", fromExt("gif").first())
        assertEquals("text/plain", fromExt("TXT").first())
        assertNull(fromExt("blahblah").first())

        assertEquals("image/gif", fromPath("/path/to/file.gif").first())
        assertNull(fromPath("/path/to/file").first())
    }

    @Test
    fun testAreMimeTypesParseable() {
        for ((_, mimes) in MIME_TYPES) {
            for (s in mimes) {
                expectMime(s)
            }
        }
    }

    @Test
    fun testAreExtensionsAscii() {
        for ((ext, _) in MIME_TYPES) {
            assertTrue(ext.all { it.code < 128 }, "Extension not ASCII: \"$ext\"")
        }
    }

    @Test
    fun testAreExtensionsSorted() {
        // simultaneously checks the requirement that duplicate extension entries are adjacent
        for ((curr, next) in MIME_TYPES.zip(MIME_TYPES.drop(1))) {
            val (ext, _) = curr
            val (nExt, _) = next
            assertTrue(
                ext <= nExt,
                "Extensions in src/mime_types should be sorted lexicographically\n" +
                    "                in ascending order. Failed assert: \"$ext\" <= \"$nExt\"",
            )
        }
    }

    @Test
    fun testGetMimeExtensionsStrNoPanicIfBadMime() {
        assertNull(getMimeExtensionsStr(""))
    }

    @Test
    fun testRevMappings() {
        val videoExts = getMimeExtensionsStr("video/*")
        assertNotNull(videoExts)
        assertTrue(videoExts.contains("mp4") || videoExts.contains("mkv"))

        val mkvExts = getMimeExtensionsStr("video/x-matroska")
        assertNotNull(mkvExts)
        assertTrue(mkvExts.contains("mkv"))

        val starStar = getMimeExtensionsStr("*/*")
        assertNotNull(starStar)
        assertTrue(starStar.isNotEmpty())
    }

    @Test
    fun testIterators() {
        val guess = fromExt("gif")
        val iter = guess.iter()
        assertEquals(iter.size, iter.len())
        assertEquals(iter.size, iter.sizeHint().first)

        val rawIter = guess.iterRaw()
        assertEquals(rawIter.size, rawIter.len())
        assertEquals(rawIter.size, rawIter.sizeHint().first)

        val reversedIter = iter.reversed()
        assertNotNull(reversedIter)
    }
}

