// port-lint: source src/lib.rs
package io.github.kotlinmania.mimeguess

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MimeGuessTest {
    @Test
    fun checkTypeBounds() {
        fun <T> assertTypeBounds() {}

        assertTypeBounds<MimeGuess>()
        assertTypeBounds<Iter>()
        assertTypeBounds<IterRaw>()
    }

    @Test
    fun testMimeTypeGuessing() {
        assertEquals(
            "image/gif",
            fromExt("gif").firstOrOctetStream().toString(),
        )
        assertEquals(
            "text/plain",
            fromExt("TXT").firstOrOctetStream().toString(),
        )
        assertEquals(
            "application/octet-stream",
            fromExt("blahblah").firstOrOctetStream().toString(),
        )

        assertEquals(
            "image/gif",
            fromPath("/path/to/file.gif").firstOrOctetStream().toString(),
        )
        assertEquals(
            "image/gif",
            fromPath("/path/to/file.gif").firstOrOctetStream().toString(),
        )
    }

    @Test
    fun testMimeTypeGuessingOpt() {
        assertEquals(
            "image/gif",
            fromExt("gif").first()!!.toString(),
        )
        assertEquals(
            "text/plain",
            fromExt("TXT").first()!!.toString(),
        )
        assertNull(fromExt("blahblah").first())

        assertEquals(
            "image/gif",
            fromPath("/path/to/file.gif").first()!!.toString(),
        )
        assertNull(fromPath("/path/to/file").first())
    }

    @Test
    fun testAreMimeTypesParseable() {
        for ((_, mimes) in MIME_TYPES) {
            mimes.forEach { s ->
                expectMime(s)
            }
        }
    }

    // RFC: Is this test necessary anymore? --@cybergeek94, 2/1/2016
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
        assertEquals(null, getMimeExtensionsStr(""))
    }
}
