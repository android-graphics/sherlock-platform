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

package com.google.sherlock.newProject;

import com.google.sherlock.newProject.steps.SherlockNewProjectSettings;
import com.intellij.facet.ui.ValidationResult;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectGeneratorBase;
import com.intellij.util.BooleanFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Abstract base class for generating empty Sherlock projects.
 *
 * @param <T> The type of project settings, which must extend {@link SherlockNewProjectSettings}.
 */
public abstract class SherlockEmptyProjectGenerator<T extends SherlockNewProjectSettings> extends DirectoryProjectGeneratorBase<T> {
  public static final SherlockNewProjectSettings NO_SETTINGS = new SherlockNewProjectSettings();
  /**
   * Generates the project.
   *
   * @param project  The project to generate.
   * @param baseDir  The base directory of the project.
   * @param settings The project settings.
   * @param module   The main module of the project.
   */
  @Override
  public void generateProject(final @NotNull Project project, final @NotNull VirtualFile baseDir,
                              final @NotNull T settings, final @NotNull Module module) {
    // Use NO_SETTINGS to avoid nullable settings of project generator
    if (settings == NO_SETTINGS) {
      configureProjectNoSettings(project, baseDir, module);
      return;
    }
    configureProject(project, baseDir, settings, module);
  }

  @Override
  public @NotNull ValidationResult validate(@NotNull String baseDirPath) {
    // Check if the base directory is writable.
    if (!Files.isWritable(Path.of(baseDirPath))) {
      return new ValidationResult("Project location should be writable");
    }
    return ValidationResult.OK;
  }

  /**
   * Configures the project when no settings are provided.
   *
   * @param project The project to configure.
   * @param baseDir The base directory of the project.
   * @param module  The main module of the project.
   */
  protected void configureProjectNoSettings(final @NotNull Project project,
                                            final @NotNull VirtualFile baseDir,
                                            final @NotNull Module module) {
    throw new IllegalStateException(String.format("%s does not support project creation with out of settings. " +
                                                  "See %s doc for detail", getClass(), SherlockEmptyProjectGenerator.class));
  }

  /**
   * Configures the project with the given settings. Note that currently it doesn't do anything specific.
   *
   * @param project  The project to configure.
   * @param baseDir  The base directory of the project.
   * @param settings The project settings.
   * @param module   The main module of the project.
   */
  protected abstract void configureProject(final @NotNull Project project,
                                           final @NotNull VirtualFile baseDir,
                                           final @NotNull SherlockNewProjectSettings settings,
                                           final @NotNull Module module);

  /**
   * Returns the project settings.
   *
   * @return The project settings.
   */
  public SherlockNewProjectSettings getProjectSettings() {
    return new SherlockNewProjectSettings();
  }

  public void locationChanged(final @NotNull String newLocation) {
  }

  public @Nullable BooleanFunction<SherlockEmptyProjectGenerator> beforeProjectGenerated() {
    return null;
  }

  public void afterProjectGenerated(Project project) {
  }
}
