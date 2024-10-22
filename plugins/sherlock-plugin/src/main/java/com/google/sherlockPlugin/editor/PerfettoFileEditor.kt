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

package com.google.sherlockPlugin.editor

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.Nls
import java.awt.BorderLayout
import java.beans.PropertyChangeListener
import java.nio.file.Path
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * Customizes the display of Perfetto trace file content within an IDE editor tab.
 *
 * While currently showing a placeholder, this `FileEditor` is designed to eventually provide a specialized interface
 * for visualizing and interacting with the performance data captured in Perfetto traces.
 */
class PerfettoFileEditor(private val file: VirtualFile) : FileEditor {

    private var panel: JPanel? = null

    override fun getFile(): VirtualFile {
        return file
    }

    override fun getComponent(): JComponent {
        ApplicationManager.getApplication().assertIsDispatchThread()
        if (panel == null) {
            panel = PlaceholderPanel()
        }
        return panel as JPanel
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getName(): String {
        return file.name
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return null
    }

    override fun isModified(): Boolean {
        return false
    }

    override fun isValid(): Boolean {
        return true
    }

    override fun setState(p0: FileEditorState) {}


    override fun <T : Any?> getUserData(p0: Key<T>): T? {
        return null
    }

    override fun <T : Any?> putUserData(p0: Key<T>, p1: T?) {}

    override fun dispose() {}

    override fun addPropertyChangeListener(p0: PropertyChangeListener) {}

    override fun removePropertyChangeListener(p0: PropertyChangeListener) {}

    companion object {
        val LOG: Logger = Logger.getInstance(PerfettoFileEditor::class.java)
    }

    // TODO(ydbeis): Replace with LoadablePanel, which contains the trace view.
    private class PlaceholderPanel : JPanel(BorderLayout()) {
        init {
            add(JLabel("This is a placeholder..."), BorderLayout.NORTH)

            val pluginPath = PathManager.getPluginsPath()
            val backend = Path.of(pluginPath, "sherlock/lib/main")
            if (!backend.toFile().isFile()) {
                LOG.error("Cannot find backend executable")
            }
        }
    }
}