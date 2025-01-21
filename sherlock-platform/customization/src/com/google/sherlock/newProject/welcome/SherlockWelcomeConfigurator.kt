package com.google.sherlock.newProject.welcome

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.DirectoryProjectConfigurator

/**
 * Configures a Sherlock project after it has been created or opened.
 */
internal class SherlockWelcomeConfigurator : DirectoryProjectConfigurator {

  /**
   * Indicates whether the configuration needs to be performed on the EDT.
   *
   * @return `false` because the configuration can be performed on a background thread.
   */
  override val isEdtRequired: Boolean
    get() = false

  override fun configureProject(project: Project, baseDir: VirtualFile, moduleRef: Ref<Module>, isProjectCreatedWithWizard: Boolean) {
    if (isProjectCreatedWithWizard || isInsideTempDirectory(baseDir)) {
      return
    }
  }

  /**
   * Checks if the given directory is inside the system's temporary directory.
   *
   * @param baseDir The directory to check.
   * @return `true` if the directory is inside the temporary directory, `false` otherwise.
   */
  private fun isInsideTempDirectory(baseDir: VirtualFile): Boolean {
    val tempDir = LocalFileSystem.getInstance().findFileByPath(FileUtil.getTempDirectory()) ?: return false
    return VfsUtil.isAncestor(tempDir, baseDir, true)
  }
}