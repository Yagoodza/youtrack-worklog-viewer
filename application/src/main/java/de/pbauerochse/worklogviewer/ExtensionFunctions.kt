package de.pbauerochse.worklogviewer

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.control.Hyperlink
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Kotlin convenience extension functions
 */
fun Long.toLocalDate() : LocalDate = toZonedDateTime().toLocalDate()
fun Long.toZonedDateTime() : ZonedDateTime = ZonedDateTime
    .ofInstant(Instant.ofEpochMilli(this), ZoneId.of("UTC"))

fun String.trimToNull() : String? {
    val trimmed = trim()
    return if (trimmed.isNotBlank()) {
        trimmed
    } else {
        null
    }
}

fun String.toLocalDate() : LocalDate? = LocalDate.parse(this, DateTimeFormatter.ISO_DATE)
fun LocalDate.toFormattedString() : String = format(DateTimeFormatter.ISO_DATE)

fun Hyperlink.setHref(url: String) {
    onAction = EventHandler { _ -> Platform.runLater { WorklogViewer.getInstance().hostServices.showDocument(url) } }
}