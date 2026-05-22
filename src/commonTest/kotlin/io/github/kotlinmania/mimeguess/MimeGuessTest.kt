// port-lint: source src/lib.rs
package io.github.kotlinmania.mimeguess

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MimeGuessTest {
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
}
