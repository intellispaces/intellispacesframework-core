package tech.intellispacesframework.core.guide;

import tech.intellispacesframework.commons.exception.UnexpectedViolationException;
import tech.intellispacesframework.core.exception.TraverseException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Mover guide embedded to object handle.
 *
 * @param <S> mover source object type.
 * @param <Q> mover qualified type.
 */
public class EmbeddedMover1<S, Q> extends AbstractMover1<S, Q> {
  private final Class<S> objectHandleClass;
  private final Method moverMethod;

  public EmbeddedMover1(Class<S> objectHandleClass, Method moverMethod) {
    if (moverMethod.getParameterCount() != 1) {
      throw UnexpectedViolationException.withMessage("Embedded guide should have 1 parameter");
    }
    this.objectHandleClass = objectHandleClass;
    this.moverMethod = moverMethod;
  }

  @Override
  @SuppressWarnings("unchecked")
  public S move(S source, Q qualifier) throws TraverseException {
    try {
      return (S) moverMethod.invoke(source, qualifier);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw TraverseException.withCauseAndMessage(e, "Failed to invoke mover method {} of object handle {}",
          moverMethod.getName(), objectHandleClass.getCanonicalName());
    }
  }
}
