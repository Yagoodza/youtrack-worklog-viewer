package de.pbauerochse.worklogviewer.view.grouping

import de.pbauerochse.worklogviewer.TestDataProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

internal class GroupingsTest {

    @Test
    fun `not grouping at all creates a default grouping`() {
        val grouping = Groupings(emptyList())

        val rows = grouping.rows(TestDataProvider.issues)

        assertEquals(1, rows.size)
    }

    @Test
    fun `grouping by a single project field value`() {
        fail<Unit>("Not implemented yet")
    }

    @Test
    fun `grouping by a single worklog item field value`() {
        fail<Unit>("Not implemented yet")
    }

    @Test
    fun `grouping multiple project field values`() {
        fail<Unit>("Not implemented yet")
    }

    @Test
    fun `grouping multiple worklog item field values`() {
        fail<Unit>("Not implemented yet")
    }

    @Test
    fun `grouping by multiple mixed worklog item and project field values`() {
        fail<Unit>("Not implemented yet")
    }

}