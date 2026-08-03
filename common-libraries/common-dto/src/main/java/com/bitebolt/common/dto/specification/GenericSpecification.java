package com.bitebolt.common.dto.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Generic specification helper for constructing dynamic JPA Criteria predicates.
 * Allows filtering on target entity attributes dynamically without tight coupling to specific request parameter classes.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Inspection:</strong> Evaluates search query keyword and target attribute field name.
 *   <li><strong>Reflection:</strong> Inspects Java attribute type on entity root target.
 *   <li><strong>Predicate Construction:</strong> Constructs dynamic {@code LIKE} predicate for String attributes or conjunction predicate.
 * </ol>
 *
 * @param <E> Target Entity type.
 * @param <T> Search criteria payload type.
 */
public class GenericSpecification<E, T> implements Specification<E>
{
  private static final long serialVersionUID = 1L;

  /** Generic search criteria payload object. */
  private final T searchCriteria;

  /** Free-text search keyword query string. */
  private final String query;

  /** Target entity attribute field name for search. */
  private final String searchField;

  /**
   * Instantiate GenericSpecification with free-text search query and target field name.
   *
   * <p><strong>Standard Execution Steps:</strong>
   *
   * <ol>
   *   <li><strong>Assignment:</strong> Stores query string and target attribute field name.
   * </ol>
   *
   * @param query Search keyword query string.
   * @param searchField Target entity field name.
   */
  public GenericSpecification(String query, String searchField) {
    this.searchCriteria = null;
    this.query = query;
    this.searchField = searchField;
  }

  /**
   * Instantiate GenericSpecification with generic search criteria payload object.
   *
   * <p><strong>Standard Execution Steps:</strong>
   *
   * <ol>
   *   <li><strong>Assignment:</strong> Stores generic search criteria object reference.
   * </ol>
   *
   * @param searchCriteria Generic search criteria container object.
   */
  public GenericSpecification(T searchCriteria) {
    this.searchCriteria = searchCriteria;
    this.query = null;
    this.searchField = null;
  }

  /**
   * Build JPA criteria predicate for entity root query.
   *
   * <p><strong>Standard Execution Steps:</strong>
   *
   * <ol>
   *   <li><strong>Validation:</strong> Checks if query keyword and searchField are present.
   *   <li><strong>Predicate Generation:</strong> Generates lower-case case-insensitive {@code LIKE} predicate if attribute is a String.
   * </ol>
   *
   * @param root Root entity reference.
   * @param criteriaQuery CriteriaQuery query container.
   * @param criteriaBuilder CriteriaBuilder criteria builder factory.
   * @return JPA Predicate filter.
   */
  @Override
  public Predicate toPredicate(
      @NonNull Root<E> root,
      @Nullable CriteriaQuery<?> criteriaQuery,
      @NonNull CriteriaBuilder criteriaBuilder) {
    if (query == null || query.trim().isEmpty() || searchField == null || searchField.trim().isEmpty()) {
      return criteriaBuilder.conjunction();
    }

    try {
      Class<?> fieldType = root.get(searchField).getJavaType();

      if (fieldType != null && String.class.isAssignableFrom(fieldType)) {
        return criteriaBuilder.like(
            criteriaBuilder.lower(root.get(searchField).as(String.class)),
            "%" + query.trim().toLowerCase() + "%");
      }

      return criteriaBuilder.conjunction();

    } catch (IllegalArgumentException e) {
      return criteriaBuilder.conjunction();
    }
  }
}
