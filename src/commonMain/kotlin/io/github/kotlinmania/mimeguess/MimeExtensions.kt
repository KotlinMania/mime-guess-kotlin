// port-lint: source lib.rs
package io.github.kotlinmania.mimeguess

import io.github.kotlinmania.mime.Mime

import io.github.kotlinmania.mime.APPLICATION_OCTET_STREAM

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
fun guessMimeType(path: String): Mime = fromPath(path).firstOrOctetStream()

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
fun guessMimeTypeOpt(path: String): Mime? = fromPath(path).first()

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
fun getMimeType(searchExt: String): Mime = fromExt(searchExt).firstOrOctetStream()

/**
 * Get the MIME type associated with a file extension.
 *
 * If there is no association for the extension, or `ext` is empty, `null` is returned.
 */
@Deprecated(
    message = "Use fromExt(searchExt).first() instead",
    replaceWith = ReplaceWith("fromExt(searchExt).first()"),
)
fun getMimeTypeOpt(searchExt: String): Mime? = fromExt(searchExt).first()

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
 * Get a list of known extensions for a given [Mime].
 *
 * Ignores parameters (only searches with `<main type>/<subtype>`). Case-insensitive (for the
 * extension types).
 *
 * Returns `null` if the MIME type is unknown.
 */
fun getMimeExtensions(mime: Mime): List<String>? = getExtensions(mime.type().asStr(), mime.subtype().asStr())

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
 */
fun getMimeExtensions(mime: String): List<String>? = getMimeExtensionsStr(mime)

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
 * Get the MIME types associated with a file extension.
 */
fun getMimeTypes(ext: String): List<String>? = ImplBinSearch.getMimeTypes(ext)

/**
 * Get the extensions associated with a top-level and sub-level MIME type.
 */
fun getExtensions(toplevel: String, sublevel: String): List<String>? = ImplBinSearch.getExtensions(toplevel, sublevel)

/**
 * Get the MIME type for `application/octet-stream` (generic binary stream).
 */
@Deprecated(
    message = "Use MIME_APPLICATION_OCTET_STREAM instead",
    replaceWith = ReplaceWith("MIME_APPLICATION_OCTET_STREAM"),
)
fun octetStream(): Mime = APPLICATION_OCTET_STREAM
