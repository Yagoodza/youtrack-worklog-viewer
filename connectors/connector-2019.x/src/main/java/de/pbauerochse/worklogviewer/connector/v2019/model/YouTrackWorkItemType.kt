package de.pbauerochse.worklogviewer.connector.v2019.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class YouTrackWorkItemType @JsonCreator constructor(
    @JsonProperty("name") val name : String?
)
