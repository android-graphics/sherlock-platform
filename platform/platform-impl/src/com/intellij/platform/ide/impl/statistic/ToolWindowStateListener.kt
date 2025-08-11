// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.platform.ide.impl.statistic

import com.intellij.internal.statistic.collectors.fus.actions.persistence.ToolWindowCollector
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ToolWindowType
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.openapi.wm.impl.ToolWindowImpl
import com.intellij.openapi.wm.impl.WindowInfoImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class ToolWindowStateListener(private val project: Project) : ToolWindowManagerListener {
  override fun stateChanged(toolWindowManager: ToolWindowManager, toolWindow: ToolWindow, changeType: ToolWindowManagerListener.ToolWindowManagerEventType) {
    project.service<ToolWindowStateCollector>().stateChanged(toolWindowManager, toolWindow, changeType)
  }
}


@Service(Service.Level.PROJECT)
private class ToolWindowStateCollector(private val project: Project, private val coroutineScope: CoroutineScope) {
  private val windowsSize = ConcurrentHashMap<String, Int>()
  private val eventFlows = mutableMapOf<String, MutableSharedFlow<Unit>>()

  private fun reportResizeEvent(toolWindowManager: ToolWindowManager, toolWindow: ToolWindowImpl) {
    val size = if (toolWindow.anchor.isHorizontal) toolWindow.component.height else toolWindow.component.width
    if (!windowsSize.containsKey(toolWindow.id) || windowsSize[toolWindow.id] != size) { //we don't care about the changed height of vertical windows or the width of horizontal ones
      val windowInfo = toolWindow.windowInfo as? WindowInfoImpl ?: return
      ToolWindowCollector.getInstance().recordResized(project, windowInfo, toolWindowManager.isMaximized(toolWindow))
      windowsSize[toolWindow.id] = size
    }
  }

  @OptIn(FlowPreview::class)
  fun stateChanged(toolWindowManager: ToolWindowManager, toolWindow: ToolWindow, changeType: ToolWindowManagerListener.ToolWindowManagerEventType) {
    if (changeType == ToolWindowManagerListener.ToolWindowManagerEventType.MovedOrResized && toolWindow.type == ToolWindowType.DOCKED && toolWindow is ToolWindowImpl) {
      val flow = eventFlows.getOrPut(toolWindow.id) {
        MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST).apply {
          coroutineScope.launch(Dispatchers.EDT) {
            debounce(DEBOUNCE_TIMEOUT_MS).collect {
              reportResizeEvent(toolWindowManager, toolWindow)
            }
          }
        }
      }

      flow.tryEmit(Unit)
    }
  }

  companion object {
    const val DEBOUNCE_TIMEOUT_MS = 500L
  }
}