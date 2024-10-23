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

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.project.Project
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBTabbedPane
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Show a dialog to let the user configure and start a recording.
 */
class StartRecordingDialog(val project: Project?): DialogWrapper(project) {

    init {
        super.init()
        setTitle("Configure and Start a Recording")
        setOKButtonText("Start Recording")
    }

    override fun createCenterPanel(): JComponent {
        val devicePane = JPanel(BorderLayout()).apply {
            add(ComboBox<String>(arrayOf("No connected device")), BorderLayout.CENTER)
            add(JButton("Refresh"), BorderLayout.EAST)
        }
        val configPane = JBTabbedPane().apply {
            add("System Profiler", JPanel())
            add("Frame Profiler", JPanel())
        }
        return JPanel(BorderLayout()).apply {
            preferredSize = JBUI.size(400, 300)
            add(devicePane, BorderLayout.NORTH)
            add(configPane, BorderLayout.CENTER)
        }
    }

    override fun doOKAction() {
        super.doOKAction()
        StopRecordingDialog(project).show()
    }
}