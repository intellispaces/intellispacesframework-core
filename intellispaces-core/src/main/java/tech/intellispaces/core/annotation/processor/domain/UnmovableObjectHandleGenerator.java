package tech.intellispaces.core.annotation.processor.domain;

import tech.intellispaces.commons.exception.UnexpectedViolationException;
import tech.intellispaces.core.common.NameConventionFunctions;
import tech.intellispaces.core.object.ObjectHandleTypes;
import tech.intellispaces.core.object.UnmovableObjectHandle;
import tech.intellispaces.core.space.domain.DomainFunctions;
import tech.intellispaces.javastatements.customtype.CustomType;
import tech.intellispaces.javastatements.reference.CustomTypeReference;

import javax.annotation.processing.RoundEnvironment;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UnmovableObjectHandleGenerator extends AbstractDomainObjectHandleGenerator {
  private String baseObjectHandle;
  private boolean isAlias;
  private String primaryObjectHandle;

  public UnmovableObjectHandleGenerator(CustomType domainType) {
    super(domainType);
  }

  @Override
  public String getArtifactName() {
    return NameConventionFunctions.getUnmovableObjectHandleTypename(annotatedType.className());
  }

  @Override
  protected ObjectHandleTypes getObjectHandleType() {
    return ObjectHandleTypes.Unmovable;
  }

  @Override
  protected String templateName() {
    return "/unmovable_object_handle.template";
  }

  protected Map<String, Object> templateVariables() {
    Map<String, Object> vars = new HashMap<>();
    vars.put("generatedAnnotation", makeGeneratedAnnotation());
    vars.put("packageName", context.packageName());
    vars.put("importedClasses", context.getImports());
    vars.put("sourceClassName", sourceClassCanonicalName());
    vars.put("sourceClassSimpleName", sourceClassSimpleName());
    vars.put("movableClassSimpleName", movableClassSimpleName());
    vars.put("classSimpleName", context.generatedClassSimpleName());
    vars.put("domainTypeParamsFull", domainTypeParamsFull);
    vars.put("domainTypeParamsBrief", domainTypeParamsBrief);
    vars.put("baseObjectHandle", baseObjectHandle);
    vars.put("domainMethods", methods);
    vars.put("unmovableObjectHandleName", context.addToImportAndGetSimpleName(UnmovableObjectHandle.class));
    vars.put("isAlias", isAlias);
    vars.put("primaryObjectHandle", primaryObjectHandle);
    return vars;
  }

  @Override
  protected boolean analyzeAnnotatedType(RoundEnvironment roundEnv) {
    context.generatedClassCanonicalName(getArtifactName());
    if (annotatedType.isNested()) {
      context.addImport(sourceClassCanonicalName());
    }
    context.addImport(UnexpectedViolationException.class);
    context.addImport(UnmovableObjectHandle.class);

    domainTypeParamsFull = annotatedType.typeParametersFullDeclaration();
    domainTypeParamsBrief = annotatedType.typeParametersBriefDeclaration();
    baseObjectHandle = context.addToImportAndGetSimpleName(
        NameConventionFunctions.getCommonObjectHandleTypename(annotatedType.className())
    );
    analyzeObjectHandleMethods(annotatedType, roundEnv);

    Optional<CustomTypeReference> primaryDomain = DomainFunctions.getPrimaryDomainForAliasDomain(annotatedType);
    isAlias = primaryDomain.isPresent();
    if (isAlias) {
      primaryObjectHandle = getObjectHandleDeclaration(primaryDomain.get(), ObjectHandleTypes.Unmovable);
    }
    return true;
  }
}
