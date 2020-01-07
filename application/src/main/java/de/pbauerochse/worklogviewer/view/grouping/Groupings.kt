package de.pbauerochse.worklogviewer.view.grouping

import de.pbauerochse.worklogviewer.report.Issue
import de.pbauerochse.worklogviewer.report.view.ReportRow
import de.pbauerochse.worklogviewer.view.GroupReportRow
import de.pbauerochse.worklogviewer.view.IssueReportRow

class Groupings(private val groupings: List<Grouping>) {

    /**
     * Groups the passed Issues by the defined Grouping criteria
     * and returns a tree structure of the grouped issues.
     */
    fun treeRoot(issues: List<Issue>): IssueGroupTreeNode {
        val rootGroup = IssueGroupTreeNode("root", issues)

        val inputGroups = mutableListOf(rootGroup)
        groupings.forEach { grouping ->
            val currentPassGroups = mutableListOf<IssueGroupTreeNode>()
            inputGroups.forEach { inputGroup ->
                val subGroups = extractIssueGroups(inputGroup, grouping)
                inputGroup.subgroups.addAll(subGroups)
                currentPassGroups.addAll(subGroups)
            }

            inputGroups.clear()
            inputGroups.addAll(currentPassGroups)
        }

        return rootGroup
    }

    /**
     * Converts the given issues to ReportRows
     * by applying any necessary grouping criteria
     */
    fun rows(issues: List<Issue>): List<ReportRow> {
        return if (groupings.isNotEmpty()) {
            val rootGroup = treeRoot(issues)
            rootGroup.subgroups.flatMap { createReportRows(it) }
        } else issues.map { IssueReportRow(it) }
    }

    private fun extractIssueGroups(parent: IssueGroupTreeNode, grouping: Grouping): List<IssueGroupTreeNode> {
        val groupedIssues = grouping.group(parent.issues)
        return groupedIssues.map { IssueGroupTreeNode(it.key, it.value) }
    }

    private fun createReportRows(groupTreeNode: IssueGroupTreeNode): List<ReportRow> {
        val childGroups = mutableListOf<ReportRow>()

        val groupReportRow = GroupReportRow(groupTreeNode.groupValue, childGroups)

        if (groupTreeNode.subgroups.isEmpty()) {
            childGroups.addAll(groupTreeNode.issues.map { issue -> IssueReportRow(issue) }.toMutableList())
        } else {
            childGroups.addAll(groupTreeNode.subgroups.flatMap { createReportRows(it) })
        }

        return listOf(groupReportRow)
    }

    data class IssueGroupTreeNode(val groupValue: String, val issues: List<Issue>) {
        val subgroups : MutableList<IssueGroupTreeNode> = mutableListOf()
    }

    override fun toString(): String {
        return "Groupings { groupings = $groupings }"
    }
}