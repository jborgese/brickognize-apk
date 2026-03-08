package com.frootsnoops.brickognize.util

/**
 * Comparator that sorts strings with embedded numbers in natural order.
 * E.g., "A2" < "A10" instead of lexicographic "A10" < "A2".
 */
val naturalSortComparator: Comparator<String> = Comparator { a, b ->
    val aParts = splitAlphaNumeric(a.uppercase())
    val bParts = splitAlphaNumeric(b.uppercase())

    for (i in 0 until minOf(aParts.size, bParts.size)) {
        val aPart = aParts[i]
        val bPart = bParts[i]
        val aNum = aPart.toLongOrNull()
        val bNum = bPart.toLongOrNull()

        val cmp = if (aNum != null && bNum != null) {
            aNum.compareTo(bNum)
        } else {
            aPart.compareTo(bPart)
        }

        if (cmp != 0) return@Comparator cmp
    }

    aParts.size.compareTo(bParts.size)
}

private fun splitAlphaNumeric(s: String): List<String> {
    val parts = mutableListOf<String>()
    val current = StringBuilder()
    var wasDigit = false

    for (c in s) {
        val isDigit = c.isDigit()
        if (current.isNotEmpty() && isDigit != wasDigit) {
            parts.add(current.toString())
            current.clear()
        }
        current.append(c)
        wasDigit = isDigit
    }

    if (current.isNotEmpty()) {
        parts.add(current.toString())
    }

    return parts
}
