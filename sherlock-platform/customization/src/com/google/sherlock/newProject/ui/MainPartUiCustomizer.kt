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
package com.google.sherlock.newProject.ui

import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.Row

/**
 * Interface for customizing the main UI part of the new project wizard.
 */
interface MainPartUiCustomizer {
  /**
   * Adds a checkbox section to the UI.
   *
   * @param row The row to add the checkboxes to.
   */
  fun checkBoxSection(row: Row)

  /**
   * Adds a panel under the checkbox section.
   *
   * @param panel The panel to add.
   */
  fun underCheckBoxSection(panel: Panel)
}
