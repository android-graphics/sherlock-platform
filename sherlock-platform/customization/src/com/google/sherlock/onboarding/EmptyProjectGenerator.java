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

import com.intellij.facet.ui.ValidationResult;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectGenerator;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.jetbrains.jps.TimingLog.LOG;

public class EmptyProjectGenerator implements DirectoryProjectGenerator<NewProjectAction.MyProjectSettings> {
  @Override
  public @Nullable @Nls(capitalization = Nls.Capitalization.Sentence) String getDescription() {
    return null;
  }

  @Override
  public @Nullable String getHelpId() {
    return null;
  }


  @Override
  public @NotNull String getName() {
    return "Sherlock";
  }

  @Override
  public @Nullable Icon getLogo() {
    return null;
  }

  @Override
  public void generateProject(@NotNull Project project, @NotNull VirtualFile baseDir,
                              @NotNull NewProjectAction.MyProjectSettings settings, @NotNull Module module) {
    ProgressManager.getInstance().run(new Task.Modal(project, "Creating My Project", true) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        indicator.setText("Creating project directory...");
        createProjectDirectory(baseDir, settings);
        indicator.setText("Adding sample files...");
        addSampleFiles(baseDir, settings);
        //TODO:  add project generation logic here eventually
      }
    });
  }


  @Override
  public @NotNull ValidationResult validate(@NotNull String baseDirPath) {
    Path baseDir = Paths.get(baseDirPath);
    if (!Files.exists(baseDir)) {
      return new ValidationResult("The specified directory does not exist.");
    }
    //Expects an empty directory
    try (Stream<Path> files = Files.list(baseDir)) {
      if (files.count() > 0) {
        return new ValidationResult("The specified directory is not empty.");
      }
    } catch (IOException e) {
      return new ValidationResult("Error accessing the directory: " + e.getMessage());
    }
    return ValidationResult.OK;
  }

  private void createProjectDirectory(@NotNull VirtualFile baseDir, @NotNull NewProjectAction.MyProjectSettings settings) {
    try {
      VirtualFile projectDir = baseDir.createChildDirectory(this, settings.myProjectName);
      //TODO: create other directories and files within the project directory
    } catch (IOException e) {
      LOG.error("Error creating project directory", e);
      ApplicationManager.getApplication().invokeLater(() ->
                                                        Messages.showErrorDialog("Error creating project directory: " + e.getMessage(), "Error")
      );
    }
  }

  private void addSampleFiles(@NotNull VirtualFile baseDir, @NotNull NewProjectAction.MyProjectSettings settings) {
    try {
      VirtualFile projectDir = baseDir.findChild(settings.myProjectName);
      if (projectDir != null) {
        //TODO: Update this based on UI.
        VirtualFile sampleFile = projectDir.createChildData(this, "main.txt");
        VfsUtil.saveText(sampleFile, "This is a sample file in your project.");
      }
    } catch (IOException e) {
      //TODO
    }
  }
}
