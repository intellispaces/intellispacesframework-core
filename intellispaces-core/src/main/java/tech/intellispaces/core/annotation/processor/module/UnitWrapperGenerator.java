package tech.intellispaces.core.annotation.processor.module;

import tech.intellispaces.actions.common.string.StringActions;
import tech.intellispaces.actions.executor.Executor;
import tech.intellispaces.commons.exception.UnexpectedViolationException;
import tech.intellispaces.commons.type.TypeFunctions;
import tech.intellispaces.core.annotation.Projection;
import tech.intellispaces.core.annotation.ProjectionDefinition;
import tech.intellispaces.core.annotation.Wrapper;
import tech.intellispaces.core.annotation.processor.AbstractGenerator;
import tech.intellispaces.core.common.NameConventionFunctions;
import tech.intellispaces.core.system.Injection;
import tech.intellispaces.core.system.Modules;
import tech.intellispaces.core.system.ProjectionInjection;
import tech.intellispaces.core.system.ProjectionInjectionImpl;
import tech.intellispaces.core.system.ProjectionRegistry;
import tech.intellispaces.core.system.UnitWrapper;
import tech.intellispaces.javastatements.customtype.CustomType;
import tech.intellispaces.javastatements.instance.AnnotationInstance;
import tech.intellispaces.javastatements.instance.ClassInstance;
import tech.intellispaces.javastatements.instance.Instance;
import tech.intellispaces.javastatements.method.MethodParam;
import tech.intellispaces.javastatements.method.MethodStatement;
import tech.intellispaces.javastatements.reference.TypeReference;

import javax.annotation.processing.RoundEnvironment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class UnitWrapperGenerator extends AbstractGenerator {
  private final List<Map<String, Object>> methodsProperties = new ArrayList<>();
  private final List<Map<String, String>> projectionProvidersProperties = new ArrayList<>();
  private final List<Map<String, String>> projectionInjectionsProperties = new ArrayList<>();

  public UnitWrapperGenerator(CustomType annotatedType) {
    super(annotatedType);
  }

  @Override
  public String getArtifactName() {
    return NameConventionFunctions.getUnitWrapperCanonicalName(annotatedType.className());
  }

  @Override
  protected String templateName() {
    return "/unit_wrapper.template";
  }

  @Override
  protected Map<String, Object> templateVariables() {
    return Map.of(
        "generatedAnnotation", makeGeneratedAnnotation(),
        "packageName", context.packageName(),
        "sourceClassName", sourceClassCanonicalName(),
        "sourceClassSimpleName", sourceClassSimpleName(),
        "classSimpleName", context.generatedClassSimpleName(),
        "importedClasses", context.getImports(),
        "projectionDefinitions", projectionProvidersProperties,
        "projectionInjections", projectionInjectionsProperties,
        "methods", methodsProperties
    );
  }

  @Override
  protected boolean analyzeAnnotatedType(RoundEnvironment roundEnv) {
    context.generatedClassCanonicalName(getArtifactName());
    if (annotatedType.isNested()) {
      context.addImport(sourceClassCanonicalName());
    }

    context.addImport(List.class);
    context.addImport(Collections.class);
    context.addImport(ArrayList.class);
    context.addImport(Wrapper.class);
    context.addImport(Modules.class);
    context.addImport(Injection.class);
    context.addImport(ProjectionRegistry.class);
    context.addImport(TypeFunctions.class);
    context.addImport(UnitWrapper.class);

    analyzeMethods();
    return true;
  }

  private void analyzeMethods() {
    for (MethodStatement method : annotatedType.declaredMethods()) {
      if (isProjectionMethod(method)) {
        addProjectionOverrideMethod(method);
        if (method.isAbstract()) {
          addProjectionImplementationMethod(method);
        } else {
          addProjectionProxyMethod(method);
        }
      } else {
        if (method.isAbstract()) {
          addInjectionImplementationMethod(method);
        }
      }
    }
  }

  private void addProjectionImplementationMethod(MethodStatement method) {
    for (AnnotationInstance methodAnnotation : method.annotations()) {
      Optional<AnnotationInstance> projectionDefinitionAnnotation = methodAnnotation.annotationStatement().selectAnnotation(
          ProjectionDefinition.class.getCanonicalName());
      if (projectionDefinitionAnnotation.isPresent()) {
        AnnotationInstance projectionDefinition = projectionDefinitionAnnotation.get();
        Optional<Instance> providerClass = projectionDefinition.elementValue("provider");
        if (providerClass.isPresent()) {
          addProjectionImplementationMethodWhenProjectionProviderDefined(
              method, providerClass.get().asClass().orElseThrow());
           return;
        }
      }
    }
    throw UnexpectedViolationException.withMessage("Projection definition is not found. See method {} in unit {}",
        method.name(), method.owner().canonicalName());
  }

  private void addProjectionImplementationMethodWhenProjectionProviderDefined(
      MethodStatement method, ClassInstance projectionProvider
  ) {
    String projectionName = method.name();
    CustomType providerType = projectionProvider.type();

    context.addImport(providerType.canonicalName());
    String providerTypename = context.simpleNameOf(projectionProvider.type().canonicalName());

    String projectionType = method.returnType().orElseThrow().actualDeclaration();

    Map<String, String> projectionProviderProperties = new HashMap<>();
    projectionProviderProperties.put("name", projectionName);
    projectionProviderProperties.put("type", projectionType);
    projectionProviderProperties.put("providerType", providerTypename);
    projectionProvidersProperties.add(projectionProviderProperties);

    Map<String, Object> methodProperties = new HashMap<>();
    methodProperties.put("signature", buildMethodSignature(method, "_" + method.name()));
    methodProperties.put("body", buildProjectionProviderMethodBody(projectionName, projectionType));
    methodProperties.put("annotations", List.of(copyProjectionAnnotation(method)));
    methodProperties.put("javadoc", "");
    methodsProperties.add(methodProperties);
  }

  private void addInjectionImplementationMethod(MethodStatement method) {
    context.addImport(Modules.class);
    context.addImport(ProjectionInjection.class);
    context.addImport(ProjectionInjectionImpl.class);

    String injectionName = method.name();
    String injectionType = method.returnType().get().actualDeclaration();

    Map<String, String> injectionProperties = new HashMap<>();
    injectionProperties.put("name", injectionName);
    injectionProperties.put("type", injectionType);
    projectionInjectionsProperties.add(injectionProperties);

    Map<String, Object> methodProperties = new HashMap<>();
    methodProperties.put("signature", buildMethodSignature(method));
    methodProperties.put("body", buildInjectionMethodBody(injectionName, injectionType));
    methodProperties.put("annotations", List.of(Override.class.getSimpleName()));
    methodProperties.put("javadoc", "");
    methodsProperties.add(methodProperties);
  }

  private void addProjectionOverrideMethod(MethodStatement projectionMethod) {
    Map<String, Object> methodProperties = new HashMap<>();

    var signature = new StringBuilder();
    TypeReference returnType = projectionMethod.returnType().get();
    context.addImports(returnType.dependencyTypenames());
    signature.append(returnType.actualDeclaration());
    signature.append(" ");
    signature.append(projectionMethod.name());
    signature.append("(");

    Executor commaAppender = StringActions.commaAppender(signature);
    for (MethodParam param : projectionMethod.params()) {
      commaAppender.execute();
      context.addImports(param.type().dependencyTypenames());
      signature.append(param.type().actualDeclaration());
      signature.append(" ");
      signature.append(param.name());
    }
    signature.append(")");

    String exceptions = projectionMethod.exceptions().stream()
        .map(e -> e.asCustomTypeReference().orElseThrow().targetType())
        .peek(e -> context.addImport(e.canonicalName()))
        .map(e -> context.simpleNameOf(e.canonicalName()))
        .collect(Collectors.joining(", "));
    if (!exceptions.isEmpty()) {
      signature.append(" throws ").append(exceptions);
    }

    var body = new StringBuilder();
    if (projectionMethod.params().isEmpty()) {
      String injectionName = projectionMethod.name();
      String injectionType = projectionMethod.returnType().get().actualDeclaration();

      context.addImport(ProjectionInjection.class);
      context.addImport(ProjectionInjectionImpl.class);

      Map<String, String> injectionProperties = new HashMap<>();
      injectionProperties.put("name", injectionName);
      injectionProperties.put("type", injectionType);
      projectionInjectionsProperties.add(injectionProperties);

      body.append(buildInjectionMethodBody(injectionName, injectionType));
    } else {
      context.addImport(UnexpectedViolationException.class.getCanonicalName());

      body.append("throw new ");
      body.append(UnexpectedViolationException.class.getSimpleName());
      body.append("();");
    }

    methodProperties.put("signature", signature.toString());
    methodProperties.put("body", body.toString());
    methodProperties.put("annotations", List.of(Override.class.getSimpleName()));
    methodProperties.put("javadoc", "");

    methodsProperties.add(methodProperties);
  }

  private void addProjectionProxyMethod(MethodStatement method) {
    Map<String, Object> methodProperties = new HashMap<>();

    var signature = new StringBuilder();
    var body = new StringBuilder();

    TypeReference returnType = method.returnType().get();
    context.addImports(returnType.dependencyTypenames());
    signature.append(returnType.actualDeclaration());
    signature.append(" ");
    signature.append("_").append(method.name());
    signature.append("(");

    body.append("return super.");
    body.append(method.name());
    body.append("(");

    Executor commaAppender = StringActions.commaAppender(signature)
        .then(StringActions.commaAppender(body));
    for (MethodParam param : method.params()) {
      commaAppender.execute();

      context.addImports(param.type().dependencyTypenames());
      signature.append(param.type().actualDeclaration());
      signature.append(" ");
      signature.append(param.name());

      body.append(" ");
      body.append(param.name());
    }
    signature.append(")");
    body.append(");");

    String exceptions = method.exceptions().stream()
        .map(e -> e.asCustomTypeReference().orElseThrow().targetType())
        .peek(e -> context.addImport(e.canonicalName()))
        .map(e -> context.simpleNameOf(e.canonicalName()))
        .collect(Collectors.joining(", "));
    if (!exceptions.isEmpty()) {
      signature.append(" throws ").append(exceptions);
    }

    methodProperties.put("signature", signature.toString());
    methodProperties.put("body", body.toString());
    methodProperties.put("annotations", List.of(copyProjectionAnnotation(method)));
    methodProperties.put("javadoc", """
      /**
       * Proxy method for projection '%s'.
       */
      """.formatted(method.name()));

    methodsProperties.add(methodProperties);
  }

  private String copyProjectionAnnotation(MethodStatement method) {
    context.addImport(Projection.class.getCanonicalName());
    return context.simpleNameOf(Projection.class.getCanonicalName()) +
        "(" + makeProjectionAnnotationAttributes(method) + ")";
  }

  private String makeProjectionAnnotationAttributes(MethodStatement method) {
    Projection annotation = method.selectAnnotation(Projection.class).orElseThrow();

    var sb = new StringBuilder();
    sb.append("value = \"").append(!annotation.value().isEmpty() ? annotation.value() : method.name());
    sb.append("\", ");
    sb.append("lazy = ").append(annotation.lazy());
    return sb.toString();
  }

  private String buildProjectionProviderMethodBody(String projectionName, String projectionTypename) {
    return "return (" + projectionTypename + ") _" + projectionName + "Provider.get();";
  }

  private String buildInjectionMethodBody(String injectionName, String injectionType) {
    return "return (" + injectionType + ") _" + injectionName + "Injection.value();";
  }

  private boolean isProjectionMethod(MethodStatement method) {
    return method.selectAnnotation(Projection.class).isPresent();
  }
}
