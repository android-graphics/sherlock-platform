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
package com.google.sherlock.module;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;


public final class SherlockModuleType extends ModuleType<SherlockModuleBuilder> {
  public static final String ID = "SHERLOCK_MODULE_TYPE";

  SherlockModuleType() {
    super(ID);
  }

  @NotNull
  @Override
  public SherlockModuleBuilder createModuleBuilder() {
    return new SherlockModuleBuilder();
  }

  @NotNull
  @Override
  public String getName() {
    return "SHERLOCK MODULE";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "Sherlock's custom module type";
  }

  @NotNull
  @Override
  public Icon getNodeIcon(@Deprecated boolean b) {
    return PlatformIcons.ABSTRACT_METHOD_ICON;
  }

  @Override
  public ModuleWizardStep @NotNull [] createWizardSteps(@NotNull WizardContext wizardContext,
                                                        @NotNull SherlockModuleBuilder moduleBuilder,
                                                        @NotNull ModulesProvider modulesProvider) {
    return super.createWizardSteps(wizardContext, moduleBuilder, modulesProvider);
  }

  public static SherlockModuleType getInstance() {
    return (SherlockModuleType)ModuleTypeManager.getInstance().findByID(ID);
  }
}