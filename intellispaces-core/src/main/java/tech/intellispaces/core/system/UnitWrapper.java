package tech.intellispaces.core.system;

import tech.intellispaces.commons.exception.UnexpectedViolationException;
import tech.intellispaces.core.annotation.Projection;
import tech.intellispaces.core.annotation.Wrapper;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

public interface UnitWrapper {

  List<Injection> getInjections();

  static Method getActualMethod(Method wrapperMethod) {
    try {
      Class<?> wrapperClass = wrapperMethod.getDeclaringClass();
      if (!wrapperClass.isAnnotationPresent(Wrapper.class)) {
        throw UnexpectedViolationException.withMessage("Expected unit wrapper class");
      }
      Class<?> unitClass = wrapperClass.getSuperclass();

      if (wrapperMethod.isAnnotationPresent(Projection.class) && wrapperMethod.getName().startsWith("_")) {
        return unitClass.getMethod(wrapperMethod.getName().substring(1),
            Arrays.stream(wrapperMethod.getParameters()).map(Parameter::getType).toArray(Class<?>[]::new));
      } else {
        return wrapperMethod;
      }
    } catch (NoSuchMethodException | SecurityException e) {
      throw UnexpectedViolationException.withCauseAndMessage(e, "Could not get actual method of unit wrapper method {}",
          wrapperMethod);
    }
  }
}
