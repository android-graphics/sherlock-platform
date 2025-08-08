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

package org.jetbrains.intellij.build
import java.nio.file.Path
import kotlinx.collections.immutable.persistentListOf

/**
 * Configures the Sherlock distribution by specifying bundled plugins, JVM args, extra files, and more.
 * See also: BaseIdeaProperties, IdeaCommunityProperties.
 */
class SherlockProperties(home: Path) : BaseIdeaProperties() {
    init {
      platformPrefix = "SherlockPlatform"
      applicationInfoModule = "intellij.idea.community.customization" // TODO: better to use own module.
      useSplash = true
      productLayout.buildAllCompatiblePlugins = false
      productLayout.prepareCustomPluginRepositoryForPublishedPlugins = false
      productLayout.productImplementationModules = listOf(
        "intellij.platform.starter",
        "intellij.idea.community.customization",
      )
      // TODO: Bundle plugin here
      productLayout.bundledPluginModules = mutableListOf()
      productLayout.pluginLayouts = persistentListOf()
    }

    override val baseFileName: String = "sherlock-platform"

    override fun getBaseArtifactName(appInfo: ApplicationInfoProperties, buildNumber: String): String = "sherlock-platform-$buildNumber"

    override fun getSystemSelector(appInfo: ApplicationInfoProperties, buildNumber: String): String = "SherlockPlatform"

    override fun createLinuxCustomizer(projectHome: String): LinuxDistributionCustomizer {
      return object : LinuxDistributionCustomizer() {}
    }

    override fun createMacCustomizer(projectHome: String): MacDistributionCustomizer {
      return object : MacDistributionCustomizer() {
        init {
          bundleIdentifier = "com.google.sherlock.platform"
          icnsPath = "${projectHome}/build/conf/ideaCE/mac/images/idea.icns" // TODO
        }
      }
    }

    override fun createWindowsCustomizer(projectHome: String): WindowsDistributionCustomizer {
      return object : WindowsDistributionCustomizer() {
        init {
          icoPath = "${projectHome}/build/conf/ideaCE/win/images/idea_CE.ico" // TODO
        }
      }
    }
}
