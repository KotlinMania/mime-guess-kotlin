// port-lint: source impl_phf.rs
package io.github.kotlinmania.mimeguess

/**
 * Map lookup implementation.
 */
internal object ImplPhf {
    data class TopLevelExts(
        val start: Int,
        val end: Int,
        val subs: List<Pair<String, Pair<Int, Int>>>,
    )

    fun getMimeTypes(ext: String): List<String>? {
        return mapLookup(MIME_TYPES, ext)
    }

    fun getExtensions(toplevel: String, sublevel: String): List<String>? {
        if (toplevel == "*") {
            return EXTS
        }

        val top = mapLookup(REV_MAPPINGS, toplevel) ?: return null

        if (sublevel == "*") {
            return EXTS.subList(top.start, top.end)
        }

        val sub = mapLookupSubs(top.subs, sublevel) ?: return null
        return EXTS.subList(sub.first, sub.second)
    }

    private fun <V> mapLookup(map: List<Pair<String, V>>, key: String): V? {
        val idx = map.binarySearch { (k, _) -> k.compareTo(key, ignoreCase = true) }
        return if (idx >= 0) map[idx].second else null
    }

    private fun mapLookupSubs(map: List<Pair<String, Pair<Int, Int>>>, key: String): Pair<Int, Int>? {
        val idx = map.binarySearch { (k, _) -> k.compareTo(key, ignoreCase = true) }
        return if (idx >= 0) map[idx].second else null
    }
}
