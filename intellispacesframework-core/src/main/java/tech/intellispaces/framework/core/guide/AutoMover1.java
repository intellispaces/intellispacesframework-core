package tech.intellispaces.framework.core.guide;

import tech.intellispaces.framework.core.exception.TraverseException;
import tech.intellispaces.framework.core.traverse.DeclarativePlan;
import tech.intellispaces.framework.core.traverse.TraverseExecutor;

/**
 * One-parametrized automatic mover guide.
 *
 * <p>Automatic guide builds the traverse plan itself.
 *
 * @param <S> source object handle type.
 * @param <B> backward object handle type.
 * @param <Q> qualifier handle type.
 */
public class AutoMover1<S, B, Q> extends AbstractMover1<S, B, Q> {
  private final String tid;
  private final TraverseExecutor traverseExecutor;
  private final DeclarativePlan declarativeTaskPlan;

  public AutoMover1(String tid, DeclarativePlan declarativeTaskPlan, TraverseExecutor traverseExecutor) {
    this.tid = tid;
    this.declarativeTaskPlan = declarativeTaskPlan;
    this.traverseExecutor = traverseExecutor;
  }

  @Override
  public String tid() {
    return tid;
  }

  @Override
  @SuppressWarnings("unchecked")
  public B move(S source, Q qualifier) throws TraverseException {
    return (B) declarativeTaskPlan.execute(source, qualifier, traverseExecutor);
  }
}
