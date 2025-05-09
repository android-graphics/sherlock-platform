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
package com.intellij.openapi.editor.actions;

import com.intellij.ide.IdeBundle;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public final class SherlockCaptureAction extends AnAction {
  private static final Logger LOG = Logger.getInstance(SherlockCaptureAction.class);

  public SherlockCaptureAction() {
    super(IdeBundle.messagePointer("recapture.trace"));
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
    String fileExtension = file.getExtension();
    if (file != null && ("perfetto".equals(fileExtension) || "pftrace".equals(fileExtension))) {
      LOG.info("Clicked on Re-capture button");
    }
  }
}
