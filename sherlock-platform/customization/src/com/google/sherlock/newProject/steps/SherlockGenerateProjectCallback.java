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
package com.google.sherlock.newProject.steps;

import com.google.sherlock.newProject.SherlockBundle;
import com.google.sherlock.newProject.SherlockEmptyProjectGenerator;
import com.intellij.ide.util.projectWizard.AbstractNewProjectStep;
import com.intellij.ide.util.projectWizard.ProjectSettingsStepBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.platform.DirectoryProjectGenerator;
import com.intellij.platform.ProjectGeneratorPeer;
import com.intellij.util.BooleanFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handles user input and project settings when creating a new Sherlock project.
 *
 * @param <T> The type of project settings.
 */
public class SherlockGenerateProjectCallback<T> extends AbstractNewProjectStep.AbstractCallback<T> {

  /**
   * Processes the project generation settings and creates the new project.
   *
   * @param step               The project settings step.
   * @param projectGeneratorPeer The project generator peer.
   */
  @Override
  public void consume(@Nullable ProjectSettingsStepBase<T> step, @NotNull ProjectGeneratorPeer<T> projectGeneratorPeer) {
    if (!(step instanceof SherlockProjectSpecificSettingsStep settingsStep)) return;
    //TODO: Update if we need to set WelcomeSettings before Project Generation.
    final DirectoryProjectGenerator generator = settingsStep.getProjectGenerator();
    if (!(generator instanceof SherlockEmptyProjectGenerator sherlockGenerator)) return;
    final @Nullable BooleanFunction
      beforeProjectGenerated = sherlockGenerator.beforeProjectGenerated();
    if (beforeProjectGenerated != null) {
      final boolean result = beforeProjectGenerated.fun(sherlockGenerator);
      if (!result) {
        Messages.showWarningDialog(SherlockBundle.message("project.cannot.be.generated"),
                                   SherlockBundle.message("error.in.project.generation"));
        return;
      }
    }

    final SherlockNewProjectSettings settings = computeProjectSettings(sherlockGenerator, settingsStep);
    final Project newProject = generateProject(settingsStep, settings);
    sherlockGenerator.afterProjectGenerated(newProject);
  }

  /**
   * Computes the project settings from the user input.
   *
   * @param generator    The project generator.
   * @param settingsStep The project settings step.
   * @return The computed project settings.
   */
  @Nullable
  public SherlockNewProjectSettings computeProjectSettings(DirectoryProjectGenerator<?> generator,
                                                           final ProjectSettingsStepBase settingsStep) {
    //TODO: To be updated once we settings configuration.
    SherlockNewProjectSettings projectSettings = null;
    if (generator instanceof SherlockEmptyProjectGenerator<?> projectGenerator) {
      projectSettings = (SherlockNewProjectSettings)projectGenerator.getProjectSettings();
    }

    if (projectSettings != null) {
      projectSettings.projectLocation = settingsStep.getProjectLocation();
    }

    return projectSettings;
  }

  /**
   * Generates the new project.
   *
   * @param settings           The project settings step.
   * @param generationSettings The project generation settings.
   * @return The newly created project.
   */
  @NotNull
  private static Project generateProject(@NotNull final ProjectSettingsStepBase settings, @Nullable SherlockNewProjectSettings generationSettings) {
    final DirectoryProjectGenerator generator = settings.getProjectGenerator();
    final String location = settings.getProjectLocation();
    return AbstractNewProjectStep.doGenerateProject(null, location, generator, generationSettings);
  }
}
