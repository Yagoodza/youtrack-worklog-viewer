package de.pbauerochse.worklogviewer.fx.groupings

import de.pbauerochse.worklogviewer.fx.converter.GroupingComboBoxConverter
import de.pbauerochse.worklogviewer.util.FormattingUtil
import de.pbauerochse.worklogviewer.view.grouping.Grouping
import javafx.beans.property.ListProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.layout.HBox
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.*

class GroupingSelector : HBox(), Initializable {

    val possibleGroupingsProperty: ListProperty<Grouping> = SimpleListProperty()
    val selectedGroupingProperty: ObjectProperty<Grouping> = SimpleObjectProperty()

    lateinit var groupingComboBox: ComboBox<Grouping>
    lateinit var removeGroupingButton: Button

    init {
        FXMLLoader(GroupingSelector::class.java.getResource("/fx/components/grouping-selector.fxml"), FormattingUtil.RESOURCE_BUNDLE).apply {
            setRoot(this@GroupingSelector)
            setController(this@GroupingSelector)
            load<Parent>()
        }
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        LOGGER.debug("Initializing")
        groupingComboBox.converter = GroupingComboBoxConverter(possibleGroupingsProperty)
        groupingComboBox.itemsProperty().bind(possibleGroupingsProperty)

        selectedGroupingProperty.bind(groupingComboBox.valueProperty())
    }

    internal fun setSelectedGrouping(selectedGroupingId: String?) {
        if (selectedGroupingId != null) {
            groupingComboBox.valueProperty().value = possibleGroupingsProperty.find { it.id == selectedGroupingId }
        }
    }

    fun unbind() {
        groupingComboBox.itemsProperty().unbind()
        groupingComboBox.valueProperty().unbind()
        selectedGroupingProperty.unbind()
        possibleGroupingsProperty.unbind()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(GroupingSelector::class.java)
    }
}