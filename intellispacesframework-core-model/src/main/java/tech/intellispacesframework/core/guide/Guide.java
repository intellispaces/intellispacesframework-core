package tech.intellispacesframework.core.guide;

import tech.intellispacesframework.core.exception.TraverseException;

/**
 * Guide.
 *
 * <p>Guide is a nano-system designed for processing objects. Guide can map or move objects.
 *
 * <p>Guides splits code into lightweight systems.
 *
 * <p>The guide is object. Guide can be constructed from other guides.
 *
 * @param <S> source object type (domain or object handle).
 * @param <T> target object type (domain or object handle).
 */
public interface Guide<S, T> {

  boolean isMapper();

  boolean isMover();

  /**
   * Synchronous execution of the guide.
   *
   * @param source source object.
   * @param qualifiers guide qualifiers.
   * @return target object.
   * @throws TraverseException throws if guide was started, but can't traverse source object.
   */
  T traverse(S source, Object... qualifiers) throws TraverseException;
}