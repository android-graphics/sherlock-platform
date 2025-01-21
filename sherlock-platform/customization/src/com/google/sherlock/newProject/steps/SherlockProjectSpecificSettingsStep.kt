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
package com.google.sherlock.newProject.steps

import com.google.sherlock.newProject.SherlockBundle.message
import com.google.sherlock.newProject.SherlockEmptyProjectGenerator
import com.intellij.ide.util.projectWizard.AbstractNewProjectStep
import com.intellij.ide.util.projectWizard.ProjectSettingsStepBase
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.observable.util.joinCanonicalPath
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.io.FileUtil
import com.intellij.platform.DirectoryProjectGenerator
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import java.nio.file.InvalidPathException
import java.nio.file.Path
import javax.swing.JPanel

/**
 * Handles the creation of a Sherlock project through the UI.
 *
 * Takes project name and location as input, validates it, and generates the project.
 * It can be further modified to enable SDK installation, create welcome scripts, etc.
 *
 * @param <T> The type of project settings.
 */
class SherlockProjectSpecificSettingsStep<T>(
  projectGenerator: DirectoryProjectGenerator<T>,
  callback: AbstractNewProjectStep.AbstractCallback<T>,
)
  : ProjectSettingsStepBase<T>(projectGenerator, callback), DumbAware {

  init {
    // Throw an IllegalArgumentException if the projectGenerator is not of the expected type
    require(projectGenerator is SherlockEmptyProjectGenerator<*>) {
      "Invalid project generator type. Expected SherlockEmptyProjectGenerator, but found ${projectGenerator::class.simpleName}"
    }
  }

  private val propertyGraph = PropertyGraph()
  private val projectName = propertyGraph.property("")
  private val projectLocation = propertyGraph.property("")
  private val locationHint = propertyGraph.property("").apply {
    dependsOn(projectName, ::updateHint)
    dependsOn(projectLocation, ::updateHint)
  }

  private lateinit var projectNameFiled: JBTextField
  lateinit var mainPanel: DialogPanel
  /**
   * Creates and fills the content panel for the project settings step.
   *
   * @return The content panel.
   */
  override fun createAndFillContentPanel(): JPanel {
    return createContentPanelWithAdvancedSettingsPanel()
  }

  /**
   * Returns the project location constructed using two parts (using the values from "Location" and "Name" fields).
   *
   * @return The project location.
   */
  override fun getProjectLocation() =
      FileUtil.expandUserHome(projectLocation.joinCanonicalPath(projectName).get())

  /**
   * Creates the base panel for the project settings step.
   *
   * @return The base panel.
   */
  override fun createBasePanel(): JPanel {
    val projectGenerator = myProjectGenerator as SherlockEmptyProjectGenerator<*>
    val nextProjectDir = myProjectDirectory.get()
    projectName.set(nextProjectDir.nameWithoutExtension)
    projectLocation.set(nextProjectDir.parent)

    mainPanel = panel {
      row(message("new.project.name")) {
        projectNameFiled = textField()
          .bindText(projectName)
          .validationOnInput {
            val validationResult = projectGenerator.validate(getProjectLocation())
            if (validationResult.isOk) null else error(validationResult.errorMessage)
          }
          .component
      }
      row(message("new.project.location")) {
        myLocationField = textFieldWithBrowseButton(fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor())
          .bindText(projectLocation)
          .align(Align.FILL)
          .component
      }
      row("") {
        comment("", maxLineLength = 60).bindText(locationHint)
      }
      //TODO: Add UI customizer here
    }

    mainPanel.registerValidators(this) { validations ->
      val anyErrors = validations.entries.any { (key, value) -> key.isVisible && !value.okEnabled }
      myCreateButton.isEnabled = !anyErrors
    }
    myCreateButton.addActionListener { mainPanel.apply() }
    return mainPanel
  }

  /**
   * Updates the location hint based on the project name and location.
   *
   * @return The updated location hint.
   */
  private fun updateHint(): String =
    try {
      val projectPath = Path.of(projectLocation.get(), projectName.get())
      message("new.project.location.hint", projectPath)
    }
    catch (e: InvalidPathException) {
      ""
    }

  /**
   * Checks if the project settings are valid.
   *
   * @return {@code true} if the settings are valid, {@code false} otherwise.
   */
  override fun checkValid(): Boolean {
    //TODO: Add proper validation eventually.
    return true
  }

  /**
   * Registers validators for the project settings.
   */
  override fun registerValidators() {
    projectName.afterChange { (myProjectGenerator as SherlockEmptyProjectGenerator<*>).locationChanged(it) }
  }

}