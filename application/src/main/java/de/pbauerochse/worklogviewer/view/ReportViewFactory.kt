package de.pbauerochse.worklogviewer.view

import de.pbauerochse.worklogviewer.report.Issue
import de.pbauerochse.worklogviewer.report.TimeReportParameters
import de.pbauerochse.worklogviewer.report.view.ReportRow
import de.pbauerochse.worklogviewer.report.view.ReportView
import de.pbauerochse.worklogviewer.view.grouping.Groupings
import org.slf4j.LoggerFactory
import java.text.Collator
import java.util.*
import kotlin.Comparator

/**
 * Converts a list of [Issue]s to a [ReportView].
 * A view is the denormalized representation of the data provided in the TimeReport
 * which is suitable to be displayed in a tabled manner (Excel Export / TableView)
 */
object ReportViewFactory {

    private val LOGGER = LoggerFactory.getLogger(ReportViewFactory::class.java)

    private val COLLATOR = Collator.getInstance(Locale.getDefault())
    private val REPORT_ROW_COMPARATOR = Comparator<ReportRow> { o1, o2 ->
        return@Comparator COLLATOR.compare(o1.label, o2.label)
    }

    fun convert(issues: List<Issue>, reportParameters: TimeReportParameters, groupings: Groupings): ReportView {
        LOGGER.debug("Converting ${issues.size} Issues for ${reportParameters.timerange} with grouping $groupings to ReportView")

        val groups = groupings.rows(issues).sortedWith(REPORT_ROW_COMPARATOR)
        val summaryReportRow = SummaryReportRow(issues)
        return ReportView(groups + summaryReportRow, issues, reportParameters)
    }
}