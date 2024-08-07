package tech.intellispaces.core.annotation.processor.preprocessing;

import com.google.auto.service.AutoService;
import tech.intellispaces.annotations.AnnotatedTypeProcessor;
import tech.intellispaces.annotations.generator.ArtifactGenerator;
import tech.intellispaces.annotations.validator.AnnotatedTypeValidator;
import tech.intellispaces.core.annotation.Preprocessing;
import tech.intellispaces.core.annotation.processor.AnnotationProcessorFunctions;
import tech.intellispaces.javastatements.customtype.CustomType;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import java.util.List;
import java.util.Set;

@AutoService(Processor.class)
public class PreprocessingAnnotationProcessor extends AnnotatedTypeProcessor {

  public PreprocessingAnnotationProcessor() {
    super(Preprocessing.class, Set.of(ElementKind.CLASS, ElementKind.INTERFACE));
  }

  @Override
  protected boolean isApplicable(CustomType customType) {
    return AnnotationProcessorFunctions.isAutoGenerationEnabled(customType);
  }

  @Override
  protected AnnotatedTypeValidator getValidator() {
    return null;
  }

  @Override
  protected List<ArtifactGenerator> makeArtifactGenerators(CustomType customType, RoundEnvironment roundEnv) {
    return AnnotationProcessorFunctions.makePreprocessingArtifactGenerators(customType, roundEnv);
  }
}
