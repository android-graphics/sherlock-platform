/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sherlockPlugin.workflow

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * Show a dialog to confirm the recording is in progress and let the user stop it.
 */
class StopRecordingDialog(val project: Project?): DialogWrapper(project) {

    init {
        super.init()
        setTitle("Recording in progress")
    }

    override fun createCenterPanel(): JComponent {
        val messageLabel = JLabel("Recording in progress...")
        return JPanel(BorderLayout()).apply {
            preferredSize = JBUI.size(300, 100)
            add(messageLabel, BorderLayout.CENTER)
        }
    }

    override fun createSouthPanel() =
        JPanel(FlowLayout(FlowLayout.CENTER)).apply { add(JButton(okAction).apply { text = "Stop" }) }

    override fun doOKAction() {
        super.doOKAction()
        // TODO(shukang): Start a thread to retrieve the trace and open it.
    }


}