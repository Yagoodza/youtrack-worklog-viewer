package de.pbauerochse.worklogviewer.view.grouping

import de.pbauerochse.worklogviewer.TestDataProvider
import de.pbauerochse.worklogviewer.util.FormattingUtil
import org.junit.jupiter.api.Assertions.*
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
        val grouping = Groupings(listOf(FieldBasedGrouping("Criteria C")))

        val rows = grouping.rows(TestDataProvider.issues)

        assertEquals(15, rows.size)
        assertTrue(rows.all { row -> row.children.all { it.isIssue } })
    }

    @Test
    fun `grouping by a single worklog item field value`() {
        val grouping = Groupings(
            listOf(WorklogItemBasedGrouping(
                "WORKTYPE",
                FormattingUtil.getFormatted("grouping.worktype")
            ) { it.workType })
        )

        val rows = grouping.rows(TestDataProvider.issues)

        assertEquals(5, rows.size)
        assertTrue(rows.all { row -> row.children.all { it.isIssue } })
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