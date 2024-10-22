/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sherlockPlugin.common

import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Paths

/**
 * Demonstrates how to write simple IntelliJ unit tests.
 */
@RunWith(JUnit4::class)
class ExampleTest : BasePlatformTestCase() {

    @Test
    fun `trace processor wrapper file exists in sandbox`() {
        val path = Paths.get(PathManager.getPluginsPath(), "sherlock", "lib", "main")
        assertExists(path.toFile())

    }
}