/*
 * Copyright 2025 Google LLC
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.sherlock.newCapture

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.util.ui.JBUI
import javax.swing.JButton
import javax.swing.JComponent

class NewCaptureAction : AnAction(), CustomComponentAction, DumbAware {

  companion object {
    val EP_NAME = ExtensionPointName.create<NewCapturePerformer>("com.google.sherlock.customization.newCapturePerformer")
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project: Project? = e.project
    if (project == null) {
      Messages.showErrorDialog("A project must be open to start a new capture.", "No Project Open")
      return
    }

    // Find registered implementations of NewCapturePerformer
    val performers = EP_NAME.extensionList
    if (performers.isEmpty()) {
      Messages.showMessageDialog(
        project,
        "Capture functionality needs to be provided by an implementing plugin. (No 'NewCapturePerformer' extension found for: ${EP_NAME.name})).",
        "Capture Implementation Required",
        Messages.getInformationIcon()
      )
      return
    }

    val performer = performers.first()
    try {
      performer.performCapture(project)
    }
    catch (ex: Exception) {
      Messages.showErrorDialog(
        project,
        "An error occurred while starting the capture: ${ex.message}",
        "Capture Error"
      )
      ex.printStackTrace()
    }
  }

  override fun update(e: AnActionEvent) {
    e.presentation.text = "New Capture"
    e.presentation.description = "Starts a new capture."
    e.presentation.isEnabledAndVisible = e.project != null
  }

  override fun getActionUpdateThread() = ActionUpdateThread.BGT

  override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
    val button = JButton(presentation.text)
    button.isFocusable = false

    button.putClientProperty("JButton.buttonType", "toolbar")
    button.putClientProperty("gotItButton", true)

    button.border = JBUI.Borders.empty(JBUI.scale(5), JBUI.scale(10))
    button.isOpaque = false

    button.addActionListener {
      val dataContext = DataManager.getInstance().getDataContext(button)
      val actionManager = ActionManager.getInstance()
      val clickEvent = AnActionEvent.createFromAnAction(
        this,
        null,
        place,
        dataContext
      )

      actionManager.tryToExecute(this, clickEvent.inputEvent, button, clickEvent.place, true)
    }
    return button
  }
}