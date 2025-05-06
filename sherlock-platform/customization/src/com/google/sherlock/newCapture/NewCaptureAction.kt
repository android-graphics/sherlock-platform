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
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.util.ui.JBUI
import javax.swing.JButton
import javax.swing.JComponent

class NewCaptureAction : AnAction(), CustomComponentAction, DumbAware {
  override fun actionPerformed(e: AnActionEvent) {
    val project: Project? = e.project
    if (project == null) {
      Messages.showErrorDialog("A project must be open to start a new capture.", "No Project Open")
      return
    }

    Messages.showMessageDialog(
      project,
      "Placeholder: 'New Capture' action performed. Implement EP delegation.",
      e.presentation.text ?: "New Capture",
      Messages.getInformationIcon()
    )
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