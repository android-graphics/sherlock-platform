/***
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
 ***/

package com.google.sherlock.onboarding;

import com.intellij.ide.util.projectWizard.ProjectSettingsStepBase;
import com.intellij.ide.util.projectWizard.AbstractNewProjectStep;
import org.jetbrains.annotations.NotNull;
import com.intellij.platform.DirectoryProjectGenerator;
import com.intellij.openapi.diagnostic.Logger;

public class NewProjectAction extends AbstractNewProjectStep<NewProjectAction.MyProjectSettings> {

  private static final Logger LOG = Logger.getInstance(NewProjectAction.class);

  public NewProjectAction() {
    super(new MyCustomization());
  }

  private static class MyCustomization extends Customization<MyProjectSettings> {
    @Override
    protected @NotNull AbstractCallback<MyProjectSettings> createCallback() {
      return new AbstractCallback<>() {};
    }

    @Override
    protected @NotNull DirectoryProjectGenerator<MyProjectSettings> createEmptyProjectGenerator() {
      return new EmptyProjectGenerator();
    }

    @Override
    protected @NotNull ProjectSettingsStepBase<MyProjectSettings> createProjectSpecificSettingsStep(
      @NotNull DirectoryProjectGenerator<MyProjectSettings> projectGenerator,
      @NotNull AbstractCallback<MyProjectSettings> callback) {
      return new ProjectSettingsStep(projectGenerator, callback);
    }

    // TODO: override getActions() for custom behavior
  }

  public static class MyProjectSettings {
    public String myProjectName;
    public String myProjectLocation;
  }
}