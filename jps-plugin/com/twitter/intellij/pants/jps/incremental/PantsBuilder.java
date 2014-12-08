// Copyright 2014 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.jps.incremental;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.twitter.intellij.pants.jps.incremental.serialization.PantsJpsModelSerializerExtension;
import com.twitter.intellij.pants.util.PantsConstants;
import com.twitter.intellij.pants.util.PantsOutputMessage;
import com.twitter.intellij.pants.util.PantsUtil;
import com.twitter.intellij.pants.jps.incremental.model.JpsPantsModuleExtension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.builders.java.JavaSourceRootDescriptor;
import org.jetbrains.jps.incremental.*;
import org.jetbrains.jps.incremental.java.JavaBuilder;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.model.module.JpsModule;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PantsBuilder extends ModuleLevelBuilder {
  private static final Logger LOG = Logger.getInstance("#com.twitter.intellij.pants.jps.incremental.PantsBuilder");

  protected PantsBuilder() {
    super(BuilderCategory.SOURCE_PROCESSOR);
  }

  @Override
  public List<String> getCompilableFileExtensions() {
    return Arrays.asList("scala", "java");
  }

  @Override
  public void buildStarted(CompileContext context) {
    JavaBuilder.IS_ENABLED.set(context, Boolean.FALSE);
  }

  @NotNull
  @Override
  public String getPresentableName() {
    return PantsConstants.PANTS;
  }

  @Override
  public ExitCode build(
    final CompileContext context,
    ModuleChunk chunk,
    DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget> dirtyFilesHolder,
    OutputConsumer outputConsumer
  ) throws ProjectBuildException, IOException {
    final List<JpsPantsModuleExtension> jpsExtensions =
      ContainerUtil.mapNotNull(
        chunk.getModules(),
        new Function<JpsModule, JpsPantsModuleExtension>() {
          @Override
          public JpsPantsModuleExtension fun(JpsModule module) {
            if (module.getSourceRoots().isEmpty()) {
              // optimization. no roots - no problems
              return null;
            }
            return PantsJpsModelSerializerExtension.findPantsModuleExtension(module);
          }
        }
      );

    if (jpsExtensions.isEmpty()) {
      return ExitCode.NOTHING_DONE;
    }

    final File pantsExecutable = findPantsExecutable(jpsExtensions);

    if (pantsExecutable == null) {
      context.processMessage(new CompilerMessage(PantsConstants.PANTS, BuildMessage.Kind.ERROR, "Failed to find Pants executable!"));
      LOG.error("Failed to find Pants executable for: " + jpsExtensions);
      return ExitCode.NOTHING_DONE;
    }

    final GeneralCommandLine commandLine = PantsUtil.defaultCommandLine(pantsExecutable);
    commandLine.addParameters("goal", "compile", "--no-colors");
    for (JpsPantsModuleExtension extension : jpsExtensions) {
      commandLine.addParameter(extension.getTargetAddress());
    }

    final Process process;
    try {
      process = commandLine.createProcess();
    }
    catch (ExecutionException e) {
      throw new ProjectBuildException(e);
    }
    final CapturingProcessHandler processHandler = new CapturingProcessHandler(process);
    processHandler.addProcessListener(
      new ProcessAdapter() {
        @Override
        public void onTextAvailable(ProcessEvent event, Key outputType) {
          super.onTextAvailable(event, outputType);
          context.processMessage(getCompilerMessage(event, outputType));
        }
      }
    );
    final ProcessOutput processOutput = processHandler.runProcess();
    processOutput.checkSuccess(LOG);

    return ExitCode.OK;
  }

  @NotNull
  public CompilerMessage getCompilerMessage(ProcessEvent event, Key outputType) {
    final PantsOutputMessage message = PantsOutputMessage.parse(event.getText(), false, true);
    if (message == null) {
      return new CompilerMessage(PantsConstants.PANTS, BuildMessage.Kind.INFO, event.getText());
    }

    final BuildMessage.Kind kind = outputType == ProcessOutputTypes.STDERR || message.isError() ? BuildMessage.Kind.ERROR : BuildMessage.Kind.INFO;
    return new CompilerMessage(
      PantsConstants.PANTS,
      kind,
      event.getText().substring(message.getEnd()),
      message.getFilePath(),
      -1L, -1L, -1L, message.getLineNumber(), -1L
    );
  }

  @Nullable
  private File findPantsExecutable(List<JpsPantsModuleExtension> extensions) {
    for (JpsPantsModuleExtension extension : extensions) {
      final File configFile = new File(extension.getConfigPath());
      final File executable = PantsUtil.findPantsExecutable(configFile);
      if (executable != null) {
        return executable;
      }
    }

    return null;
  }
}
