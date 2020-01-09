package de.pbauerochse.worklogviewer.fx.groupings

import de.pbauerochse.worklogviewer.report.TimeReport
import de.pbauerochse.worklogviewer.settings.SettingsViewModel
import de.pbauerochse.worklogviewer.util.FormattingUtil.RESOURCE_BUNDLE
import de.pbauerochse.worklogviewer.view.grouping.Grouping
import de.pbauerochse.worklogviewer.view.grouping.GroupingFactory
import de.pbauerochse.worklogviewer.view.grouping.Groupings
import javafx.beans.binding.Bindings
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.*

class GroupingView(
    private val settingsViewModel: SettingsViewModel,
    private val timereportProperty: ObjectProperty<TimeReport?>
) : HBox(), Initializable {

    private val groupingSelectors = FXCollections.observableArrayList<GroupingSelector>()
    private val currentlyAvailableGroupingsProperty = SimpleListProperty<Grouping>(FXCollections.observableArrayList())

    val groupingsProperty = SimpleObjectProperty<Groupings>()

    init {
        FXMLLoader(GroupingSelector::class.java.getResource("/fx/components/grouping-view.fxml"), RESOURCE_BUNDLE).apply {
            setRoot(this@GroupingView)
            setController(this@GroupingView)
            load<Parent>()
        }
    }

    @FXML
    private lateinit var addGroupingButton: Button

    @FXML
    private lateinit var groupingSelectorContainer: Pane

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        LOGGER.debug("Initializing")

        bindSelectorContainersToView()
        restoreGroupingContainersFromModel()
        initializeAddGroupingButton()

        bindReportChangeListeners()
    }

    @Suppress("UNCHECKED_CAST")
    private fun bindSelectorContainersToView() {
        // synchronize groupingsContainer with the groupings List
        Bindings.bindContentBidirectional(groupingSelectors as ObservableList<Node>, groupingSelectorContainer.children)
    }

    private fun restoreGroupingContainersFromModel() {
        repeat(settingsViewModel.lastUsedGroupByCategoryIdsProperty.size) { addGroupingSelector() }
    }

    private fun initializeAddGroupingButton() {
        /**
         * You may only add grouping criteria if all of these conditions are met:
         * - the current timereport is not null
         * - there are less than MAX_GROUPINGS criterias already defined
         * - there are no null grouping values present
         */
        val addingMoreCriteriaAllowed = timereportProperty.isNotNull
            .and(Bindings.size(groupingSelectors).lessThan(MAX_GROUPINGS))
            .and(
                // TODO actually we need to check the value of the property at index
                Bindings.valueAt(groupingSelectors, Bindings.size(groupingSelectors).subtract(1)).isNotNull
            )

        addGroupingButton.disableProperty().bind(addingMoreCriteriaAllowed.not())
        addGroupingButton.setOnAction { addGroupingSelector() }
    }

    private fun addGroupingSelector() {
        val selectedGroupingId = getSelectedGroupingId(groupingSelectors.size)
        val groupingSelector = GroupingSelector().apply {
            setSelectedGrouping(selectedGroupingId)
            disableProperty().bind(timereportProperty.isNull)
            possibleGroupingsProperty.bind(currentlyAvailableGroupingsProperty)
            removeGroupingButton.setOnAction { removeGroupingSelector(this) }
            selectedGroupingProperty.addListener { _, _, _ -> refreshGroupingsProperty() }
        }

        groupingSelectors.add(groupingSelector)
    }

    private fun removeGroupingSelector(groupingSelector: GroupingSelector) {
        groupingSelector.unbind()
        groupingSelectors.remove(groupingSelector)
        refreshGroupingsProperty()
    }

    private fun getSelectedGroupingId(index: Int): String? {
        return if (settingsViewModel.lastUsedGroupByCategoryIdsProperty.size > index) {
            settingsViewModel.lastUsedGroupByCategoryIdsProperty[index]
        } else null
    }

    private fun bindReportChangeListeners() {
        timereportProperty.addListener { _, _, timeReport -> timeReport?.let { onTimeReport(it) } }
    }

    private fun onTimeReport(timeReport: TimeReport) {
        val possibleGroupings = GroupingFactory.getAvailableGroupings(timeReport)
        currentlyAvailableGroupingsProperty.setAll(possibleGroupings)
        applySelectedGroupingsFromViewModel()
    }

    private fun applySelectedGroupingsFromViewModel() {
        LOGGER.debug("Updating selected groupings from viewModel ${settingsViewModel.lastUsedGroupByCategoryIdsProperty}")

        // to avoid concurrent modifications create a copy of the list first by calling .toList()
        settingsViewModel.lastUsedGroupByCategoryIdsProperty.toList().forEachIndexed { index, selectedGrouping ->
            groupingSelectors[index].setSelectedGrouping(selectedGrouping)
        }
        refreshGroupingsProperty()
    }

    private fun refreshGroupingsProperty() {
        val selectedGroupingsInOrder = groupingSelectors.map { it.selectedGroupingProperty.value }

        LOGGER.debug("Groupings changed to $selectedGroupingsInOrder")
        groupingsProperty.value = Groupings(selectedGroupingsInOrder)
        settingsViewModel.lastUsedGroupByCategoryIdsProperty.setAll(selectedGroupingsInOrder.map { it?.id })
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(GroupingView::class.java)
        private const val MAX_GROUPINGS = 3
    }
}
