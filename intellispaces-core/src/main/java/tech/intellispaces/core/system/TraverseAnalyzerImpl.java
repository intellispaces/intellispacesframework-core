package tech.intellispaces.core.system;

import tech.intellispaces.commons.exception.UnexpectedViolationException;
import tech.intellispaces.core.guide.Guide;
import tech.intellispaces.core.guide.GuideKind;
import tech.intellispaces.core.guide.GuideKinds;
import tech.intellispaces.core.guide.n0.Guide0;
import tech.intellispaces.core.guide.n1.Guide1;
import tech.intellispaces.core.object.ObjectFunctions;
import tech.intellispaces.core.traverse.ActualPlan;
import tech.intellispaces.core.traverse.CallGuide0PlanImpl;
import tech.intellispaces.core.traverse.CallGuide1PlanImpl;
import tech.intellispaces.core.traverse.MapObjectHandleThruTransition0Plan;
import tech.intellispaces.core.traverse.MapObjectHandleThruTransition0PlanImpl;
import tech.intellispaces.core.traverse.MapObjectHandleThruTransition1Plan;
import tech.intellispaces.core.traverse.MapObjectHandleThruTransition1PlanImpl;
import tech.intellispaces.core.traverse.MapObjectHandleThruTransition2Plan;
import tech.intellispaces.core.traverse.MapObjectHandleThruTransition2PlanImpl;
import tech.intellispaces.core.traverse.MoveObjectHandleThruTransition0Plan;
import tech.intellispaces.core.traverse.MoveObjectHandleThruTransition0PlanImpl;
import tech.intellispaces.core.traverse.MoveObjectHandleThruTransition1Plan;
import tech.intellispaces.core.traverse.MoveObjectHandleThruTransition1PlanImpl;
import tech.intellispaces.core.traverse.MoveObjectHandleThruTransition2Plan;
import tech.intellispaces.core.traverse.MoveObjectHandleThruTransition2PlanImpl;
import tech.intellispaces.core.traverse.ObjectHandleTraversePlan;
import tech.intellispaces.core.traverse.TraverseAnalyzer;
import tech.intellispaces.core.traverse.TraversePlanTypes;

import java.util.ArrayList;
import java.util.List;

public class TraverseAnalyzerImpl implements TraverseAnalyzer {
  private final ModuleGuideRegistry moduleGuideRegistry;
  private final AttachedGuideRegistry attachedGuideRegistry;

  public TraverseAnalyzerImpl(ModuleGuideRegistry moduleGuideRegistry, AttachedGuideRegistry attachedGuideRegistry) {
    this.moduleGuideRegistry = moduleGuideRegistry;
    this.attachedGuideRegistry = attachedGuideRegistry;
  }

  @Override
  public MapObjectHandleThruTransition0Plan buildTraversePlanMapObjectHandleThruTransition0(
      Class<?> objectHandleClass, String tid
  ) {
    MapObjectHandleThruTransition0Plan declarativePlan = new MapObjectHandleThruTransition0PlanImpl(
        objectHandleClass, tid);
    setActualPlan(declarativePlan, objectHandleClass);
    return declarativePlan;
  }

  @Override
  public MapObjectHandleThruTransition1Plan buildTraversePlanMapObjectHandleThruTransition1(
      Class<?> objectHandleClass, String tid
  ) {
    MapObjectHandleThruTransition1Plan declarativePlan = new MapObjectHandleThruTransition1PlanImpl(
        objectHandleClass, tid);
    setActualPlan(declarativePlan, objectHandleClass);
    return declarativePlan;
  }

  @Override
  public MapObjectHandleThruTransition2Plan buildTraversePlanMapObjectHandleThruTransition2(
      Class<?> objectHandleClass, String tid
  ) {
    MapObjectHandleThruTransition2Plan declarativePlan = new MapObjectHandleThruTransition2PlanImpl(
        objectHandleClass, tid);
    setActualPlan(declarativePlan, objectHandleClass);
    return declarativePlan;
  }

  @Override
  public MoveObjectHandleThruTransition0Plan buildTraversePlanMoveObjectHandleThruTransition0(
      Class<?> objectHandleClass, String tid
  ) {
    MoveObjectHandleThruTransition0Plan declarativePlan = new MoveObjectHandleThruTransition0PlanImpl(
        objectHandleClass, tid);
    setActualPlan(declarativePlan, objectHandleClass);
    return declarativePlan;
  }

  @Override
  public MoveObjectHandleThruTransition1Plan buildTraversePlanMoveObjectHandleThruTransition1(
      Class<?> objectHandleClass, String tid
  ) {
    MoveObjectHandleThruTransition1Plan declarativePlan = new MoveObjectHandleThruTransition1PlanImpl(
        objectHandleClass, tid);
    setActualPlan(declarativePlan, objectHandleClass);
    return declarativePlan;
  }

  @Override
  public MoveObjectHandleThruTransition2Plan buildTraversePlanMoveObjectHandleThruTransition2(
      Class<?> objectHandleClass, String tid
  ) {
    MoveObjectHandleThruTransition2Plan declarativePlan = new MoveObjectHandleThruTransition2PlanImpl(
        objectHandleClass, tid);
    setActualPlan(declarativePlan, objectHandleClass);
    return declarativePlan;
  }

  private void setActualPlan(ObjectHandleTraversePlan plan, Class<?> objectHandleClass) {
    ActualPlan actualPlan = getActualTraversePlan(plan, objectHandleClass);
    if (actualPlan != null) {
      plan.addActualPlan(objectHandleClass, actualPlan);
    }
  }

  @Override
  public ActualPlan getActualTraversePlan(ObjectHandleTraversePlan plan, Class<?> objectHandleClass) {
    if (!ObjectFunctions.isObjectHandleClass(objectHandleClass)) {
      throw UnexpectedViolationException.withMessage("Traverse plan of type {} expected object handle to input",
          plan.type());
    }
    if (objectHandleClass != plan.objectHandleClass() && !plan.objectHandleClass().isAssignableFrom(objectHandleClass)) {
      throw UnexpectedViolationException.withMessage("Traverse plan of type {} expected object handle of class {} " +
          "or it subclass", plan.objectHandleClass());
    }

    ActualPlan actualPlan = plan.getActualPlan(plan.objectHandleClass());
    if (actualPlan == null) {
      actualPlan = plan.getActualPlan(objectHandleClass);
    }
    if (actualPlan == null) {
      actualPlan = buildActualTraversePlan(plan, plan.objectHandleClass());
      if (actualPlan != null) {
        plan.addActualPlan(plan.objectHandleClass(), actualPlan);
      } else {
        actualPlan = buildActualTraversePlan(plan, objectHandleClass);
        if (actualPlan != null) {
          plan.addActualPlan(objectHandleClass, actualPlan);
        }
      }
    }
    return actualPlan;
  }

  private ActualPlan buildActualTraversePlan(ObjectHandleTraversePlan plan, Class<?> objectHandleClass) {
    GuideKinds guideKind = getGuideKind((TraversePlanTypes) plan.type());
    List<Guide<?, ?>> guides = findGuides(objectHandleClass, guideKind, plan.tid());
    if (guides.isEmpty()) {
      return null;
    }
    if (guides.size() > 1) {
      throw new UnsupportedOperationException("Not implemented");
    }
    return switch (guideKind) {
      case Mapper0, Mover0 -> new CallGuide0PlanImpl((Guide0<?, ?>) guides.get(0));
      case Mapper1, Mover1 -> new CallGuide1PlanImpl((Guide1<?, ?, ?>) guides.get(0));
      default -> throw new UnsupportedOperationException("Not implemented");
    };
  }

  private List<Guide<?, ?>> findGuides(Class<?> objectHandleClass, GuideKind kind, String tid) {
    var guides = new ArrayList<Guide<?, ?>>();
    Guide<?, ?> attachedGuide = attachedGuideRegistry.getGuide(objectHandleClass, kind, tid);
    if (attachedGuide != null) {
      guides.add(attachedGuide);
    }
    guides.addAll(moduleGuideRegistry.findGuides(kind, tid));
    return guides;
  }

  private GuideKinds getGuideKind(TraversePlanTypes planType) {
    return switch (planType) {
      case MapObjectHandleThruTransition0 -> GuideKinds.Mapper0;
      case MapObjectHandleThruTransition1 -> GuideKinds.Mapper1;
      case MoveObjectHandleThruTransition0 -> GuideKinds.Mover0;
      case MoveObjectHandleThruTransition1 -> GuideKinds.Mover1;
      default -> throw new UnsupportedOperationException("Not implemented");
    };
  }
}
