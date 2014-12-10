// Copyright 2014 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class PantsOutputMessage {
  private final int myStart;
  private final int myEnd;
  private final int myLineNumber;
  private final String myFilePath;
  private final boolean myError;

  public PantsOutputMessage(int start, int end, String filePath, int lineNumber) {
    this(start, end, filePath, lineNumber, false);
  }

  public PantsOutputMessage(int start, int end, String filePath, int lineNumber, boolean isError) {
    myStart = start;
    myEnd = end;
    myFilePath = filePath;
    myLineNumber = lineNumber;
    myError = isError;
  }

  public int getStart() {
    return myStart;
  }

  public int getEnd() {
    return myEnd;
  }

  public int getLineNumber() {
    return myLineNumber;
  }

  public String getFilePath() {
    return myFilePath;
  }

  public boolean isError() {
    return myError;
  }

  @Override
  public String toString() {
    return "PantsOutputMessage{" +
           "start=" + myStart +
           ", end=" + myEnd +
           ", lineNumber=" + myLineNumber +
           ", filePath='" + myFilePath + '\'' +
           '}';
  }

  @Nullable
  public static PantsOutputMessage parseOutputMessage(@NotNull String message) {
    return parseMessage(message, false, false);
  }

  /**
   * @param line of an output
   * @param onlyCompilerMessages will look only for compiler specific message e.g. with a log level
   * @param checkFileExistence will check if the parsed {@code myFilePath} exists
   */
  @Nullable
  public static PantsOutputMessage parseMessage(@NotNull String line, boolean onlyCompilerMessages, boolean checkFileExistence) {
    int i = 0;
    final boolean isError = line.contains("[error]");
    if (isError || line.contains("[warning]") || line.contains("[debug]")) {
      i = line.indexOf(']') + 1;
    } else if (onlyCompilerMessages) {
      return null;
    }
    while (i < line.length() && (Character.isSpaceChar(line.charAt(i)) || line.charAt(i) == '\t')) {
      ++i;
    }
    final int start = i;
    while (i < line.length() && line.charAt(i) != ' ' && line.charAt(i) != '\n' && line.charAt(i) != ':') {
      ++i;
    }
    int end = i;
    i++;
    final String filePath = line.substring(start, end);
    if (checkFileExistence && !(new File(filePath).exists())) {
      return null;
    }
    while (i < line.length() && Character.isDigit(line.charAt(i))) {
      ++i;
    }
    int lineNumber = 0;
    try {
      lineNumber = Integer.parseInt(line.substring(end + 1, i)) - 1;
      end = i;
    }
    catch (Exception ignored) {
    }
    return new PantsOutputMessage(start, end, filePath, lineNumber, isError);
  }
}
