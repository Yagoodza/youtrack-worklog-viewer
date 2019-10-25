package de.pbauerochse.worklogviewer.fx.components

import org.controlsfx.control.IndexedCheckModel

fun <T> IndexedCheckModel<T>.setCheckedItems(items : List<T>) {
    // uncheck all
    this.checkedItems.forEach { this.clearCheck(it) }

    // add checks in order
    items.forEach { check(it) }
}