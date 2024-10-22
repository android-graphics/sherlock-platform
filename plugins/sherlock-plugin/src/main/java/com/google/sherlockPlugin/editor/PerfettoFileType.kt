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

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.util.NlsContexts
import javax.swing.Icon

/**
 * Defines the file type for Perfetto trace files, providing metadata like name, description,and icon to be used in the
 * IDE file view. Also specifies characteristics of the file type, like binary format and read-only behavior.
 */
class PerfettoFileType : FileType {

    companion object {
        // Used for "fieldName" in "plugin.xml".
        @Suppress("UNUSED")
        val INSTANCE = PerfettoFileType()

        private const val DEFAULT_EXTENSION = "perfetto"
        private const val FILE_TYPE_NAME = "Perfetto Trace File"
        private const val FILE_TYPE_DESC = "Perfetto trace file"
    }

    override fun getName(): String {
        return FILE_TYPE_NAME
    }

    @NlsContexts.Label
    override fun getDescription(): String {
        return FILE_TYPE_DESC
    }

    override fun getDefaultExtension(): String {
        return DEFAULT_EXTENSION
    }

    override fun getIcon(): Icon {
        // TODO(ydbeis): Replace with Perfetto-indicating icon.
        return AllIcons.FileTypes.Any_type
    }

    override fun isBinary(): Boolean {
        return true
    }

    override fun isReadOnly(): Boolean {
        return true
    }
}