package de.pbauerochse.worklogviewer.fx

import javafx.beans.WeakListener
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import java.lang.ref.WeakReference

object CollectionBindings {

    fun <F,T> bind(source: ObservableList<F>, target: ObservableList<T>, transform: (F) -> T) {
        val transformListListener = TransformListListener(target, transform)
        source.removeListener(transformListListener)
        source.addListener(transformListListener)
    }

    /**
     * Taken from [com.sun.javafx.binding.ContentBinding#bind]
     */
    internal class TransformListListener<F,T>(
        target: MutableList<T>,
        private val transform: (F) -> T
    ) : ListChangeListener<F>, WeakListener {

        private val targetListRef: WeakReference<MutableList<T>> = WeakReference(target)

        override fun onChanged(c: ListChangeListener.Change<out F>) {
            val targetList = targetListRef.get()
            if (targetList == null) {
                c.list.removeListener(this)
            } else {
                while (c.next()) {
                    if (c.wasPermutated() || c.wasRemoved()) {
                        targetList.subList(c.from, c.to).clear()
                        targetList.addAll(c.from, c.list.subList(c.from, c.to).map { transform.invoke(it) })
                    }

                    if (c.wasPermutated().not() && c.wasAdded()) {
                        targetList.addAll(c.from, c.addedSubList.map { transform.invoke(it) })
                    }
                }
            }
        }

        override fun wasGarbageCollected(): Boolean = targetListRef.get() == null
    }


}