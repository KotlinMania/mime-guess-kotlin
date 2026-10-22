package io.github.kotlinmania.mimeguess

import io.github.kotlinmania.mime.APPLICATION_OCTET_STREAM
import io.github.kotlinmania.mime.Mime
import io.github.kotlinmania.mime.TEXT_PLAIN
import io.github.kotlinmania.mime.parse

/**
 * A "guess" of the MIME/Media Type(s) of an extension or path as one or more
 * [Mime] instances.
 *
 * ### Note: Ordering
 * A given file format may have one or more applicable Media Types; in this case the first Media
 * Type returned is whatever is declared in the latest IETF RFC for the presumed file format or
 * the one that explicitly supercedes all others. Ordering of additional Media Types is arbitrary.
 *
 * ### Note: Values Not Stable
 * The exact Media Types returned in any given guess are not considered to be stable and are
 * often updated in patch releases in order to reflect the most up-to-date information possible.
 */
class MimeGuess internal constructor(
    private val backing: List<String>,
) {
    /** `true` if the guess did not return any known mappings for the given path or extension. */
    fun isEmpty(): Boolean = backing.isEmpty()

    /** Get the number of MIME types in the current guess. */
    fun count(): Int = backing.size

    /**
     * Get the first guessed [Mime], if applicable.
     *
     * See [Note: Ordering](#note-ordering) above.
     */
    fun first(): Mime? = firstRaw()?.let(::expectMime)

    /**
     * Get the first guessed Media Type as a string, if applicable.
     *
     * See [Note: Ordering](#note-ordering) above.
     */
    fun firstRaw(): String? = backing.firstOrNull()

    /**
     * Get the first guessed [Mime], or if the guess is empty, return
     * [APPLICATION_OCTET_STREAM] instead.
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
    fun firstOrOctetStream(): Mime = firstOr(APPLICATION_OCTET_STREAM)

    /**
     * Get the first guessed [Mime], or if the guess is empty, return
     * [TEXT_PLAIN] instead.
     *
     * See [Note: Ordering](#note-ordering) above.
     */
    fun firstOrTextPlain(): Mime = firstOr(TEXT_PLAIN)

    /**
     * Get the first guessed [Mime], or if the guess is empty, return the given [Mime] instead.
     *
     * See [Note: Ordering](#note-ordering) above.
     */
    fun firstOr(default: Mime): Mime = first() ?: default

    /**
     * Get the first guessed [Mime], or if the guess is empty, execute the closure and return its
     * result.
     *
     * See [Note: Ordering](#note-ordering) above.
     */
    fun firstOrElse(defaultFn: () -> Mime): Mime = first() ?: defaultFn()

    /**
     * Get an iterator over the [Mime] values contained in this guess.
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

    /** Allow `for (mime in guess)` style iteration; the iterator yields [Mime] values. */
    operator fun iterator(): Iterator<Mime> = iter()

    /** Into-iterator conversion. */
    fun intoIter(): Iter = iter()

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

typealias Item = Mime
typealias IntoIter = Iter

/**
 * An iterator over the [Mime] values of a [MimeGuess].
 *
 * See [Note: Ordering on `MimeGuess`][MimeGuess].
 */
class Iter internal constructor(
    private val raw: IterRaw,
) : Iterator<Mime> {
    override fun hasNext(): Boolean = raw.hasNext()

    override fun next(): Mime = expectMime(raw.next())

    /** Length of the underlying sequence. */
    val size: Int get() = raw.size

    /** Length of the underlying sequence. */
    fun len(): Int = size

    /** Size hint of the iterator. */
    fun sizeHint(): Pair<Int, Int?> = raw.sizeHint()

    /** Pop and return the last remaining parsed media-type value. */
    fun nextBack(): Mime? = raw.nextBack()?.let(::expectMime)

    /** Reverse iteration helper, producing a new fully-reversed view of the remaining range. */
    fun reversed(): Iter = Iter(raw.reversed())
}

/**
 * An iterator over the raw media type strings of a [MimeGuess].
 *
 * See [Note: Ordering on `MimeGuess`][MimeGuess].
 */
class IterRaw internal constructor(
    private val backing: List<String>,
) : Iterator<String> {
    private var index: Int = 0
    private var endExclusive: Int = backing.size

    override fun hasNext(): Boolean = index < endExclusive

    override fun next(): String {
        if (index >= endExclusive) throw NoSuchElementException()
        return backing[index++]
    }

    /** Pop and return the last remaining element. */
    fun nextBack(): String? {
        if (index >= endExclusive) return null
        endExclusive--
        return backing[endExclusive]
    }

    /** Length of the underlying sequence. */
    val size: Int get() = endExclusive - index

    /** Length of the underlying sequence. */
    fun len(): Int = size

    /** Size hint of the iterator. */
    fun sizeHint(): Pair<Int, Int?> = Pair(size, size)

    /** Reverse iteration helper. */
    fun reversed(): IterRaw = IterRaw(backing.subList(index, endExclusive).asReversed())
}

internal fun expectMime(s: String): Mime = parse(s)

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
internal fun pathExtension(path: String): String? {
    val lastSep = path.lastIndexOfAny(charArrayOf('/', '\\'))
    val fileName = if (lastSep >= 0) path.substring(lastSep + 1) else path
    if (fileName.isEmpty()) return null
    if (fileName.startsWith(".")) return null
    val dot = fileName.lastIndexOf('.')
    if (dot < 0) return null
    val ext = fileName.substring(dot + 1)
    return if (ext.isEmpty()) null else ext
}
