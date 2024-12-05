// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.google.sherlock.newProject

import com.intellij.ide.util.projectWizard.AbstractNewProjectStep
import com.intellij.ide.util.projectWizard.ProjectSettingsStepBase
import com.intellij.openapi.ui.VerticalFlowLayout
import javax.swing.JButton
import javax.swing.JPanel

/**
 * A wizard-enabled project settings step that you should use for your {@link #getProjectGenerator() projectGenerator} in your
 * {@link AbstractNewProjectStep.Customization#createProjectSpecificSettingsStep(DirectoryProjectGenerator, AbstractNewProjectStep.AbstractCallback)}
 * to provide the project wizard UI and actions.
 *
 * @param <T> The type of project settings.
 */
class NewProjectWizardProjectSettingsStep<T : Any>(private val projectGenerator: NewProjectWizardDirectoryGeneratorAdapter<T>)
  : ProjectSettingsStepBase<T>(projectGenerator, null) {

  init {
    myCallback = AbstractNewProjectStep.AbstractCallback()
  }

  /**
   * Creates and fills the content panel for the project settings step.
   *
   * @return The content panel.
   */
  override fun createAndFillContentPanel(): JPanel =
    JPanel(VerticalFlowLayout()).apply {
      add(peer.component)
    }

  override fun registerValidators() {}

  /**
   * Returns the project location.
   *
   * @return The project location.
   */
  override fun getProjectLocation(): String =
    projectGenerator.panel.step.context.projectFileDirectory

  /**
   * Returns the action button for the project settings step.
   *
   * @return The action button.
   */
  override fun getActionButton(): JButton =
    super.getActionButton().apply {
      addActionListener {
        projectGenerator.panel.apply()
      }
    }
}