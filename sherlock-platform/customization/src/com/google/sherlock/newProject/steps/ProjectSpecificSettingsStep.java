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

import com.google.sherlock.newProject.SherlockEmptyProjectGenerator;
import com.intellij.ide.util.projectWizard.AbstractNewProjectStep;
import com.intellij.ide.util.projectWizard.ProjectSettingsStepBase;
import com.intellij.openapi.project.DumbAware;
import com.intellij.platform.DirectoryProjectGenerator;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;

public class ProjectSpecificSettingsStep<T> extends ProjectSettingsStepBase<T> implements DumbAware {

  public ProjectSpecificSettingsStep(final @NotNull DirectoryProjectGenerator<T> projectGenerator,
                                     final @NotNull AbstractNewProjectStep.AbstractCallback<T> callback) {
    super(projectGenerator, callback);
  }

  @Override
  protected JPanel createAndFillContentPanel() {
    if (myProjectGenerator instanceof SherlockEmptyProjectGenerator) {
      // Allow generator to display custom error
      ((SherlockEmptyProjectGenerator<?>)myProjectGenerator).setErrorCallback(this::setErrorText);
    }
    return createContentPanelWithAdvancedSettingsPanel();
  }
}
