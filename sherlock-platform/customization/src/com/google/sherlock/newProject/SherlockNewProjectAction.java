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

import com.google.sherlock.newProject.ui.SherlockNewProjectDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * Action class to initiate the Sherlock new project wizard.
 * This action opens a dialog that guides the user through the process of creating a new Sherlock project.
 */
public class SherlockNewProjectAction extends DumbAwareAction {

  @Override
  public void actionPerformed(@NotNull final AnActionEvent e) {
    final SherlockNewProjectDialog dlg = new SherlockNewProjectDialog();
    dlg.show();
  }
}