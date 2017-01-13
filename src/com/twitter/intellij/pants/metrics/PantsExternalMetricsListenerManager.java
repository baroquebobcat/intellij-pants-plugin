// Copyright 2016 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.metrics;

import com.intellij.execution.CommonProgramRunConfigurationParameters;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.ExtensionPointName;
import org.jetbrains.plugins.scala.testingSupport.test.AbstractTestRunConfiguration;

import java.util.Arrays;


public class PantsExternalMetricsListenerManager implements PantsExternalMetricsListener {

  private static final Logger LOG = Logger.getInstance(PantsExternalMetricsListenerManager.class);


  private static PantsExternalMetricsListenerManager instance = new PantsExternalMetricsListenerManager();

  private static ExtensionPointName<PantsExternalMetricsListener>
    EP_NAME = ExtensionPointName.create("com.intellij.plugins.pants.pantsExternalMetricsListener");

  public static PantsExternalMetricsListenerManager getInstance() {
    return instance;
  }

  @Override
  public void logIsIncrementalImport(boolean isIncremental) {
    Arrays.stream(EP_NAME.getExtensions()).forEach(s -> {
      try {
        s.logIsIncrementalImport(isIncremental);
      }
      catch (Throwable t) {
        LOG.info(t);
      }
    });
  }

  @Override
  public void logIsGUIImport(boolean isGUI) {
    Arrays.stream(EP_NAME.getExtensions()).forEach(s -> {
      try {
        s.logIsGUIImport(isGUI);
      }
      catch (Throwable t) {
        LOG.info(t);
      }
    });
  }

  @Override
  public void logTestRunner(PantsExternalMetricsListener.TestRunnerType runner) {
    Arrays.stream(EP_NAME.getExtensions()).forEach(s -> {
      try {
        s.logTestRunner(runner);
      }
      catch (Throwable t) {
        LOG.info(t);
      }
    });
  }

  public void logTestRunner(RunConfiguration runConfiguration) {
    /**
     /**
     * Scala related run/test configuration inherit {@link AbstractTestRunConfiguration}
     */
    if (runConfiguration instanceof AbstractTestRunConfiguration) {
      Arrays.stream(EP_NAME.getExtensions()).forEach(s -> {
        try {
          s.logTestRunner(TestRunnerType.SCALA_RUNNER);
        }
        catch (Throwable t) {
          LOG.info(t);
        }
      });
    }
    /**
     * JUnit, Application, etc configuration inherit {@link CommonProgramRunConfigurationParameters}
     */
    else if (runConfiguration instanceof CommonProgramRunConfigurationParameters) {
      Arrays.stream(EP_NAME.getExtensions()).forEach(s -> {
        try {
          s.logTestRunner(TestRunnerType.JUNIT_RUNNER);
        }
        catch (Throwable t) {
          LOG.info(t);
        }
      });
    }
  }
}