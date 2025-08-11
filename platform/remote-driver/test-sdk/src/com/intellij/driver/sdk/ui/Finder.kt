package com.intellij.driver.sdk.ui

import com.intellij.driver.client.Driver
import com.intellij.driver.sdk.ui.components.ComponentData
import com.intellij.driver.sdk.ui.components.UIComponentsList
import com.intellij.driver.sdk.ui.components.UiComponent
import com.intellij.driver.sdk.ui.remote.Component
import com.intellij.driver.sdk.ui.remote.RobotProvider
import com.intellij.driver.sdk.ui.remote.SearchService
import org.intellij.lang.annotations.Language

internal const val DEFAULT_FIND_TIMEOUT_SECONDS = 15

interface Finder {
  val driver: Driver
  val searchService: SearchService
  val robotProvider: RobotProvider
  val searchContext: SearchContext

  val isRemoteIdeMode: Boolean
    get() = driver.isRemoteIdeMode

  fun x(@Language("xpath") xpath: String): UiComponent {
    return UiComponent(ComponentData(xpath, driver, searchService, robotProvider, searchContext, null))
  }

  fun x(init: QueryBuilder.() -> String): UiComponent {
    return x(xQuery { init() })
  }

  fun <T : UiComponent> x(type: Class<T>, init: QueryBuilder.() -> String): T {
    return x(xQuery { init() }, type)
  }

  fun <T : UiComponent> x(@Language("xpath") xpath: String, type: Class<T>): T {
    return type.getConstructor(
      ComponentData::class.java
    ).newInstance(ComponentData(xpath, driver, searchService, robotProvider, searchContext, null))
  }

  fun xx(@Language("xpath") xpath: String): UIComponentsList<UiComponent> {
    return UIComponentsList(xpath, UiComponent::class.java, driver, searchService, robotProvider, searchContext)
  }

  fun xx(init: QueryBuilder.() -> String): UIComponentsList<UiComponent> {
    return UIComponentsList(xQuery { init() }, UiComponent::class.java, driver, searchService, robotProvider, searchContext)
  }

  fun <T : UiComponent> xx(@Language("xpath") xpath: String, type: Class<T>): UIComponentsList<T> {
    return UIComponentsList(xpath, type, driver, searchService, robotProvider, searchContext)
  }
}

interface SearchContext {
  val context: String
  fun findAll(xpath: String): List<Component>
}