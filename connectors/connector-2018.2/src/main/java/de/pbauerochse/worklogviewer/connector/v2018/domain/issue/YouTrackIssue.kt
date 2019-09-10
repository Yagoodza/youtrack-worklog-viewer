package de.pbauerochse.worklogviewer.connector.v2018.domain.issue

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import de.pbauerochse.worklogviewer.toLocalDateTimeUsingUserTimeZone
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class YouTrackIssue @JsonCreator constructor(
    @JsonProperty("id") val id: String,
    @JsonProperty("field") val fields: List<IssueField>
) {

    val description = fields.find { it.name == "summary" }?.textValue ?: ""
    val resolutionDate: LocalDateTime? = fields.find { it.name == "resolved" }?.textValue?.toLong()?.toLocalDateTimeUsingUserTimeZone()

}