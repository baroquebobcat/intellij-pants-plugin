// Copyright 2014 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.integration.custom;


import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.Parameterized;
import com.intellij.testFramework.Parameterized.Parameters;
import com.twitter.intellij.pants.integration.oss.OSSPantsIntegrationTest;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@RunWith(Parameterized.class)
public class CustomProjectImportTests extends OSSPantsIntegrationTest {

  private static final String PROJECT_LIST = "project.config.list";

  private String projectLocation;
  private static String projectConfigFileName;

  public CustomProjectImportTests(String location) {
    projectLocation = location;
    projectConfigFileName = System.getProperty(PROJECT_LIST);
  }

  @Parameters(name="{0}")
  public static Iterable<String> generateParameters() throws IOException {
    assertNotNull(projectConfigFileName);
    File projectConfigFile = new File(projectConfigFileName);
    assertExists(projectConfigFile);
    final String[] testProjectList = StringUtil.splitByLines(FileUtil.loadFile(projectConfigFile));
    return Arrays.asList(testProjectList);
  }

  public void testProject() {
    doImport(projectLocation);
    compileProject();
  }
}
