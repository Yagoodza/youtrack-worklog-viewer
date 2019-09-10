package de.pbauerochse.worklogviewer.fx.components.treetable

import de.pbauerochse.worklogviewer.report.Issue

/**
 * A row in the [WorklogsTreeTableView] containing
 * the worlogs for a certain [Issue]
 */
data class IssueTreeTableRow(val issue: Issue) : TreeTableRowModel {

    override val isSummaryRow: Boolean = false
    override val isIssueRow: Boolean = true
    override val isGroupByRow: Boolean = false
    override fun getLabel(): String = issue.fullTitle

    override fun getTotalTimeSpent(): Long = issue.getTotalTimeInMinutes()
}