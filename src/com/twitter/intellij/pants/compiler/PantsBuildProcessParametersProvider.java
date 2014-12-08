// Copyright 2014 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.compiler;

import com.intellij.compiler.server.BuildProcessParametersProvider;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PantsBuildProcessParametersProvider extends BuildProcessParametersProvider {
  @NotNull
  @Override
  public List<String> getClassPath() {
    // hack to trick BuildProcessClasspathManager
    final String classpath = System.getProperty("pants.jps.plugin.classpath");
    if (StringUtil.isNotEmpty(classpath)) {
      return StringUtil.split(classpath, ":");
    }
    return super.getClassPath();
  }
}
