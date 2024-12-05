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
package com.google.sherlock.newProject.welcome;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

/**
 * Provides localized messages for the Sherlock welcome functionality.
 */
public class SherlockWelcomeBundle {
  private static final @NonNls String BUNDLE = "messages.SherlockWelcomeBundle";
  private static final DynamicBundle INSTANCE = new DynamicBundle(SherlockWelcomeBundle.class, BUNDLE);

  private SherlockWelcomeBundle() { }

  /**
   * Returns the localized message for the given key.
   *
   * @param key    The key of the message.
   * @param params The parameters to format the message with.
   * @return The localized message.
   */
  public static @NotNull @Nls String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    return INSTANCE.getMessage(key, params);
  }
}
