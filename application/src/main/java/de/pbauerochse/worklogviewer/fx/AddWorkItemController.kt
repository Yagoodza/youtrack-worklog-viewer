package de.pbauerochse.worklogviewer.fx

import de.pbauerochse.worklogviewer.addWorkItem
import de.pbauerochse.worklogviewer.connector.workitem.AddWorkItemRequest
import de.pbauerochse.worklogviewer.connector.workitem.AddWorkItemResult
import de.pbauerochse.worklogviewer.connector.workitem.MinimalWorklogItem
import de.pbauerochse.worklogviewer.fx.state.ReportDataHolder
import de.pbauerochse.worklogviewer.fx.tasks.AddWorkItemTask
import de.pbauerochse.worklogviewer.fx.tasks.TaskRunnerImpl
import de.pbauerochse.worklogviewer.settings.SettingsUtil
import de.pbauerochse.worklogviewer.trimToNull
import de.pbauerochse.worklogviewer.util.WorklogTimeFormatter
import javafx.beans.property.*
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.WindowEvent
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.LocalDate
import java.util.*

/**
 * Controller for the view to add a work item
 * to an issue
 */
class AddWorkItemController : Initializable {

    val issueProperty: StringProperty = SimpleStringProperty()
    val dateProperty: ObjectProperty<LocalDate?> = SimpleObjectProperty()
    private val durationProperty: StringProperty = SimpleStringProperty()
    private val isValidWorkTimeProperty: BooleanProperty = SimpleBooleanProperty()

    @FXML
    private lateinit var saveButton: Button

    @FXML
    private lateinit var cancelButton: Button

    @FXML
    private lateinit var issueTextField: TextField

    @FXML
    private lateinit var workDateDatePicker: DatePicker

    @FXML
    private lateinit var workDurationTextField: TextField

    @FXML
    private lateinit var workDescriptionTextField: TextField

    @FXML
    private lateinit var progressIndicator: StackPane

    @FXML
    private lateinit var progressBarContainer: VBox

    @FXML
    private lateinit var errorLabel : Label

    private lateinit var taskRunner: TaskRunnerImpl

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        taskRunner = TaskRunnerImpl(progressBarContainer, progressIndicator, false)
        issueTextField.textProperty().bindBidirectional(issueProperty)
        workDateDatePicker.valueProperty().bindBidirectional(dateProperty)
        workDurationTextField.textProperty().bindBidirectional(durationProperty)
        workDurationTextField.textProperty().addListener { _, _, newDuration -> updateIsValidDurationProperty(newDuration) }
        errorLabel.visibleProperty().bind(errorLabel.textProperty().isNotEmpty)

        cancelButton.disableProperty().bind(progressIndicator.visibleProperty())
        saveButton.disableProperty().bind(
            progressIndicator.visibleProperty().or(durationProperty.isEmpty.or(isValidWorkTimeProperty.not()).or(issueProperty.isEmpty).or(dateProperty.isNull))
        )
    }

    fun closeDialog() {
        LOGGER.debug("Closing AddWorkItem dialogue")
        val window = progressBarContainer.scene.window
        window.fireEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST))
    }

    fun createNewWorkItem() {
        LOGGER.info("Trying to save new WorkItem")
        val request = AddWorkItemRequest(
            issueId = issueProperty.value,
            date = dateProperty.value!!,
            durationInMinutes = parseWorkTimeFromField(workDurationTextField.text)!!,
            description = workDescriptionTextField.text.trimToNull()
        )

        val task = AddWorkItemTask(request)
        task.onSucceeded = EventHandler { handleAddWorkItemResponse(it.source.value as AddWorkItemResult) }
        taskRunner.startTask(task)
    }

    private fun updateIsValidDurationProperty(newDuration: String?) {
        isValidWorkTimeProperty.value = newDuration.isNullOrBlank().not() && isValidWorkTime(newDuration!!)
    }

    private fun isValidWorkTime(newDuration: String): Boolean {
        val parseWorkTimeFromField = parseWorkTimeFromField(newDuration)
        return parseWorkTimeFromField != null
    }

    private fun parseWorkTimeFromField(newDuration: String) = WorklogTimeFormatter(SettingsUtil.settingsViewModel.workhoursProperty.value).parseDurationInMinutes(newDuration)

    private fun handleAddWorkItemResponse(addWorkItemResult: AddWorkItemResult) {
        if (addWorkItemResult.success) {
            updateCurrentTimeReport(addWorkItemResult.worklogItem!!)
        } else {
            showError(addWorkItemResult.errorMessage!!)
        }
    }

    private fun showError(errorMessage: String) {
        LOGGER.warn("Error while adding new worklog item")
        errorLabel.text = errorMessage
        errorLabel.tooltip = Tooltip(errorMessage)
    }

    private fun updateCurrentTimeReport(newWorkitem: MinimalWorklogItem) {
        LOGGER.debug("Adding Work Item was successful. Adding $newWorkitem to current rpeort")
        val currentTimeReport = ReportDataHolder.currentTimeReportProperty.value!!
        val newTimeReport = currentTimeReport.addWorkItem(newWorkitem)

        ReportDataHolder.currentTimeReportProperty.value = newTimeReport
        closeDialog()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(AddWorkItemController::class.java)
    }

}