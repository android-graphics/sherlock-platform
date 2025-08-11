package com.intellij.driver.sdk.ui.components

import com.intellij.driver.client.Driver
import com.intellij.driver.model.RemoteMouseButton
import com.intellij.driver.model.TextData
import com.intellij.driver.sdk.screenshot.takeScreenshot
import com.intellij.driver.sdk.ui.DEFAULT_FIND_TIMEOUT_SECONDS
import com.intellij.driver.sdk.ui.Finder
import com.intellij.driver.sdk.ui.SearchContext
import com.intellij.driver.sdk.ui.UiText
import com.intellij.driver.sdk.ui.keyboard.WithKeyboard
import com.intellij.driver.sdk.ui.remote.Component
import com.intellij.driver.sdk.ui.remote.Robot
import com.intellij.driver.sdk.ui.remote.RobotProvider
import com.intellij.driver.sdk.ui.remote.SearchService
import com.intellij.driver.sdk.waitFor
import com.intellij.openapi.util.SystemInfo
import java.awt.Color
import java.awt.Point
import java.awt.Rectangle
import java.awt.image.BufferedImage
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


data class ComponentData(val xpath: String,
                         val driver: Driver,
                         val searchService: SearchService,
                         val robotProvider: RobotProvider,
                         val parentSearchContext: SearchContext,
                         val foundComponent: Component?)

open class UiComponent(private val data: ComponentData) : Finder, WithKeyboard {

  companion object {
    /**
     * Waits until the element specified is found within the parent search context. Doesn't guaranty visibility.
     *
     * @param timeout The maximum time to wait for the element to not be found. If not specified, the default timeout is used.
     */
    fun <T : UiComponent> T.waitFound(): T {
      findThisComponent()
      return this
    }

    /**
     * Asserts that the current UI component is found. Doesn't check visibility.
     *
     * @return The current UI component.
     */
    fun <T : UiComponent> T.assertFound(): T {
      assert(present()) { "Component '$this' should be found" }
      return this
    }

  }

  private var cachedComponent: Component? = null
  val component: Component
    get() = data.foundComponent ?: kotlin.runCatching { cachedComponent?.takeIf { it.isShowing() } }.getOrNull()
            ?: findThisComponent().apply { cachedComponent = this }

  fun setFocus() {
    robot.focus(this.component)
  }

  private fun findThisComponent(): Component {
    lateinit var result: List<Component>
    var actual = 0
    waitFor(DEFAULT_FIND_TIMEOUT_SECONDS.seconds,
            errorMessage = { "Can't find component with '${data.xpath}' in ${data.parentSearchContext.context.takeIf { it.isNotEmpty() } ?: "whole hierarchy"}, expected 1, but got ${actual}" }) {
      result = data.parentSearchContext.findAll(data.xpath)
      actual = result.size
      result.size == 1
    }
    return result.first()
  }

  override val driver: Driver = data.driver
  override val searchService: SearchService = data.searchService
  override val robotProvider: RobotProvider = data.robotProvider

  val robot: Robot by lazy {
    data.robotProvider.getRobotFor(component)
  }

  override val searchContext: SearchContext = object : SearchContext {
    override val context: String = data.parentSearchContext.context + data.xpath

    override fun findAll(xpath: String): List<Component> {
      return searchService.findAll(xpath, component)
    }
  }

  // Search Text Locations
  fun findText(text: String): UiText {
    val allTexts = findAllText()
    val filteredTexts = allTexts.filter { it.text == text }
    if (filteredTexts.isEmpty()) {
      throw AssertionError("No '$text' found. Available texts are:\n${allTexts.joinToString("\n") { it.text }}")
    }
    if (filteredTexts.size > 1) {
      throw AssertionError("Found ${filteredTexts.size} texts '$text', expected 1")
    }
    return filteredTexts.first()
  }

  fun findFirst(text: String, duration: Duration = DEFAULT_FIND_TIMEOUT_SECONDS.seconds): UiText {
    var allTexts = emptyList<UiText>()
    waitFor(duration, errorMessage = "Can't find '$text' in ${data.parentSearchContext.context}") {
      allTexts = findAllText()
      allTexts.any { it.text == text }
    }
    return allTexts.first { it.text == text }
  }

  fun findOne(text: String, duration: Duration = DEFAULT_FIND_TIMEOUT_SECONDS.seconds): UiText {
    var allTexts = emptyList<UiText>()
    waitFor(duration, errorMessage = "Can't find '$text' in ${data.parentSearchContext.context}") {
      allTexts = findAllText()
      allTexts.any { it.text == text }
    }
    val filteredTexts = allTexts.filter { it.text == text }
    if (filteredTexts.size > 1) {
      throw AssertionError("Found ${filteredTexts.size} texts '$text', expected 1")
    }
    return filteredTexts.first()
  }

  fun findOneContainsText(text: String, ignoreCase: Boolean = true, duration: Duration = DEFAULT_FIND_TIMEOUT_SECONDS.seconds): UiText {
    var allTexts = emptyList<UiText>()
    waitFor(duration, errorMessage = "Can't find '$text' in ${data.parentSearchContext.context}") {
      allTexts = findAllText()
      allTexts.any { it.text.contains(text, ignoreCase = ignoreCase) }
    }
    val filteredTexts = allTexts.filter { it.text.contains(text, ignoreCase = ignoreCase) }
    if (filteredTexts.size > 1) {
      throw AssertionError("Found ${filteredTexts.size} texts '$text', expected 1")
    }
    return filteredTexts.first()
  }

  fun hasText(text: String): Boolean {
    return findAllText {
      it.text == text
    }.isNotEmpty()
  }

  fun hasTextSequence(vararg texts: String, indexOffset: Int = 0): Boolean {
    require(indexOffset >= 0) { "Value must be non-negative" }
    val stringList = texts.toList()
    val uiTextList = findAllText()
    return stringList.indices.all { index ->
      val uiTextIndex = index + indexOffset
      uiTextIndex in uiTextList.indices && stringList[index] == uiTextList[uiTextIndex].text
    }
  }

  fun hasSubtext(subtext: String): Boolean {
    return findAllText {
      it.text.contains(subtext)
    }.isNotEmpty()
  }

  fun findText(predicate: (TextData) -> Boolean): UiText {
    return searchService.findAllText(component).single(predicate).let { UiText(this, it) }
  }

  fun present(): Boolean {
    return data.parentSearchContext.findAll(data.xpath).isNotEmpty()
  }

  fun notPresent(): Boolean {
    return data.parentSearchContext.findAll(data.xpath).isEmpty()
  }

  fun hasText(predicate: (TextData) -> Boolean): Boolean {
    return searchService.findAllText(component).any(predicate)
  }

  fun isVisible(): Boolean = component.isVisible()

  fun isEnabled(): Boolean = component.isEnabled()

  fun findAllText(predicate: (TextData) -> Boolean): List<UiText> {
    return searchService.findAllText(component).filter(predicate).map { UiText(this, it) }
  }

  fun findAllText(): List<UiText> {
    return searchService.findAllText(component).map { UiText(this, it) }
  }

  fun hasVisibleComponent(component: UiComponent): Boolean {
    val components = searchContext.findAll(component.data.xpath)
    if (components.isEmpty()) return false
    return components.any { it.isVisible() }
  }

  fun getParent(): UiComponent {
    return UiComponent(ComponentData(data.xpath + "/..", driver, searchService, robotProvider, data.parentSearchContext, component.getParent()))
  }

  fun getScreenshot(): BufferedImage {
    val screenshot = takeScreenshot(Rectangle(component.getLocationOnScreen().x, component.getLocationOnScreen().y, component.width, component.height))
    if (SystemInfo.isWindows && this is DialogUiComponent)
      return screenshot.getSubimage(
        7,
        0,
        component.width - 14,
        component.height - 7
      )
    return screenshot
  }

  // Mouse
  fun click(point: Point? = null) {
    if (point != null) {
      robot.click(component, point)
    }
    else {
      robot.click(component)
    }
  }

  fun doubleClick(point: Point? = null) {
    if (point != null) {
      robot.click(component, point, RemoteMouseButton.LEFT, 2)
    }
    else {
      robot.doubleClick(component)
    }
  }

  fun rightClick(point: Point? = null) {
    if (point != null) {
      robot.click(component, point, RemoteMouseButton.RIGHT, 1)
    }
    else {
      robot.rightClick(component)
    }
  }

  fun click(button: RemoteMouseButton, count: Int) {
    robot.click(component, button, count)
  }

  fun moveMouse() {
    robot.moveMouse(component)
  }

  fun moveMouse(point: Point) {
    robot.moveMouse(component, point)
  }

  fun hasFocus(): Boolean {
    return component.isFocusOwner()
  }

  fun mousePressMoveRelease(from: Point, to: Point) {
    robot.apply {
      moveMouse(component, from)
      pressMouse(RemoteMouseButton.LEFT)

      moveMouse(component, to)
      releaseMouse(RemoteMouseButton.LEFT)
    }
  }

  fun getBackgroundColor() = Color(component.getBackground().getRGB())

  fun getForegroundColor() = Color(component.getForeground().getRGB())

  fun waitIsFocusOwner() {
    waitFor {
      component.isFocusOwner()
    }
  }
}