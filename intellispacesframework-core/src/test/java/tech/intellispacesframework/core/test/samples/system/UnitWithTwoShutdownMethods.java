package tech.intellispacesframework.core.test.samples.system;

import tech.intellispacesframework.core.annotation.Shutdown;
import tech.intellispacesframework.core.annotation.Unit;

@Unit
public class UnitWithTwoShutdownMethods {

  @Shutdown
  public void shutdown1() {

  }

  @Shutdown
  public void shutdown2() {

  }
}