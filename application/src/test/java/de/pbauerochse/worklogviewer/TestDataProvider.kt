package de.pbauerochse.worklogviewer

import de.pbauerochse.worklogviewer.report.Field
import de.pbauerochse.worklogviewer.report.Issue
import de.pbauerochse.worklogviewer.report.User
import de.pbauerochse.worklogviewer.report.WorklogItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

/**
 * Contains example data that can be used in Tests
 */
object TestDataProvider {

    val self = User("me", "Myself")

    val projects: List<String> = listOf("PROJECT_1", "PROJECT_2", "PROJECT_3")

    val users: List<User> = listOf(
        self,
        User("alice", "Alice"),
        User("bob", "Bob"),
        User("charlie", "Charlie")
    )

    val issues: List<Issue> = projects.flatMap { createIssuesForProject(it) }

    private fun createIssuesForProject(project: String): Iterable<Issue> {
        return (1..30).map {
            val issueNumber = it + 1000
            val issueFields = getFieldsForIssue(issueNumber)
            val issueResolutionDate: LocalDateTime? = if (issueNumber.rem(2) == 0) LocalDateTime.of(2020, Month.JANUARY, 1, 15, 0,0).plusDays(it.toLong()) else null
            Issue(
                id = "${project}-$issueNumber",
                summary = "This is Issue $issueNumber of Project $project",
                description = """
                    This is the whole description of Issue $issueNumber
                    for the project $project
                """.trimIndent(),
                fields = issueFields,
                resolutionDate = issueResolutionDate
            ).apply {  addWorklogItems(issueNumber, this) }
        }
    }

    private fun getFieldsForIssue(issueNumber: Int): List<Field> {
        val remainder = issueNumber.rem(15)

        return listOf(
            Field("Criteria A", listOf("Value $remainder")),
            Field("Criteria B", listOf("Value $remainder", "Another Value $remainder")),
            Field("Criteria C", listOf("Another Value $remainder", "Yet another value $remainder")),
            Field("Criteria D", listOf()),
            Field("Criteria E", listOf("This is the value $remainder")),
            Field("Criteria F", listOf("Value $remainder")),
            Field("Criteria G", listOf("Got a value for you here $remainder")),
            Field("Criteria H", listOf("Value $remainder"))
        )
    }

    private fun addWorklogItems(issueNumber: Int, issue: Issue) {
        users.forEach { user ->
            val days = issueNumber.rem(10)
            (0..days).map { day ->
                val date = LocalDate.of(2020, Month.JANUARY, 1).plusDays(day.toLong())
                val duration = issueNumber.rem(3)
                val workType = days.takeIf { it.rem(3) == 0 }?.let { "Worktype $it" }
                val workItem = WorklogItem(issue, user, date, duration.toLong(), "Description", workType)
                issue.worklogItems.add(workItem)
            }
        }
    }

}