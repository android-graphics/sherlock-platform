/*
 * Copyright 2024 Google LLC
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
package com.google.sherlock.newProject

import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.ide.wizard.GeneratorNewProjectWizard
import com.intellij.ide.wizard.NewProjectWizardStepPanel
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.DirectoryProjectGeneratorBase
import com.intellij.platform.GeneratorPeerImpl
import com.intellij.platform.ProjectGeneratorPeer
import javax.swing.Icon
import javax.swing.JComponent

/**
 * Adapts a {@link GeneratorNewProjectWizard} to a {@link DirectoryProjectGeneratorBase}.
 *
 * This allows using the wizard UI and actions in the new project wizard.
 *
 * @param <T> The type of project settings.
 */
open class NewProjectWizardDirectoryGeneratorAdapter<T : Any>(val wizard: GeneratorNewProjectWizard) : DirectoryProjectGeneratorBase<T>() {
  internal lateinit var panel: NewProjectWizardStepPanel

  /**
   * Returns the name of the project generator.
   *
   * @return The name of the project generator.
   */
  @Suppress("DialogTitleCapitalization")
  override fun getName(): String = wizard.name

  /**
   * Returns the logo for the project generator.
   *
   * @return The logo for the project generator.
   */
  override fun getLogo(): Icon = wizard.icon

  /**
   * Generates the project.
   *
   * @param project  The project to generate.
   * @param baseDir  The base directory of the project.
   * @param settings The project settings.
   * @param module   The main module of the project.
   */
  override fun generateProject(project: Project, baseDir: VirtualFile, settings: T, module: Module) {
    panel.step.setupProject(project)
  }

  /**
   * Creates the peer for the project generator.
   *
   * @return The project generator peer.
   */
  override fun createPeer(): ProjectGeneratorPeer<T> {
    val context = WizardContext(null) {}
    return object : GeneratorPeerImpl<T>() {
      /**
       * Returns the component for the project generator peer.
       *
       * @return The component for the project generator peer.
       */
      override fun getComponent(): JComponent {
        panel = NewProjectWizardStepPanel(wizard.createStep(context))
        return panel.component
      }
    }
  }
}

