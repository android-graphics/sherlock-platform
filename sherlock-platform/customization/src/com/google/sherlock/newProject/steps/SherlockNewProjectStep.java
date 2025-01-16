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

import com.intellij.ide.util.projectWizard.AbstractNewProjectStep;
import com.intellij.ide.util.projectWizard.ProjectSettingsStepBase;
import com.intellij.platform.DirectoryProjectGenerator;
import org.jetbrains.annotations.NotNull;

/**
 * The initial step in the Sherlock new project wizard.
 * This step handles the creation of a new Sherlock project.
 */
public class SherlockNewProjectStep extends AbstractNewProjectStep<SherlockNewProjectSettings> {

  public SherlockNewProjectStep() {
    super(new Customization());
  }

  /**
   * Inner class providing customizations for the project creation process.
   */
  protected static class Customization extends AbstractNewProjectStep.Customization<SherlockNewProjectSettings> {
    @Override
    protected @NotNull AbstractCallback<SherlockNewProjectSettings> createCallback() {
      return new SherlockGenerateProjectCallback<>() {
      };
    }

    @Override
    protected @NotNull DirectoryProjectGenerator<SherlockNewProjectSettings> createEmptyProjectGenerator() {
      return new SherlockBaseProjectGenerator();
    }

    @Override
    protected @NotNull ProjectSettingsStepBase<SherlockNewProjectSettings> createProjectSpecificSettingsStep(
      @NotNull DirectoryProjectGenerator<SherlockNewProjectSettings> projectGenerator,
      @NotNull AbstractCallback<SherlockNewProjectSettings> callback) {
      return new SherlockProjectSpecificSettingsStep<>(projectGenerator, callback);
    }
  }
}