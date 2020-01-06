package de.pbauerochse.worklogviewer.view.grouping

import de.pbauerochse.worklogviewer.report.Issue
import de.pbauerochse.worklogviewer.report.view.ReportRow
import de.pbauerochse.worklogviewer.view.GroupReportRow
import de.pbauerochse.worklogviewer.view.IssueReportRow

class Groupings(private val groupings: List<Grouping>) {

    /**
     * Converts the given issues to ReportRows
     * by applying any necessary grouping criteria
     */
    fun rows(issues: List<Issue>): List<ReportRow> {
        val rootGroup = IssueGroup("root", issues)

        if (groupings.isEmpty()) {
            rootGroup.subgroups.add(IssueGroup("MEH?", issues))
        } else {
            val inputGroups = mutableListOf(rootGroup)
            groupings.forEach { grouping ->
                val currentPassGroups = mutableListOf<IssueGroup>()
                inputGroups.forEach { inputGroup ->
                    val subGroups = extractIssueGroups(inputGroup, grouping)
                    inputGroup.subgroups.addAll(subGroups)
                    currentPassGroups.addAll(subGroups)
                }

                inputGroups.clear()
                inputGroups.addAll(currentPassGroups)
            }
        }

//        var previousGroups = listOf(rootGroup)
//        groupings.forEach { grouping ->
//            val tmpPreviousGroups = mutableListOf<IssueGroup>()
//            previousGroups.forEach { parentGroup ->
//                val subgroups = grouping
//                    .group(parentGroup.issues)
//                    .map { group -> IssueGroup(group.key, group.value) }
//                parentGroup.subgroups.addAll(subgroups)
//                tmpPreviousGroups.addAll(subgroups)
//            }
//
//            previousGroups = tmpPreviousGroups
//        }

        return rootGroup.subgroups.flatMap { createReportRows(it) }
    }

    private fun extractIssueGroups(parent: IssueGroup, grouping: Grouping): List<IssueGroup> {
        val groupedIssues = grouping.group(parent.issues)
        return groupedIssues.map { IssueGroup(it.key, it.value) }
    }

    private fun createReportRows(group: IssueGroup, parent: ReportRow? = null): List<ReportRow> {
        val childGroups = mutableListOf<ReportRow>()

        val groupReportRow = GroupReportRow(group.groupValue, childGroups)
        parent?.children?.add(groupReportRow)

        if (group.subgroups.isEmpty()) {
            childGroups.addAll(group.issues.map { issue -> IssueReportRow(issue) }.toMutableList())
        } else {
            childGroups.addAll(
                group.subgroups.flatMap { createReportRows(it, groupReportRow) }
            )
        }

        return listOf(groupReportRow)
    }

    internal data class IssueGroup(val groupValue: String, val issues: List<Issue>) {
        val subgroups : MutableList<IssueGroup> = mutableListOf()
    }

    override fun toString(): String {
        return "Groupings { groupings = $groupings }"
    }
}