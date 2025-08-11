// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.xdebugger.impl.ui.visualizedtext

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.diagnostic.Attachment
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.DimensionService
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.AppUIUtil
import com.intellij.ui.ScreenUtil
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.JBUI
import com.intellij.xdebugger.frame.XFullValueEvaluator
import com.intellij.xdebugger.impl.ui.*
import com.intellij.xdebugger.ui.TextValueVisualizer
import com.intellij.xdebugger.ui.VisualizedContentTab
import java.awt.CardLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.Rectangle
import java.awt.event.MouseEvent
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JComponent
import javax.swing.JPanel
import kotlin.math.max

/**
 * Provides tools to show a text-like value that might be formatted for better readability (JSON, XML, HTML, etc.).
 */
internal object VisualizedTextPopup {

  private const val SELECTED_TAB_KEY_PREFIX = "DEBUGGER_VISUALIZED_TEXT_SELECTED_TAB#"

  private val LOG = Logger.getInstance(VisualizedTextPopup.javaClass)

  private val extensionPoint: ExtensionPointName<TextValueVisualizer> =
    ExtensionPointName.create("com.intellij.xdebugger.textValueVisualizer")

  fun showValuePopup(event: MouseEvent, project: Project, editor: Editor?, component: JComponent, cancelCallback: Runnable?) {
    var size = DimensionService.getInstance().getSize(DebuggerUIUtil.FULL_VALUE_POPUP_DIMENSION_KEY, project)
    if (size == null) {
      val frameSize = WindowManager.getInstance().getFrame(project)!!.size
      size = Dimension(frameSize.width / 2, frameSize.height / 2)
    }

    component.preferredSize = size

    val popup = DebuggerUIUtil.createValuePopup(project, component, cancelCallback)
    if (editor == null) {
      val bounds = Rectangle(event.locationOnScreen, size)
      ScreenUtil.fitToScreenVertical(bounds, 5, 5, true)
      if (size.width != bounds.width || size.height != bounds.height) {
        size = bounds.size
        component.preferredSize = size
      }
      popup.showInScreenCoordinates(event.component, bounds.location)
    }
    else {
      popup.showInBestPositionFor(editor)
    }
  }

  fun evaluateAndShowValuePopup(evaluator: XFullValueEvaluator, event: MouseEvent, project: Project, editor: Editor?) {
    if (evaluator is CustomComponentEvaluator) {
      return evaluator.show(event, project, editor)
    }

    val panel = TextPanel(project)
    val callback = EvaluationCallback(project, panel)
    showValuePopup(event, project, editor, panel, callback::setObsolete)
    evaluator.startEvaluation(callback) // to make it really cancellable
  }

  private class TextPanel(private val project: Project) : JPanel(CardLayout()) {
    init {
      showOnlyText(XDebuggerUIConstants.getEvaluatingExpressionMessage())
    }

    fun showOnlyText(value: String, format: (TextViewer) -> Unit = {}) {
      val textArea = DebuggerUIUtil.createTextViewer(value, project)
      format(textArea)

      removeAll()
      add(textArea)
      revalidate()
      repaint()
    }

  }

  private class EvaluationCallback(private val project: Project, private val panel: TextPanel) : XFullValueEvaluator.XFullValueEvaluationCallback {
    private val obsolete = AtomicBoolean(false)

    private var lastFullValueHashCode: Int? = null

    override fun evaluated(fullValue: String, font: Font?) {
      // This code is not expected to be called multiple times, but it is actually called in the case of huge Java string.
      // 1. NodeDescriptorImpl.updateRepresentation() calls ValueDescriptorImpl.calcRepresentation() and it calls labelChanged()
      // 2. NodeDescriptorImpl.updateRepresentation() also directly calls labelChanged()
      // Double visualization spoils statistics and wastes the resources.
      // Try to prevent it by a simple hash code check.
      if (fullValue.hashCode() == lastFullValueHashCode) return
      lastFullValueHashCode = fullValue.hashCode()

      AppUIUtil.invokeOnEdt {
        try {
          panel.removeAll()
          panel.add(createComponent(fullValue))
          panel.revalidate()
          panel.repaint()
        }
        catch (e: Exception) {
          errorOccurred(e.toString())
        }
      }
    }

    override fun errorOccurred(errorMessage: String) {
      AppUIUtil.invokeOnEdt {
        panel.showOnlyText(errorMessage) {
          it.foreground = XDebuggerUIConstants.ERROR_MESSAGE_ATTRIBUTES.fgColor
        }
      }
    }

    fun setObsolete() {
      obsolete.set(true)
    }

    override fun isObsolete(): Boolean {
      return obsolete.get()
    }

    private fun createComponent(fullValue: String): JComponent {
      val tabs = collectVisualizedTabs(fullValue)
      assert(tabs.isNotEmpty()) { "at least one raw tab is expected to be provided by fallback visualizer" }
      if (tabs.size > 1) {
        try {
          return createTabbedPane(tabs, fullValue)
        }
        catch (e: Exception) {
          LOG.error("failed to visualize value", e, Attachment("value.txt", fullValue))
          // Fallback to the default visualizer, which provided the last tab.
        }
      }

      return tabs.last()
        .also { it.onShown(project, firstTime = true) }
        .createComponent(project)
    }

    private fun createTabbedPane(tabs: List<VisualizedContentTab>, fullValue: String): JComponent {
      assert(tabs.isNotEmpty())

      val panel = JBTabbedPane()
      panel.tabComponentInsets = JBUI.emptyInsets()

      for (tab in tabs) {
        val component = try {
          tab.createComponent(project)
        }
        catch (e: Throwable) {
          // It's not easy to recover after missing a tab, so we throw and catch above.
          throw Exception("failed to create visualized component (${tab.id})", e)
        }
        panel.addTab(tab.name, component)
      }

      // We try to make it content-specific by remembering separate value for every set of tabs.
      // E.g., it allows remembering that in the group HTML+XML+RAW user prefers HTML, and in the group HTML+MARKDOWN+RAW -- MARKDOWN.
      val selectedTabKey = SELECTED_TAB_KEY_PREFIX + tabs.map { it.id }.sorted().joinToString("#")

      val alreadyShownTabs = mutableSetOf<VisualizedContentTab>()
      fun onTabShown() {
        val idx = panel.selectedIndex
        if (idx < 0 || idx >= tabs.size) return

        val selectedTab = tabs[idx]
        PropertiesComponent.getInstance().setValue(selectedTabKey, selectedTab.id)

        val firstTime = alreadyShownTabs.add(selectedTab)
        selectedTab.onShown(project, firstTime)
      }

      val savedSelectedTabId = PropertiesComponent.getInstance().getValue(selectedTabKey)
      val selectedIndex = max(0, tabs.indexOfFirst { it.id == savedSelectedTabId })
      panel.selectedIndex = selectedIndex
      onTabShown() // call it manually, because change listener is triggered only if selectedIndex > 0

      panel.model.addChangeListener { onTabShown() }

      return panel
    }
  }

  private fun collectVisualizedTabs(fullValue: String): List<VisualizedContentTab> {
    return extensionPoint.extensionList
             .flatMap { viz ->
               try {
                 viz.visualize(fullValue)
               }
               catch (t: Throwable) {
                 LOG.error("failed to visualize value ($viz)", t, Attachment("value.txt", fullValue))
                 emptyList()
               }
             } +
           // Explicitly add the fallback raw visualizer to make it the last one.
           FallbackTextVisualizer.visualize(fullValue)
  }

  fun isVisualizable(fullValue: String): Boolean {
    // text with line breaks would be nicely rendered by the raw visualizer
    return StringUtil.containsLineBreak(fullValue) ||
           extensionPoint.extensionList
             .any { viz ->
               try {
                 viz.canVisualize(fullValue)
               }
               catch (t: Throwable) {
                 LOG.error("failed to check visualization of value ($viz)", t, Attachment("value.txt", fullValue))
                 false
               }
             }
  }

}