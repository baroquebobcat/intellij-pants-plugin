// Copyright 2014 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.testFramework.runner;


import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.twitter.intellij.pants.testFramework.PantsIntegrationTestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class CustomProjectIntegrationTests extends PantsIntegrationTestCase {

  private static final String PROJECT_LIST = "project.config.list";
  private static final String CUSTOM_PROJECT_WS = "project.workspace";

  @org.junit.runners.Parameterized.Parameter()
  private String target;

  public CustomProjectIntegrationTests(@NotNull String name, @NotNull String target) {
    this.target = target;
    this.setName(name);
  }

  @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> getProjectList(){
    String projectConfigFileName = System.getProperty(PROJECT_LIST);
    assertNotNull(projectConfigFileName);
    File projectConfigFile = new File(projectConfigFileName);
    assertExists(projectConfigFile);
    try{
     final String[] testProjectList = StringUtil.splitByLines(FileUtil.loadFile(projectConfigFile));
      return ContainerUtil.map(
        testProjectList, new Function<String, Object[]>() {
          @Override
          public Object[] fun(String file) {
            return new Object[]{file, file};
          }
        }
      );
    } catch (IOException e) {
       assertTrue("File not found", false);
    }
    return null;
  }

  @Test
  public void testProject() {
    assertNotNull(target);
    //doImport(projectLocation);
    //compileProject();
  }

  @Override
  protected List<File> getProjectFoldersToCopy() {
    final String projectWorkspace = System.getProperty(CUSTOM_PROJECT_WS);
    System.out.println("this is " + projectWorkspace);
    assertNotNull(projectWorkspace);
    ArrayList<File> folders = new ArrayList<File>();
    File projectWorkspaceFolder = new File(projectWorkspace);
    assertExists(projectWorkspaceFolder);
    folders.add(projectWorkspaceFolder);
    return folders;
  }
}
