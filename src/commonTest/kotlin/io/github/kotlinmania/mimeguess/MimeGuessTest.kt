// port-lint: ignore - translated subset of src/lib.rs tests that compile against the current port
package io.github.kotlinmania.mimeguess

import kotlin.test.Test
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
}
