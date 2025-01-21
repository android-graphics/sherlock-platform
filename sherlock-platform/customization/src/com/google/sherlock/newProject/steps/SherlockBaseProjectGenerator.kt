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

import com.google.sherlock.newProject.SherlockBundle
import com.google.sherlock.newProject.SherlockEmptyProjectGenerator
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.Nls
import javax.swing.Icon

/**
 * Provides a base implementation for generating empty Sherlock projects.
 */
class SherlockBaseProjectGenerator : SherlockEmptyProjectGenerator<SherlockNewProjectSettings?>() {
  /**
   * Returns the name of the project generator.
   *
   * @return The name of the project generator.
   */
  override fun getName(): @Nls String = SherlockBundle.message("sherlock.project")

  /**
   * Returns the logo for the project generator.
   *
   * @return The logo for the project generator.
   */
  override fun getLogo(): Icon? {
    //TODO: Update when we have a Logo
    return null
  }

  /**
   * Configures the newly created project.
   *
   * @param project  The newly created project.
   * @param baseDir  The base directory of the project.
   * @param settings The project settings.
   * @param module   The main module of the project.
   */
  public override fun configureProject(
    project: Project,
    baseDir: VirtualFile,
    settings: SherlockNewProjectSettings,
    module: Module) {}
}
