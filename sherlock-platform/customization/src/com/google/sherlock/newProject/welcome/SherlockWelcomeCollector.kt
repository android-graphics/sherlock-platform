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

import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import com.intellij.openapi.project.Project

/**
 * Collects usage statistics for the Sherlock welcome functionality.
 */
internal object SherlockWelcomeCollector : CounterUsagesCollector() {

  /**
   * Returns the event log group for the collector.
   *
   * @return The event log group.
   */
  override fun getGroup(): EventLogGroup = GROUP

  /**
   * Enum representing the type of project (new or opened).
   */
  internal enum class ProjectType { NEW, OPENED }

  /**
   * Enum representing the result of attempting to run a welcome script.
   */
  internal enum class ScriptResult { DISABLED_BUT_COULD, DISABLED_AND_COULD_NOT }

  /**
   * Enum representing the point at which the project view is expanded.
   */
  internal enum class ProjectViewPoint { IMMEDIATELY, FROM_LISTENER }

  /**
   * Enum representing the result of attempting to expand the project view.
   */
  internal enum class ProjectViewResult { EXPANDED, REJECTED, NO_TOOLWINDOW }

  /**
   * Logs an event for welcoming a user to a project.
   *
   * @param project The project.
   * @param type The type of project.
   */
  internal fun logWelcomeProject(project: Project, type: ProjectType): Unit = welcomeProjectEvent.log(project, type)

  /**
   * Logs an event for attempting to run a welcome script.
   *
   * @param project The project.
   * @param result The result of the attempt.
   */
  internal fun logWelcomeScript(project: Project, result: ScriptResult): Unit = welcomeScriptEvent.log(project, result)

  /**
   * Logs an event for attempting to expand the project view.
   *
   * @param project The project.
   * @param point The point at which the project view is expanded.
   * @param result The result of the attempt.
   */
  internal fun logWelcomeProjectView(project: Project, point: ProjectViewPoint, result: ProjectViewResult) {
    welcomeProjectViewEvent.log(project, point, result)
  }

  private val GROUP = EventLogGroup("sherlock.welcome.events", 2)

  private val welcomeProjectEvent =
    GROUP.registerEvent("welcome.project", EventFields.Enum("project_type", ProjectType::class.java))

  private val welcomeScriptEvent =
    GROUP.registerEvent("welcome.script", EventFields.Enum("script_result", ScriptResult::class.java))

  private val welcomeProjectViewEvent =
    GROUP.registerEvent("welcome.projectView",
                        EventFields.Enum("project_view_point", ProjectViewPoint::class.java),
                        EventFields.Enum("project_view_result", ProjectViewResult::class.java))

}