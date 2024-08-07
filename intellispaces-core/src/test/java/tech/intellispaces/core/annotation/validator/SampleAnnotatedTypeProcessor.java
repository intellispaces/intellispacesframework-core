package tech.intellispaces.core.annotation.validator;

import tech.intellispaces.annotations.AnnotatedTypeProcessor;
import tech.intellispaces.annotations.generator.ArtifactGenerator;
import tech.intellispaces.annotations.validator.AnnotatedTypeValidator;
import tech.intellispaces.javastatements.customtype.CustomType;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import java.util.List;
import java.util.Set;

public class SampleAnnotatedTypeProcessor extends AnnotatedTypeProcessor {
  private CustomType annotatedType;

  public SampleAnnotatedTypeProcessor() {
    super(Sample.class, Set.of(ElementKind.INTERFACE, ElementKind.CLASS));
  }

  public CustomType getAnnotatedType() {
    return annotatedType;
  }

  @Override
  protected boolean isApplicable(CustomType annotatedType) {
    this.annotatedType = annotatedType;
    return false;
  }

  @Override
  protected AnnotatedTypeValidator getValidator() {
    return null;
  }

  @Override
  protected List<ArtifactGenerator> makeArtifactGenerators(
      CustomType annotatedType, RoundEnvironment roundEnv
  ) {
    return List.of();
  }
}
