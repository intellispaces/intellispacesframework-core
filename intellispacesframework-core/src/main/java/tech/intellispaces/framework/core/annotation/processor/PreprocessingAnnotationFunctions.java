package tech.intellispaces.framework.core.annotation.processor;

import tech.intellispaces.framework.core.annotation.Preprocessing;
import tech.intellispaces.framework.javastatements.JavaStatements;
import tech.intellispaces.framework.javastatements.statement.AnnotatedStatement;
import tech.intellispaces.framework.javastatements.statement.custom.CustomType;
import tech.intellispaces.framework.javastatements.statement.instance.AnnotationInstance;
import tech.intellispaces.framework.javastatements.statement.instance.ClassInstance;
import tech.intellispaces.framework.javastatements.statement.instance.Instance;

import javax.annotation.processing.RoundEnvironment;
import java.util.List;
import java.util.Optional;

public interface PreprocessingAnnotationFunctions {

  static List<AnnotationInstance> findPreprocessingAnnotations(
      CustomType customType, ArtifactTypes artifact, RoundEnvironment roundEnv
  ) {
    return findPreprocessingAnnotations(customType.canonicalName(), artifact, roundEnv);
  }

  static List<AnnotationInstance> findPreprocessingAnnotations(
      String canonicalName, ArtifactTypes artifact, RoundEnvironment roundEnv
  ) {
    return roundEnv.getElementsAnnotatedWith(Preprocessing.class).stream()
        .map(JavaStatements::statement)
        .map(stm -> (AnnotatedStatement) stm)
        .map(stm -> stm.selectAnnotation(Preprocessing.class.getCanonicalName()))
        .map(Optional::orElseThrow)
        .filter(ann -> isPreprocessingAnnotationOf(ann, canonicalName, artifact))
        .toList();
  }

  static List<CustomType> getPreprocessingClasses(AnnotationInstance preprocessingAnnotation) {
    return preprocessingAnnotation.elementValue("value").orElseThrow()
        .asArray().orElseThrow()
        .elements().stream()
        .map(Instance::asClass)
        .map(Optional::orElseThrow)
        .map(ClassInstance::type)
        .toList();
  }

  static boolean isPreprocessingAnnotationOf(
      AnnotationInstance preprocessingAnnotation, String canonicalClassName
  ) {
    List<CustomType> preprocessingClasses = getPreprocessingClasses(preprocessingAnnotation);
    for (CustomType aClass : preprocessingClasses) {
      if (canonicalClassName.equals(aClass.canonicalName())) {
        return true;
      }
    }
    return false;
  }

  static boolean isPreprocessingAnnotationOf(
      AnnotationInstance preprocessingAnnotation, String canonicalClassName, ArtifactTypes artifact
  ) {
    List<CustomType> preprocessingClasses = getPreprocessingClasses(preprocessingAnnotation);
    for (CustomType aClass : preprocessingClasses) {
      if (canonicalClassName.equals(aClass.canonicalName())) {
        if (artifact.name().equals(getPreprocessingArtifact(preprocessingAnnotation))) {
          return true;
        }
      }
    }
    return false;
  }

  static boolean isPreprocessingEnabled(AnnotationInstance preprocessingAnnotation) {
    Object enabled = preprocessingAnnotation.elementValue("enable").orElseThrow()
        .asPrimitive().orElseThrow()
        .value();
    return Boolean.TRUE == enabled;
  }

  static String getPreprocessingArtifact(AnnotationInstance preprocessingAnnotation) {
    return preprocessingAnnotation.elementValue("artifact").orElseThrow()
        .asString().orElseThrow()
        .value();
  }

  static Optional<CustomType> getPreprocessingExtendWith(AnnotationInstance preprocessingAnnotation) {
    return preprocessingAnnotation.elementValue("extendWith").orElseThrow()
        .asClass()
        .map(ClassInstance::type)
        .filter(c -> !Void.class.getCanonicalName().equals(c.canonicalName()));
  }
}