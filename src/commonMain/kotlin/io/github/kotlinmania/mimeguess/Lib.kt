// port-lint: source src/lib.rs
package io.github.kotlinmania.mimeguess

/**
 * Guessing of MIME types by file extension.
 *
 * Uses a static list of file-extension : MIME type mappings.
 *
 * ```
 * // the file doesn't have to exist, it just looks at the path
 * val guess = fromPath("some_file.gif")
 * check(guess.first() == "image/gif")
 * ```
 *
 * #### Note: MIME Types Returned Are Not Stable/Guaranteed
 * The media types returned for a given extension are not considered to be part of the crate's
 * stable API and are often updated in patch (`x.y.[z + 1]`) releases to be as correct as
 * possible.
 *
 * Additionally, only the extensions of paths/filenames are inspected in order to guess the MIME
 * type. The file that may or may not reside at that path may or may not be a valid file of the
 * returned MIME type. Be wary of unsafe or un-validated assumptions about file structure or
 * length.
 *
 * The upstream Rust crate re-exports `mime::Mime` as its own media-type. Until a `mime-kotlin`
 * sibling artifact is published, this port represents a parsed media type as a raw [String];
 * callers who need richer parsing can wrap the result with their own type. The publicly exposed
 * helper [MIME_APPLICATION_OCTET_STREAM] and [MIME_TEXT_PLAIN] preserve the upstream constants
 * `mime::APPLICATION_OCTET_STREAM` and `mime::TEXT_PLAIN` as Kotlin-side string literals.
 */

/** Counterpart to upstream `mime::APPLICATION_OCTET_STREAM`. */
const val MIME_APPLICATION_OCTET_STREAM: String = "application/octet-stream"

/** Counterpart to upstream `mime::TEXT_PLAIN`. */
const val MIME_TEXT_PLAIN: String = "text/plain"

/**
 * A "guess" of the MIME/Media Type(s) of an extension or path as one or more media-type strings.
 *
 * ### Note: Ordering
 * A given file format may have one or more applicable Media Types; in this case the first Media
 * Type returned is whatever is declared in the latest IETF RFC for the presumed file format or
 * the one that explicitly supercedes all others. Ordering of additional Media Types is arbitrary.
 *
 * ### Note: Values Not Stable
 * The exact Media Types returned in any given guess are not considered to be stable and are
 * often updated in patch releases in order to reflect the most up-to-date information possible.
 *
 * Upstream Rust derives `Copy, Clone, Debug, PartialEq, Eq`. The data-class translation gets
 * structural equality, `hashCode`, and `toString` for free; immutability of the backing list is
 * the consumer's responsibility, matching the upstream `&'static [&'static str]` field.
 */
class MimeGuess internal constructor(private val backing: List<String>) {

    /** `true` if the guess did not return any known mappings for the given path or extension. */
    fun isEmpty(): Boolean = backing.isEmpty()

    /** Get the number of MIME types in the current guess. */
    fun count(): Int = backing.size

    /**
     * Get the first guessed media-type string, if applicable.
     *
     * See [Note: Ordering](#note-ordering) above.
     */
    fun first(): String? = firstRaw()?.let(::expectMime)

    /**
     * Get the first guessed Media Type as a string, if applicable.
     *
     * See [Note: Ordering](#note-ordering) above.
     */
    fun firstRaw(): String? = backing.firstOrNull()

    /**
     * Get the first guessed media-type string, or if the guess is empty, return
     * [`application/octet-stream`][MIME_APPLICATION_OCTET_STREAM] instead.
     *
     * See [Note: Ordering](#note-ordering) above.
     *
     * ### Note: HTTP Applications
     * For HTTP request and response bodies if a value for the `Content-Type` header cannot be
     * determined it might be preferable to not send one at all instead of defaulting to
     * `application/octet-stream` as the recipient will expect to infer the format directly from
     * the content instead. ([RFC 7231, Section 3.1.1.5](https://tools.ietf.org/html/rfc7231#section-3.1.1.5))
     *
     * On the contrary, for `multipart/form-data` bodies, the `Content-Type` of a form-data part
     * is assumed to be `text/plain` unless specified so a default of `application/octet-stream`
     * for non-text parts is safer. ([RFC 7578, Section 4.4](https://tools.ietf.org/html/rfc7578#section-4.4))
     */
    fun firstOrOctetStream(): String = firstOr(MIME_APPLICATION_OCTET_STREAM)

    /**
     * Get the first guessed media-type string, or if the guess is empty, return
     * [`text/plain`][MIME_TEXT_PLAIN] instead.
     *
     * See [Note: Ordering](#note-ordering) above.
     */
    fun firstOrTextPlain(): String = firstOr(MIME_TEXT_PLAIN)

    /**
     * Get the first guessed media-type string, or if the guess is empty, return the given media-type string instead.
     *
     * See [Note: Ordering](#note-ordering) above.
     */
    fun firstOr(default: String): String = first() ?: default

    /**
     * Get the first guessed media-type string, or if the guess is empty, execute the closure and return its
     * result.
     *
     * See [Note: Ordering](#note-ordering) above.
     */
    fun firstOrElse(defaultFn: () -> String): String = first() ?: defaultFn()

    /**
     * Get an iterator over the parsed media-type values contained in this guess.
     *
     * See [Note: Ordering](#note-ordering) above.
     */
    fun iter(): Iter = Iter(iterRaw())

    /**
     * Get an iterator over the raw media-type strings in this guess.
     *
     * See [Note: Ordering](#note-ordering) above.
     */
    fun iterRaw(): IterRaw = IterRaw(backing)

    /** Allow `for (mime in guess)` style iteration; the iterator yields parsed media-type values. */
    operator fun iterator(): Iterator<String> = iter()

    override fun equals(other: Any?): Boolean = other is MimeGuess && backing == other.backing

    override fun hashCode(): Int = backing.hashCode()

    override fun toString(): String = "MimeGuess($backing)"

    companion object {
        /**
         * Guess the MIME type of a file (real or otherwise) with the given extension.
         *
         * The search is case-insensitive.
         *
         * If `ext` is empty or has no (currently) known MIME type mapping, then an empty guess is
         * returned.
         */
        fun fromExt(ext: String): MimeGuess {
            if (ext.isEmpty()) {
                return MimeGuess(emptyList())
            }
            val mimes = getMimeTypes(ext)
            return if (mimes != null) MimeGuess(mimes) else MimeGuess(emptyList())
        }

        /**
         * Guess the MIME type of `path` by its extension (the substring after the final `.` of the
         * file-name component, mirroring Rust `Path::extension()`). **No disk access is
         * performed.**
         *
         * If `path` has no extension, or has no known MIME type mapping, then an empty guess is
         * returned.
         *
         * The search is case-insensitive.
         *
         * ## Note
         * **Guess** is the operative word here, as there are no guarantees that the contents of
         * the file that `path` points to match the MIME type associated with the path's extension.
         *
         * Take care when processing files with assumptions based on the return value of this
         * function.
         */
        fun fromPath(path: String): MimeGuess {
            val ext = pathExtension(path)
            return if (ext != null) fromExt(ext) else MimeGuess(emptyList())
        }
    }
}

/**
 * An iterator over the parsed media-type values of a [MimeGuess].
 *
 * See [Note: Ordering on `MimeGuess`][MimeGuess].
 *
 * Upstream Rust derives `Clone, Debug` and implements `Iterator`, `DoubleEndedIterator`,
 * `FusedIterator`, and `ExactSizeIterator`. Kotlin's standard `Iterator` interface covers
 * forward iteration; bidirectional and exact-size access are surfaced through [reversed] and
 * [size] which call into the raw underlying list.
 */
class Iter internal constructor(private val raw: IterRaw) : Iterator<String> {
    override fun hasNext(): Boolean = raw.hasNext()

    override fun next(): String = expectMime(raw.next())

    /** Length of the underlying sequence, matching upstream `ExactSizeIterator::len`. */
    val size: Int get() = raw.size

    /** Reverse iteration helper, matching upstream `DoubleEndedIterator::next_back`. */
    fun reversed(): Iter = Iter(raw.reversed())
}

/**
 * An iterator over the raw media type strings of a [MimeGuess].
 *
 * See [Note: Ordering on `MimeGuess`][MimeGuess].
 */
class IterRaw internal constructor(private val backing: List<String>) : Iterator<String> {
    private var index: Int = 0
    private var endExclusive: Int = backing.size

    override fun hasNext(): Boolean = index < endExclusive

    override fun next(): String {
        if (index >= endExclusive) throw NoSuchElementException()
        return backing[index++]
    }

    /** Pop and return the last remaining element, matching upstream `DoubleEndedIterator`. */
    fun nextBack(): String? {
        if (index >= endExclusive) return null
        endExclusive--
        return backing[endExclusive]
    }

    /** Length of the underlying sequence, matching upstream `ExactSizeIterator::len`. */
    val size: Int get() = endExclusive - index

    /** Reverse iteration helper, matching upstream `DoubleEndedIterator::next_back`. */
    fun reversed(): IterRaw = IterRaw(backing.subList(index, endExclusive).asReversed())
}

private fun expectMime(s: String): String {
    // The mime-types table is exhaustively tested for parseability upstream; for the
    // string-typed Kotlin port this is the identity function, but the indirection is preserved
    // so a future `Mime` data class can plug in real parsing here.
    return s
}

/**
 * Returns the extension of `path` (without the leading dot), or `null` if `path` has no
 * extension. Mirrors Rust `Path::extension()`:
 * - Examines only the file-name component (after the last path separator).
 * - Returns `null` for empty paths, for paths whose final component starts with `.`, and for
 *   final components that contain no `.` at all.
 *
 * Both `/` and `\` are treated as path separators so the helper is independent of the host
 * platform's directory delimiter.
 */
private fun pathExtension(path: String): String? {
    val lastSep = path.lastIndexOfAny(charArrayOf('/', '\\'))
    val fileName = if (lastSep >= 0) path.substring(lastSep + 1) else path
    if (fileName.isEmpty()) return null
    if (fileName.startsWith(".")) return null
    val dot = fileName.lastIndexOf('.')
    if (dot < 0) return null
    val ext = fileName.substring(dot + 1)
    return if (ext.isEmpty()) null else ext
}

/** Wrapper of [MimeGuess.fromExt]. */
fun fromExt(ext: String): MimeGuess = MimeGuess.fromExt(ext)

/** Wrapper of [MimeGuess.fromPath]. */
fun fromPath(path: String): MimeGuess = MimeGuess.fromPath(path)

/**
 * Guess the MIME type of `path` by its extension.
 *
 * If `path` has no extension, or its extension has no known MIME type mapping, then the MIME
 * type is assumed to be `application/octet-stream`.
 *
 * ## Note
 * **Guess** is the operative word here, as there are no guarantees that the contents of the
 * file that `path` points to match the MIME type associated with the path's extension.
 *
 * Take care when processing files with assumptions based on the return value of this function.
 *
 * In HTTP applications, it might be [preferable](https://tools.ietf.org/html/rfc7231#section-3.1.1.5)
 * to not send a `Content-Type` header at all instead of defaulting to `application/octet-stream`.
 */
@Deprecated(
    message = "Use fromPath(path).firstOrOctetStream() instead",
    replaceWith = ReplaceWith("fromPath(path).firstOrOctetStream()"),
)
fun guessMimeType(path: String): String = fromPath(path).firstOrOctetStream()

/**
 * Guess the MIME type of `path` by its extension.
 *
 * If `path` has no extension, or its extension has no known MIME type mapping, then `null` is
 * returned.
 */
@Deprecated(
    message = "Use fromPath(path).first() instead",
    replaceWith = ReplaceWith("fromPath(path).first()"),
)
fun guessMimeTypeOpt(path: String): String? = fromPath(path).first()

/**
 * Guess the MIME type string of `path` by its extension.
 *
 * If `path` has no extension, or its extension has no known MIME type mapping, then `null` is
 * returned.
 *
 * ## Note
 * **Guess** is the operative word here, as there are no guarantees that the contents of the
 * file that `path` points to match the MIME type associated with the path's extension.
 *
 * Take care when processing files with assumptions based on the return value of this function.
 */
@Deprecated(
    message = "Use fromPath(path).firstRaw() instead",
    replaceWith = ReplaceWith("fromPath(path).firstRaw()"),
)
fun mimeStrForPathExt(path: String): String? = fromPath(path).firstRaw()

/**
 * Get the MIME type associated with a file extension.
 *
 * If there is no association for the extension, or `ext` is empty, `application/octet-stream`
 * is returned.
 *
 * ## Note
 * In HTTP applications, it might be [preferable](https://tools.ietf.org/html/rfc7231#section-3.1.1.5)
 * to not send a `Content-Type` header at all instead of defaulting to `application/octet-stream`.
 */
@Deprecated(
    message = "Use fromExt(searchExt).firstOrOctetStream() instead",
    replaceWith = ReplaceWith("fromExt(searchExt).firstOrOctetStream()"),
)
fun getMimeType(searchExt: String): String = fromExt(searchExt).firstOrOctetStream()

/**
 * Get the MIME type associated with a file extension.
 *
 * If there is no association for the extension, or `ext` is empty, `null` is returned.
 */
@Deprecated(
    message = "Use fromExt(searchExt).first() instead",
    replaceWith = ReplaceWith("fromExt(searchExt).first()"),
)
fun getMimeTypeOpt(searchExt: String): String? = fromExt(searchExt).first()

/**
 * Get the MIME type string associated with a file extension. Case-insensitive.
 *
 * If `searchExt` is not already lowercase, it will be converted to lowercase to facilitate the
 * search.
 *
 * Returns `null` if `searchExt` is empty or an associated extension was not found.
 */
@Deprecated(
    message = "Use fromExt(searchExt).firstRaw() instead",
    replaceWith = ReplaceWith("fromExt(searchExt).firstRaw()"),
)
fun getMimeTypeStr(searchExt: String): String? = fromExt(searchExt).firstRaw()

/**
 * Get a list of known extensions for a given media-type string of the form `top/sub`.
 *
 * Ignores parameters (only searches with `<main type>/<subtype>`). Case-insensitive (for the
 * extension types).
 *
 * Returns `null` if the MIME type is unknown.
 *
 * ### Wildcards
 * If the top-level of the MIME type is a wildcard (`*`), returns all extensions.
 *
 * If the sub-level of the MIME type is a wildcard, returns all extensions for the top-level.
 *
 * The upstream Rust signature `getMimeExtensions(mime: Mime)` takes a parsed `mime::Mime`;
 * the Kotlin port deconstructs the string-typed media value inline, so this is the same operation as
 * [getMimeExtensionsStr] minus the `;`-parameter trimming.
 */
fun getMimeExtensions(mime: String): List<String>? {
    val splitIdx = mime.indexOf('/')
    if (splitIdx < 0) return null
    val top = mime.substring(0, splitIdx)
    val sub = mime.substring(splitIdx + 1)
    return getExtensions(top, sub)
}

/**
 * Get a list of known extensions for a MIME type string.
 *
 * Ignores parameters (only searches `<main type>/<subtype>`). Case-insensitive.
 *
 * Returns `null` if the MIME type is unknown.
 *
 * ### Wildcards
 * If the top-level of the MIME type is a wildcard (`*`), returns all extensions.
 *
 * If the sub-level of the MIME type is a wildcard, returns all extensions for the top-level.
 *
 * ### Throws
 * Returns `null` if `mimeStr` is not a valid MIME type specifier (naive).
 */
fun getMimeExtensionsStr(mimeStr: String): List<String>? {
    var trimmed = mimeStr.trim()

    val sepIdx = trimmed.indexOf(';')
    if (sepIdx >= 0) {
        trimmed = trimmed.substring(0, sepIdx)
    }

    val splitIdx = trimmed.indexOf('/')
    if (splitIdx < 0) return null
    val top = trimmed.substring(0, splitIdx)
    val sub = trimmed.substring(splitIdx + 1)

    return getExtensions(top, sub)
}

/**
 * Get the MIME type for `application/octet-stream` (generic binary stream).
 */
@Deprecated(
    message = "Use MIME_APPLICATION_OCTET_STREAM instead",
    replaceWith = ReplaceWith("MIME_APPLICATION_OCTET_STREAM"),
)
fun octetStream(): String = MIME_APPLICATION_OCTET_STREAM
