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
package com.google.sherlock.newProject.steps;

/**
 * Stores the project generation settings selected on the first page of the new project dialog.
 * Currently, it only stores the project location, but it will eventually be used to configure
 * SDK installation, welcome scripts, and other project settings.
 */
public class SherlockNewProjectSettings {
  public String projectLocation;

  //TODO: Include SDK installation, welcome scripts etc.
}
