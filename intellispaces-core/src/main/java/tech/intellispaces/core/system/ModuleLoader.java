package tech.intellispaces.core.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class ModuleLoader {
  private static final ModuleDefaultFactory factory = new ModuleDefaultFactory();
  private static final ModuleValidator moduleValidator = new ModuleValidator();

  private static final Logger LOG = LoggerFactory.getLogger(ModuleLoader.class);

  public static void loadModule(Class<?> moduleClass, String[] args) {
    loadModule(List.of(moduleClass), args);
  }

  public static void loadModule(Class<?>... unitClasses) {
    loadModule(Arrays.stream(unitClasses).toList(), new String[0]);
  }

  public static void loadModule(List<Class<?>> unitClasses, String[] args) {
    ModuleDefault currentModule = Modules.currentSilently();
    if (currentModule != null) {
      LOG.warn("Current module has already been loaded into application. Current active module will be reloaded");
    }

    ModuleDefault newModule = factory.createModule(unitClasses);
    moduleValidator.validate(newModule);
    if (currentModule != null) {
      currentModule.stop();
      Modules.setCurrentModule(null);
    }
    Modules.setCurrentModule(newModule);

    newModule.start(args);
  }

  public static void unloadModule() {
    Module currentModule = Modules.currentSilently();
    if (currentModule != null) {
      currentModule.stop();
    }
    Modules.setCurrentModule(null);
  }
}
