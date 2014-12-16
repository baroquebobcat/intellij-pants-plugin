// Copyright 2014 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.jps.incremental.model;

import com.intellij.util.containers.ContainerUtil;
import com.twitter.intellij.pants.jps.incremental.serialization.PantsJpsProjectExtensionSerializer;
import com.twitter.intellij.pants.jps.util.PantsJpsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildTargetLoader;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.JpsProject;

import java.util.List;

public class PantsBuildTargetType extends BuildTargetType<PantsBuildTarget> {
  public static PantsBuildTargetType INSTANCE = new PantsBuildTargetType();

  protected PantsBuildTargetType() {
    super("pants-goal-compile");
  }

  @NotNull
  @Override
  public List<PantsBuildTarget> computeAllTargets(@NotNull JpsModel model) {
    return ContainerUtil.createMaybeSingletonList(getTarget(model));
  }

  public PantsBuildTarget getTarget(JpsModel model) {
    final JpsProject jpsProject = model.getProject();
    final JpsPantsProjectExtension projectExtension = PantsJpsProjectExtensionSerializer.findPantsProjectExtension(jpsProject);
    return projectExtension != null && PantsJpsUtil.containsPantsModules(jpsProject.getModules()) ?
           new PantsBuildTarget(projectExtension.getTargetPath(), projectExtension.getTargetNames()) : null;
  }

  @NotNull
  @Override
  public BuildTargetLoader<PantsBuildTarget> createLoader(@NotNull final JpsModel model) {
    return new BuildTargetLoader<PantsBuildTarget>() {
      @Nullable
      @Override
      public PantsBuildTarget createTarget(@NotNull String targetId) {
        return getTarget(model);
      }
    };
  }
}
