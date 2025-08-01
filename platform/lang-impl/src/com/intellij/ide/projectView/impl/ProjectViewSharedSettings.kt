// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.ide.projectView.impl

import com.intellij.ide.projectView.NodeSortKey
import com.intellij.ide.projectView.ProjectViewSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ReportValue
import com.intellij.openapi.components.SettingsCategory
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * @author Konstantin Bulenkov
 */
@State(name = "ProjectViewSharedSettings", storages = [(Storage(value = "projectView.xml"))], category = SettingsCategory.UI)
class ProjectViewSharedSettings : PersistentStateComponent<ProjectViewSharedSettings> {
  var flattenPackages: Boolean = false
  var showMembers: Boolean = false
  @ReportValue
  var sortKey: NodeSortKey = ProjectViewSettings.Immutable.DEFAULT.sortKey
  var showModules: Boolean = true
  var flattenModules: Boolean = false
  // Sherlock: Default behavior is to not show excluded files.
  var showExcludedFiles: Boolean = false
  // Sherlock: Default behavior is to not show excluded files.
  var showVisibilityIcons: Boolean = false
  var showLibraryContents: Boolean = true
  var showScratchesAndConsoles: Boolean = true
  var hideEmptyPackages: Boolean = true
  var compactDirectories: Boolean = false
  var abbreviatePackages: Boolean = false
  var autoscrollFromSource: Boolean = false
  var autoscrollToSource: Boolean = false
  var openDirectoriesWithSingleClick: Boolean = false
  var foldersAlwaysOnTop: Boolean = true
  var manualOrder: Boolean = false

  override fun getState(): ProjectViewSharedSettings {
    return this
  }

  override fun loadState(state: ProjectViewSharedSettings) {
    XmlSerializerUtil.copyBean(state, this)
  }

  companion object {
    val instance: ProjectViewSharedSettings
      get() = ApplicationManager.getApplication().getService(ProjectViewSharedSettings::class.java)
  }
}
