package de.pbauerochse.worklogviewer.view.grouping

import de.pbauerochse.worklogviewer.TestDataProvider
import de.pbauerochse.worklogviewer.util.FormattingUtil.getFormatted
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class GroupingsTest {

    @Test
    fun `not grouping at all creates a default grouping`() {
        val grouping = Groupings(emptyList())

        val rootNode = grouping.treeRoot(TestDataProvider.issues)

        assertEquals(TestDataProvider.issues.size, rootNode.issues.size, "All Issues should be contained on the root node")
        assertEquals(0, rootNode.subgroups.size, "No subgrouping expected as there a no grouping criterias")
        assertTrue(rootNode.subgroups.all { it.subgroups.isEmpty() }, "Expected no further grouping")
    }

    @Test
    fun `grouping by a single project field value`() {
        val grouping = Groupings(listOf(FieldBasedGrouping("Criteria C")))

        val rootNode = grouping.treeRoot(TestDataProvider.issues)

        rootNode.subgroups.map { it.groupValue }.sorted().forEach { print("\"$it\", ") }

        assertEquals(15, rootNode.subgroups.size)
        assertEquals(
            listOf(
                "Another Value 0, Yet another value 0", "Another Value 1, Yet another value 1", "Another Value 10, Yet another value 10",
                "Another Value 11, Yet another value 11", "Another Value 12, Yet another value 12", "Another Value 13, Yet another value 13",
                "Another Value 14, Yet another value 14", "Another Value 2, Yet another value 2", "Another Value 3, Yet another value 3",
                "Another Value 4, Yet another value 4", "Another Value 5, Yet another value 5", "Another Value 6, Yet another value 6",
                "Another Value 7, Yet another value 7", "Another Value 8, Yet another value 8", "Another Value 9, Yet another value 9"
            ),
            rootNode.subgroups.map { it.groupValue }.sorted()
        )
        assertTrue(rootNode.subgroups.all { primaryGroupingNode -> primaryGroupingNode.subgroups.isEmpty() }, "Only one level of groupings expected")
        assertEquals(TestDataProvider.issues.size, rootNode.subgroups.sumBy { it.issues.size }, "All issues should be present in the subgroups")
    }

    @Test
    fun `grouping by a single worklog item field value`() {
        val criteria = WorklogItemBasedGrouping("WORKTYPE", "Worktype") { it.workType }
        val grouping = Groupings(listOf(criteria))

        val rootNode = grouping.treeRoot(TestDataProvider.issues)

        assertEquals(5, rootNode.subgroups.size)
        assertEquals(listOf(getFormatted("grouping.none"), "Worktype 0", "Worktype 3", "Worktype 6", "Worktype 9"), rootNode.subgroups.map { it.groupValue }.sorted())
        assertTrue(rootNode.subgroups.all { primarySubgroup -> primarySubgroup.subgroups.isEmpty() }, "Only one level of groupings expected")
    }

    @Test
    fun `grouping multiple project field values`() {
        val grouping = Groupings(listOf(
            FieldBasedGrouping("Criteria C"),
            FieldBasedGrouping("Criteria A")
        ))

        val rootNode = grouping.treeRoot(TestDataProvider.issues)

        assertEquals(15, rootNode.subgroups.size)
        assertEquals(
            listOf(
                "Another Value 0, Yet another value 0", "Another Value 1, Yet another value 1", "Another Value 10, Yet another value 10",
                "Another Value 11, Yet another value 11", "Another Value 12, Yet another value 12", "Another Value 13, Yet another value 13",
                "Another Value 14, Yet another value 14", "Another Value 2, Yet another value 2", "Another Value 3, Yet another value 3",
                "Another Value 4, Yet another value 4", "Another Value 5, Yet another value 5", "Another Value 6, Yet another value 6",
                "Another Value 7, Yet another value 7", "Another Value 8, Yet another value 8", "Another Value 9, Yet another value 9"
            ),
            rootNode.subgroups.map { it.groupValue }.sorted()
        )

        assertTrue(rootNode.subgroups.all { primarySubgroup -> primarySubgroup.subgroups.isNotEmpty() }, "Another level of groupings expected")
        assertEquals(listOf("Value 11"), rootNode.subgroups[0].subgroups.map { it.groupValue }.sorted())
        assertEquals(listOf("Value 12"), rootNode.subgroups[1].subgroups.map { it.groupValue }.sorted())
        assertEquals(listOf("Value 13"), rootNode.subgroups[2].subgroups.map { it.groupValue }.sorted())
        assertEquals(listOf("Value 14"), rootNode.subgroups[3].subgroups.map { it.groupValue }.sorted())
        assertEquals(listOf("Value 0"), rootNode.subgroups[4].subgroups.map { it.groupValue }.sorted())
        assertEquals(listOf("Value 1"), rootNode.subgroups[5].subgroups.map { it.groupValue }.sorted())
        assertEquals(listOf("Value 2"), rootNode.subgroups[6].subgroups.map { it.groupValue }.sorted())
        assertEquals(listOf("Value 3"), rootNode.subgroups[7].subgroups.map { it.groupValue }.sorted())
        assertEquals(listOf("Value 4"), rootNode.subgroups[8].subgroups.map { it.groupValue }.sorted())
        assertEquals(listOf("Value 5"), rootNode.subgroups[9].subgroups.map { it.groupValue }.sorted())
        assertEquals(listOf("Value 6"), rootNode.subgroups[10].subgroups.map { it.groupValue }.sorted())
        assertEquals(listOf("Value 7"), rootNode.subgroups[11].subgroups.map { it.groupValue }.sorted())
        assertEquals(listOf("Value 8"), rootNode.subgroups[12].subgroups.map { it.groupValue }.sorted())
        assertEquals(listOf("Value 9"), rootNode.subgroups[13].subgroups.map { it.groupValue }.sorted())
        assertEquals(listOf("Value 10"), rootNode.subgroups[14].subgroups.map { it.groupValue }.sorted())
        assertEquals(TestDataProvider.issues.size, rootNode.subgroups.sumBy { subgroup -> subgroup.subgroups.sumBy { it.issues.size } }, "Deepest grouping level should contain all issues")
    }

    @Test
    fun `grouping multiple worklog item field values`() {
        val grouping = Groupings(listOf(
            WorklogItemBasedGrouping("WORKTYPE", "Worktype") { it.workType },
            WorklogItemBasedGrouping("WORKAUTHOR", "Workauthor") { it.user.username }
        ))

        val rootNode = grouping.treeRoot(TestDataProvider.issues)

        assertEquals(5, rootNode.subgroups.size)
        assertEquals(listOf(getFormatted("grouping.none"), "Worktype 0", "Worktype 3", "Worktype 6", "Worktype 9"), rootNode.subgroups.map { it.groupValue }.sorted())

        assertTrue(rootNode.subgroups.all { primarySubgroup -> primarySubgroup.subgroups.isNotEmpty() }, "Another level of groupings expected")
        assertEquals(listOf("alice", "bob", "charlie", "me"), rootNode.subgroups[0].subgroups.map { it.groupValue }.sorted())
        assertEquals(listOf("alice", "bob", "charlie", "me"), rootNode.subgroups[1].subgroups.map { it.groupValue }.sorted())
        assertEquals(listOf("alice", "bob", "charlie", "me"), rootNode.subgroups[2].subgroups.map { it.groupValue }.sorted())
        assertEquals(listOf("alice", "bob", "charlie", "me"), rootNode.subgroups[3].subgroups.map { it.groupValue }.sorted())
        assertEquals(listOf("alice", "bob", "charlie", "me"), rootNode.subgroups[4].subgroups.map { it.groupValue }.sorted())
    }

    @Test
    fun `grouping by multiple mixed worklog item and project field values`() {
        val grouping = Groupings(listOf(
            FieldBasedGrouping("Criteria C"),
            WorklogItemBasedGrouping("WORKAUTHOR", "Workauthor") { it.user.username }
        ))

        val rootNode = grouping.treeRoot(TestDataProvider.issues)

        assertEquals(15, rootNode.subgroups.size)
        rootNode.subgroups.forEach { primaryGroup ->
            assertEquals(listOf("alice", "bob", "charlie", "me"), primaryGroup.subgroups.map { it.groupValue }.sorted())
        }
    }

}