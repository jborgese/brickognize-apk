package com.frootsnoops.brickognize.util

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Natural Sort Comparator Tests")
class NaturalSortTest {

    @Test
    @DisplayName("Sorts single-digit numbers correctly")
    fun `sorts single digit numbers`() {
        val input = listOf("A3", "A1", "A2")
        val sorted = input.sortedWith(naturalSortComparator)
        assertThat(sorted).containsExactly("A1", "A2", "A3").inOrder()
    }

    @Test
    @DisplayName("Sorts double-digit numbers in natural order (10 after 9)")
    fun `sorts double digit numbers naturally`() {
        val input = listOf("Bin 10", "Bin 2", "Bin 1", "Bin 20", "Bin 3")
        val sorted = input.sortedWith(naturalSortComparator)
        assertThat(sorted).containsExactly("Bin 1", "Bin 2", "Bin 3", "Bin 10", "Bin 20").inOrder()
    }

    @Test
    @DisplayName("Sorts purely numeric labels naturally")
    fun `sorts numeric labels`() {
        val input = listOf("10", "1", "9", "2", "20", "100")
        val sorted = input.sortedWith(naturalSortComparator)
        assertThat(sorted).containsExactly("1", "2", "9", "10", "20", "100").inOrder()
    }

    @Test
    @DisplayName("Sorts case-insensitively")
    fun `case insensitive sort`() {
        val input = listOf("b1", "A2", "a1", "B2")
        val sorted = input.sortedWith(naturalSortComparator)
        assertThat(sorted).containsExactly("a1", "A2", "b1", "B2").inOrder()
    }

    @Test
    @DisplayName("Handles mixed alpha-numeric segments")
    fun `handles mixed segments`() {
        val input = listOf("Shelf2B", "Shelf10A", "Shelf2A", "Shelf1A")
        val sorted = input.sortedWith(naturalSortComparator)
        assertThat(sorted).containsExactly("Shelf1A", "Shelf2A", "Shelf2B", "Shelf10A").inOrder()
    }

    @Test
    @DisplayName("Handles empty and single-character strings")
    fun `handles edge cases`() {
        val input = listOf("B", "", "A", "1")
        val sorted = input.sortedWith(naturalSortComparator)
        assertThat(sorted).containsExactly("", "1", "A", "B").inOrder()
    }

    @Test
    @DisplayName("Equal strings compare as equal")
    fun `equal strings`() {
        assertThat(naturalSortComparator.compare("A1", "A1")).isEqualTo(0)
    }
}
