package tech.intellispacesframework.core.test.samples.system;

import tech.intellispacesframework.core.annotation.Projection;
import tech.intellispacesframework.core.annotation.Unit;

@Unit
public class UnitWithProjectionAndStringParameter {

  @Projection
  String projection(String value) {
    return "";
  }
}
