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
package com.google.sherlock.newProject.welcome

import com.google.sherlock.newProject.SherlockPluginDisposable
import com.google.sherlock.newProject.welcome.SherlockWelcomeCollector.ProjectType
import com.google.sherlock.newProject.welcome.SherlockWelcomeCollector.ProjectViewPoint
import com.google.sherlock.newProject.welcome.SherlockWelcomeCollector.ProjectViewResult
import com.google.sherlock.newProject.welcome.SherlockWelcomeCollector.ScriptResult
import com.intellij.ide.impl.ProjectViewSelectInTarget
import com.intellij.ide.projectView.impl.ProjectViewPane
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.AppUIExecutor
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbAwareRunnable
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.ui.dsl.builder.panel
import org.jetbrains.annotations.CalledInAny
import javax.swing.JPanel

/**
 * Utility object for welcoming users to new or opened Sherlock projects.
 */
internal object SherlockWelcome {

  /**
   * Creates the welcome settings panel for the new project wizard.
   *
   * @return The welcome settings panel.
   */
  fun createWelcomeSettingsPanel(): JPanel {
    return panel {
      row {
        checkBox(SherlockWelcomeBundle.message("sherlock.welcome.new.project.text"))
          .comment(SherlockWelcomeBundle.message("sherlock.welcome.new.project.description"))
      }
    }
  }

  /**
   * Welcomes the user to the project.
   *
   * @param project The project.
   * @param baseDir The base directory of the project.
   * @param module The main module of the project.
   */
  @CalledInAny
  fun welcomeUser(project: Project, baseDir: VirtualFile, module: Module?) {
    SherlockWelcomeCollector.logWelcomeProject(project, ProjectType.NEW)

    if (isEmptyProject(project, baseDir, module)) {
      SherlockWelcomeCollector.logWelcomeScript(project, ScriptResult.DISABLED_BUT_COULD)
      expandProjectTree(project, baseDir, module, null)
    }
    else {
      SherlockWelcomeCollector.logWelcomeScript(project, ScriptResult.DISABLED_AND_COULD_NOT)
      expandProjectTree(project, baseDir, module, null)
    }
  }

  /**
   * Checks if the project is empty.
   *
   * @param project The project.
   * @param baseDir The base directory of the project.
   * @param module The main module of the project.
   * @return `true` if the project is empty, `false` otherwise.
   */
  private fun isEmptyProject(project: Project, baseDir: VirtualFile, module: Module?): Boolean {
    return firstUserFile(project, baseDir, module) == null
  }

  /**
   * Returns the first user file in the project.
   *
   * @param project The project.
   * @param baseDir The base directory of the project.
   * @param module The main module of the project.
   * @return The first user file, or `null` if none is found.
   */
  private fun firstUserFile(project: Project, baseDir: VirtualFile, module: Module?): VirtualFile? {
    if (module != null && module.isDisposed || module == null && project.isDisposed) return null
    return baseDir.children.firstOrNull()
  }


  /**
   * Expands the project tree in the Project View tool window.
   *
   * @param project The project.
   * @param baseDir The base directory of the project.
   * @param module The main module of the project.
   * @param file The file to select.
   */
  @CalledInAny
  private fun expandProjectTree(project: Project, baseDir: VirtualFile, module: Module?, file: VirtualFile?) {
    expandProjectTree(project, ToolWindowManager.getInstance(project), baseDir, module, file, ProjectViewPoint.IMMEDIATELY)
  }

  /**
   * Expands the project tree in the Project View tool window.
   *
   * @param project The project.
   * @param toolWindowManager The tool window manager.
   * @param baseDir The base directory of the project.
   * @param module The main module of the project.
   * @param file The file to select.
   * @param point The point at which the project tree is expanded.
   */
  @CalledInAny
  private fun expandProjectTree(
    project: Project,
    toolWindowManager: ToolWindowManager,
    baseDir: VirtualFile,
    module: Module?,
    file: VirtualFile?,
    point: ProjectViewPoint,
  ) {
    // the approach was taken from com.intellij.platform.PlatformProjectViewOpener
    val toolWindow = toolWindowManager.getToolWindow(ToolWindowId.PROJECT_VIEW)
    if (toolWindow == null) {
      val listener = ProjectViewListener(project, baseDir, module, file)
      Disposer.register(SherlockPluginDisposable.getInstance(project), listener)
      // collected listener will release the connection
      project.messageBus.connect(listener).subscribe(ToolWindowManagerListener.TOPIC, listener)
    }
    else {
      StartupManager.getInstance(project).runAfterOpened(
        DumbAwareRunnable {
          AppUIExecutor
            .onUiThread(ModalityState.nonModal())
            .expireWith(SherlockPluginDisposable.getInstance(project))
            .submit {
              val fileToChoose = (file ?: firstUserFile(project, baseDir, module)) ?: return@submit

              ProjectViewSelectInTarget
                .select(project, fileToChoose, ProjectViewPane.ID, null, fileToChoose, false)
                .doWhenDone { SherlockWelcomeCollector.logWelcomeProjectView(project, point, ProjectViewResult.EXPANDED) }
                .doWhenRejected(Runnable { SherlockWelcomeCollector.logWelcomeProjectView(project, point, ProjectViewResult.REJECTED) })
            }
        }
      )
    }
  }

  /**
   * A listener for the Project View tool window.
   */
  private class ProjectViewListener(
    private val project: Project,
    private val baseDir: VirtualFile,
    private val module: Module?,
    private val file: VirtualFile?,
  ) : ToolWindowManagerListener, Disposable {

    private var toolWindowRegistered = false

    /**
     * Called when tool windows are registered.
     *
     * @param ids The IDs of the registered tool windows.
     * @param toolWindowManager The tool window manager.
     */
    override fun toolWindowsRegistered(ids: List<String>, toolWindowManager: ToolWindowManager) {
      if (ToolWindowId.PROJECT_VIEW in ids) {
        toolWindowRegistered = true
        Disposer.dispose(this) // to release message bus connection
        expandProjectTree(project, toolWindowManager, baseDir, module, file, ProjectViewPoint.FROM_LISTENER)
      }
    }

    override fun dispose() {
      if (!toolWindowRegistered) {
        SherlockWelcomeCollector.logWelcomeProjectView(project, ProjectViewPoint.FROM_LISTENER, ProjectViewResult.NO_TOOLWINDOW)
      }
    }
  }
}