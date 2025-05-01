/*
 * Copyright 2025 Google LLC
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
package com.google.sherlock.newCapture

import com.intellij.openapi.project.Project

/**
 * Defines the contract for performing a new capture operation.
 * Plugins wishing to provide capture functionality for the "New Capture" action
 * must implement this interface.
 */
interface NewCapturePerformer {
  /**
   * Called when the "New Capture" action is invoked and this performer is selected.
   *
   * @param project The current project context.
   */
  fun performCapture(project: Project)
}