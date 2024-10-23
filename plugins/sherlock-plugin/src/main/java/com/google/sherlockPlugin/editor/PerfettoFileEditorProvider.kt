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

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.NonNls

/**
 * Registers the custom `PerfettoFileEditor` as the handler for Perfetto trace files, ensuring they are opened with the
 * specialized visualization instead of the default editor.
 */
class PerfettoFileEditorProvider : FileEditorProvider, DumbAware {

    // A collection of possible extensions for Perfetto-based trace files (aggregation of extensions used in
    // Android Studio Profiler and Android Graphics Inspector).
    private val PERFETTO_FILE_EXTENSIONS = listOf("perfetto", "trace", "pftrace", "perfetto-trace")

    override fun accept(project: Project, file: VirtualFile): Boolean {
        // Only handles files with specified Perfetto-indicating extensions.
        return PERFETTO_FILE_EXTENSIONS.contains(file.extension)
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return PerfettoFileEditor(file)
    }

    override fun getPolicy(): FileEditorPolicy {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR
    }

    override fun getEditorTypeId(): String {
        return TYPE_ID
    }

    companion object {
        @NonNls
        private const val TYPE_ID = "perfetto"
    }
}