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

import com.intellij.ide.util.projectWizard.AbstractNewProjectStep;
import com.intellij.ide.util.projectWizard.ProjectSettingsStepBase;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.platform.DirectoryProjectGenerator;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;


public class ProjectSettingsStep extends ProjectSettingsStepBase<NewProjectAction.MyProjectSettings> {
  private JPanel myMainPanel;
  private TextFieldWithBrowseButton myLocationField;
  private JTextField myProjectNameField;

  public ProjectSettingsStep(@NotNull DirectoryProjectGenerator<NewProjectAction.MyProjectSettings> projectGenerator,
                             @NotNull AbstractNewProjectStep.AbstractCallback<NewProjectAction.MyProjectSettings> callback) {
    super(projectGenerator, callback);

    // UI Setup
    myMainPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);

    // Project Name
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    myMainPanel.add(new JLabel("Project Name:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    myProjectNameField = new JTextField(20);
    myMainPanel.add(myProjectNameField, gbc);

    // Project Location
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.anchor = GridBagConstraints.WEST;
    myMainPanel.add(new JLabel("Project Location:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    myLocationField = new TextFieldWithBrowseButton();
    myLocationField.addBrowseFolderListener(
      "Select Project Location",
      null,
      null,
      FileChooserDescriptorFactory.createSingleFolderDescriptor()
    );
    myMainPanel.add(myLocationField, gbc);
  }
}
