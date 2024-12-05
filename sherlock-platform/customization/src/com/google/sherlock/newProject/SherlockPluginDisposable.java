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

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Provides a disposable instance that can be used as a parent disposable for project- or application-level components.
 * <p>
 * This service is intended to be used instead of a project or application instance as a parent disposable.
 */
@Service({Service.Level.APP, Service.Level.PROJECT})
public final class SherlockPluginDisposable implements Disposable {
  @Override
  public void dispose() {
  }

  /**
   * Returns the application-level instance of the disposable.
   *
   * @return The application-level instance.
   */
  public static @NotNull Disposable getInstance() {
    return ApplicationManager.getApplication().getService(SherlockPluginDisposable.class);
  }

  /**
   * Returns the project-level instance of the disposable.
   *
   * @param project The project.
   * @return The project-level instance.
   */
  public static @NotNull Disposable getInstance(@NotNull Project project) {
    return project.getService(SherlockPluginDisposable.class);
  }
}

