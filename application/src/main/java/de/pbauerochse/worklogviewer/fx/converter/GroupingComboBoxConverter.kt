package de.pbauerochse.worklogviewer.fx.converter

import de.pbauerochse.worklogviewer.util.FormattingUtil.getFormatted
import de.pbauerochse.worklogviewer.view.grouping.Grouping
import javafx.collections.ObservableList
import javafx.util.StringConverter

class GroupingComboBoxConverter(private val items : ObservableList<Grouping>) : StringConverter<Grouping>() {

    override fun toString(category: Grouping?): String {
        return category?.label ?: getFormatted("grouping.none")
    }

    override fun fromString(categoryName: String?): Grouping? {
        // special "nothing-selected" item
        return if (getFormatted("grouping.none") == categoryName) {
            null
        } else items.firstOrNull { it.label == categoryName }
    }
}
