package tech.intellispaces.core.object;

import tech.intellispaces.commons.exception.UnexpectedViolationException;
import tech.intellispaces.commons.type.TypeFunctions;
import tech.intellispaces.core.annotation.Data;
import tech.intellispaces.core.annotation.ObjectHandleBunch;
import tech.intellispaces.core.common.CoreFunctions;
import tech.intellispaces.core.common.NameConventionFunctions;
import tech.intellispaces.core.space.domain.DomainFunctions;
import tech.intellispaces.javastatements.JavaStatements;
import tech.intellispaces.javastatements.customtype.CustomType;
import tech.intellispaces.javastatements.reference.TypeReference;
import tech.intellispaces.javastatements.type.Type;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ObjectFunctions {

  private ObjectFunctions() {}

  public static boolean isDataObjectHandle(Class<?> objectHandleClass) {
    return getDomainClassOfObjectHandle(objectHandleClass).isAnnotationPresent(Data.class);
  }

  public static Class<?> getObjectHandleClass(ObjectHandleTypes objectHandleType) {
    return switch (objectHandleType) {
      case Common -> ObjectHandle.class;
      case Movable -> MovableObjectHandle.class;
      case Unmovable -> UnmovableObjectHandle.class;
      case Bunch -> throw UnexpectedViolationException.withMessage("Bunch object handle class is not defined");
    };
  }

  public static boolean isObjectHandleClass(Class<?> aClass) {
    return isDefaultObjectHandleClass(aClass) || isCustomObjectHandleClass(aClass);
  }

  public static boolean isObjectHandleType(TypeReference type) {
    return isDefaultObjectHandleType(type) || isCustomObjectHandleType(type);
  }

  public static boolean isDefaultObjectHandleClass(Class<?> aClass) {
    return DEFAULT_OBJECT_HANDLE_CLASSES.contains(aClass.getCanonicalName());
  }

  public static boolean isDefaultObjectHandleType(TypeReference type) {
    return type.isPrimitiveReference() ||
        (type.isCustomTypeReference() && isDefaultObjectHandleType(type.asCustomTypeReferenceOrElseThrow().targetType()));
  }

  public static boolean isDefaultObjectHandleType(CustomType type) {
    return isDefaultObjectHandleType(type.canonicalName());
  }

  public static boolean isDefaultObjectHandleType(String canonicalName) {
    return DEFAULT_OBJECT_HANDLE_CLASSES.contains(canonicalName);
  }

  public static boolean isMovableObjectHandle(CustomType objectHandleType) {
    return objectHandleType.hasParent(MovableObjectHandle.class.getCanonicalName());
  }

  public static String getBaseObjectHandleTypename(TypeReference domainType) {
    if (domainType.isPrimitiveReference()) {
      return TypeFunctions.getPrimitiveWrapperClass(
          domainType.asPrimitiveReferenceOrElseThrow().typename()).getCanonicalName();
    } else if (domainType.isNamedReference()) {
      return domainType.asNamedReferenceOrElseThrow().name();
    } else if (domainType.isWildcard()) {
      return Object.class.getCanonicalName();
    }
    return getBaseObjectHandleTypename(domainType.asCustomTypeReferenceOrElseThrow().targetType());
  }

  public static String getBaseObjectHandleTypename(CustomType domainType) {
    if (isDefaultObjectHandleType(domainType)) {
      return domainType.className();
    }
    return NameConventionFunctions.getCommonObjectHandleTypename(domainType.className());
  }

  public static String getObjectHandleTypename(CustomType customType, ObjectHandleTypes type) {
    if (isDefaultObjectHandleType(customType)) {
      return customType.canonicalName();
    }
    if (!DomainFunctions.isDomainType(customType)) {
      return customType.canonicalName();
    }
    return NameConventionFunctions.getObjectHandleTypename(customType.className(), type);
  }

  public static String getObjectHandleTypename(String canonicalName, ObjectHandleTypes type) {
    if (isDefaultObjectHandleType(canonicalName)) {
      return canonicalName;
    }
    return NameConventionFunctions.getObjectHandleTypename(canonicalName, type);
  }

  public static boolean isCustomObjectHandleClass(Class<?> aClass) {
    return ObjectHandle.class.isAssignableFrom(aClass);
  }

  public static boolean isCustomObjectHandleType(TypeReference type) {
    return type.isCustomTypeReference() && type.asCustomTypeReferenceOrElseThrow().targetType().hasParent(ObjectHandle.class);
  }

  public static Class<?> defineObjectHandleClass(Class<?> aClass) {
    return defineObjectHandleClassInternal(aClass);
  }

  private static Class<?> defineObjectHandleClassInternal(Class<?> aClass) {
    if (aClass.isAnnotationPresent(tech.intellispaces.core.annotation.MovableObjectHandle.class) ||
        aClass.isAnnotationPresent(tech.intellispaces.core.annotation.UnmovableObjectHandle.class) ||
        isDefaultObjectHandleClass(aClass)
    ) {
      return aClass;
    }
    if (aClass.getSuperclass() != null) {
      Class<?> result = defineObjectHandleClassInternal(aClass.getSuperclass());
      if (result != null) {
        return result;
      }
    }
    for (Class<?> anInterface : aClass.getInterfaces()) {
      Class<?> result = defineObjectHandleClassInternal(anInterface);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  public static boolean isCompatibleObjectType(Class<?> type1, Class<?> type2) {
    Class<?> actualType1 = TypeFunctions.getObjectClass(type1);
    Class<?> actualType2 = TypeFunctions.getObjectClass(type2);
    return actualType2 == actualType1 || actualType1.isAssignableFrom(actualType2);
  }

  public static CustomType getDomainTypeOfObjectHandle(TypeReference objectHandleType) {
    if (objectHandleType.isPrimitiveReference()) {
      Class<?> wrapperClass = TypeFunctions.getPrimitiveWrapperClass(objectHandleType.asPrimitiveReferenceOrElseThrow().typename());
      return JavaStatements.customTypeStatement(wrapperClass);
    } else if (objectHandleType.isCustomTypeReference()) {
      return getDomainTypeOfObjectHandle(objectHandleType.asCustomTypeReferenceOrElseThrow().targetType());
    } else {
      throw UnexpectedViolationException.withMessage("Not implemented");
    }
  }

  public static CustomType getDomainTypeOfObjectHandle(CustomType objectHandleType) {
    List<CustomType> objectHandleTypes = CoreFunctions.findTopAnnotatedTypes(
        objectHandleType, ObjectHandleBunch.class.getCanonicalName()
    );
    if (objectHandleTypes.isEmpty()) {
      throw UnexpectedViolationException.withMessage("Could not find object handle type of class {}",
          objectHandleType.canonicalName());
    } else if (objectHandleTypes.size() > 1) {
      throw UnexpectedViolationException.withMessage("More than one object handle type of class {} found",
          objectHandleType.canonicalName());
    }
    return objectHandleTypes.get(0).selectAnnotation(ObjectHandleBunch.class.getCanonicalName()).orElseThrow()
        .elementValue("value").orElseThrow()
        .asClass().orElseThrow()
        .type();
  }

  public static Class<?> getDomainClassOfObjectHandle(Class<?> objectHandleClass) {
    List<Class<?>> objectHandleTypes = CoreFunctions.findTopAnnotatedClasses(
        objectHandleClass, ObjectHandleBunch.class
    );
    if (objectHandleTypes.isEmpty()) {
      throw UnexpectedViolationException.withMessage("Could not find object handle type of class {}",
          objectHandleClass.getCanonicalName());
    } else if (objectHandleTypes.size() > 1) {
      throw UnexpectedViolationException.withMessage("More than one object handle type of class {} found",
          objectHandleClass.getCanonicalName());
    }
    return objectHandleTypes.get(0).getAnnotation(ObjectHandleBunch.class).value();
  }

  public static boolean isMovableObjectHandle(Class<?> objectHandleClass) {
    return MovableObjectHandle.class.isAssignableFrom(objectHandleClass);
  }

  @SuppressWarnings("unchecked")
  public static <T> T tryDowngrade(Object sourceObjectHandle, Class<T> targetObjectHandleClass) {
    Class<?> sourceObjectHandleClass = sourceObjectHandle.getClass();
    if (isCustomObjectHandleClass(sourceObjectHandleClass) && isCustomObjectHandleClass(targetObjectHandleClass)) {
      Class<?> sourceObjectHandleDomain = getDomainClassOfObjectHandle(sourceObjectHandleClass);
      Class<?> targetObjectHandleDomain = getDomainClassOfObjectHandle(targetObjectHandleClass);
      if (targetObjectHandleDomain.isAssignableFrom(sourceObjectHandleDomain)) {
        if (isMovableObjectHandle(targetObjectHandleClass)) {
          return (T) tryCreateDowngradeObjectHandle(sourceObjectHandle, sourceObjectHandleDomain, targetObjectHandleDomain);
        }
      }
    }
    return null;
  }

  private static Object tryCreateDowngradeObjectHandle(
      Object sourceObjectHandle, Class<?> sourceObjectHandleDomain, Class<?> targetObjectHandleDomain
  ) {
    String downgradeObjectHandleCanonicalName = NameConventionFunctions.getMovableDownwardObjectHandleTypename(
        sourceObjectHandleDomain, targetObjectHandleDomain);
    Optional<Class<?>> downgradeObjectHandleClass = TypeFunctions.getClass(downgradeObjectHandleCanonicalName);
    if (downgradeObjectHandleClass.isPresent()) {
      try {
        Constructor<?> constructor = downgradeObjectHandleClass.get().getConstructors()[0];
        return constructor.newInstance(sourceObjectHandle);
      } catch (Exception e) {
        throw UnexpectedViolationException.withCauseAndMessage(e, "Could not create downgrade object handle");
      }
    }
    return null;
  }

  private final static Set<String> DEFAULT_OBJECT_HANDLE_CLASSES = Set.of(
      boolean.class.getCanonicalName(),
      byte.class.getCanonicalName(),
      short.class.getCanonicalName(),
      int.class.getCanonicalName(),
      long.class.getCanonicalName(),
      float.class.getCanonicalName(),
      double.class.getCanonicalName(),
      char.class.getCanonicalName(),
      Object.class.getCanonicalName(),
      Boolean.class.getCanonicalName(),
      Byte.class.getCanonicalName(),
      Short.class.getCanonicalName(),
      Integer.class.getCanonicalName(),
      Long.class.getCanonicalName(),
      Float.class.getCanonicalName(),
      Double.class.getCanonicalName(),
      Character.class.getCanonicalName(),
      String.class.getCanonicalName(),
      Class.class.getCanonicalName(),
      Type.class.getCanonicalName(),
      Void.class.getCanonicalName()
  );

  public static Class<?> propertiesHandleClass() {
    if (propertiesHandleClass == null) {
      propertiesHandleClass = TypeFunctions.getClass(ObjectHandleConstants.PROPERTIES_HANDLE_CLASSNAME).orElseThrow(
          () -> UnexpectedViolationException.withMessage("Could not get class {}", ObjectHandleConstants.PROPERTIES_HANDLE_CLASSNAME)
      );
    }
    return propertiesHandleClass;
  }

  private static Class<?> propertiesHandleClass;
}
