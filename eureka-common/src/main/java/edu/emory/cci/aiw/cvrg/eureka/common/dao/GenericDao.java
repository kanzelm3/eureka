/*
 * #%L
 * Eureka Common
 * %%
 * Copyright (C) 2012 - 2013 Emory University
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package edu.emory.cci.aiw.cvrg.eureka.common.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import javax.persistence.NoResultException;
import javax.persistence.criteria.Predicate;

/**
 * Implements the {@link Dao} interface in a generic way.
 *
 * @param <T> The type of the entity.
 * @param <PK> The type of the unique identifier for the entity.
 * @author hrathod
 */
public class GenericDao<T, PK> implements Dao<T, PK> {

	public enum SqlComparator {

		LESS_THAN_OR_EQUAL_TO,
		LESS_THAN,
		EQUAL_TO,
		NOT_EQUAL_TO,
		GREATER_THAN,
		GREATER_THAN_OR_EQUAL_TO
	}

	/**
	 * The class level logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(
			GenericDao.class);
	/**
	 * The type for the entities this DAO instance handles.
	 */
	private final Class<T> entityClass;
	/**
	 * Provides entity managers on demand.
	 */
	private final Provider<EntityManager> managerProvider;

	/**
	 * Creates a generic DAO that handles the given type of entity. The entity
	 * manager provider is used to fetch entity managers to interact with the
	 * data store.
	 *
	 * @param inEntityClass The type of entities to handle.
	 * @param inManagerProvider Provides entity managers on demand.
	 */
	protected GenericDao(Class<T> inEntityClass,
			Provider<EntityManager> inManagerProvider) {
		this.entityClass = inEntityClass;
		this.managerProvider = inManagerProvider;
	}

	@Override
	@Transactional
	public T create(T entity) {
		EntityManager entityManager = this.getEntityManager();
		entityManager.persist(entity);
		return entity;
	}

	@Override
	public T retrieve(PK uniqueId) {
		return this.getEntityManager().find(this.entityClass, uniqueId);
	}

	@Override
	@Transactional
	public T update(T entity) {
		EntityManager entityManager = this.getEntityManager();
		T result = entityManager.merge(entity);
		return result;
	}

	@Override
	@Transactional
	public T remove(T entity) {
		EntityManager entityManager = this.getEntityManager();
		if (entityManager.contains(entity)) {
			entityManager.remove(entity);
		} else {
			entityManager.remove(entityManager.merge(entity));
		}
		return entity;
	}

	@Override
	public T refresh(T entity) {
		this.getEntityManager().refresh(entity);
		return entity;
	}

	@Override
	public List<T> getAll() {
		DatabaseSupport dbSupport
				= new DatabaseSupport(getEntityManager());
		return dbSupport.getAll(this.entityClass);
	}

	protected List<T> getListAsc(SingularAttribute<T, ?> attribute) {
		EntityManager entityManager = this.getEntityManager();
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = builder.createQuery(this.entityClass);
		Root<T> root = criteriaQuery.from(this.entityClass);
		criteriaQuery.orderBy(builder.asc(root.get(attribute)));
		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	/**
	 * Provides a convenient way for subclasses to implement simple queries that
	 * compare an attribute of the entity to a target value.
	 *
	 * @param attribute The attribute of the entity to compare.
	 * @param value The target value of the given attribute.
	 * @param <Y> The type of the attribute and target value.
	 * @return A single result that matches the given criteria.
	 */
	protected <Y> T getUniqueByAttribute(SingularAttribute<T, Y> attribute,
			Y value) {
		try {
			return new DatabaseSupport(getEntityManager())
					.getUniqueByAttribute(this.entityClass, attribute, value);
		} catch (NoResultException ex) {
			return null;
		}
	}

	protected <Y> List<T> getListByAttribute(SingularAttribute<T, Y> attribute, Y value) {
		return new DatabaseSupport(getEntityManager())
				.getListByAttribute(this.entityClass, attribute, value);
	}

	protected <Y extends Number> List<T> getListByAttribute(
			SingularAttribute<T, Y> attribute,
			SqlComparator comparator,
			Y value) {
		TypedQuery<T> query = this.createTypedQuery(
				attribute, comparator, value);
		return query.getResultList();
	}

	private <Y extends Number> TypedQuery<T> createTypedQuery(
			SingularAttribute<T, Y> attribute,
			SqlComparator comparator, Y value) {
		EntityManager entityManager = this.getEntityManager();
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = builder.createQuery(this.entityClass);
		Root<T> root = criteriaQuery.from(this.entityClass);
		Path<Y> path = root.get(attribute);
		Predicate pred;
		switch (comparator) {
			case LESS_THAN:
				pred = builder.lt(path, value);
				break;
			case LESS_THAN_OR_EQUAL_TO:
				pred = builder.le(path, value);
				break;
			case EQUAL_TO:
				pred = builder.equal(path, value);
				break;
			case NOT_EQUAL_TO:
				pred = builder.notEqual(path, value);
				break;
			case GREATER_THAN_OR_EQUAL_TO:
				pred = builder.ge(path, value);
				break;
			case GREATER_THAN:
				pred = builder.gt(path, value);
				break;
			default:
				throw new AssertionError("Invalid SQLComparator: "
						+ comparator);
		}
		return entityManager.createQuery(criteriaQuery.where(
				builder.equal(path, value)));
	}

	/**
	 * Get a list of entities whose path value is the same as the given target
	 * value. The path is provided by the QueryPathProvider, and is followed
	 * through to get the resulting value. That resulting value is compared to
	 * the given target value in the query.
	 *
	 * @param provider Provides the path from the entity to the target
	 * attribute/column.
	 * @param value The target value to compare with the resulting attribute
	 * value.
	 * @param <Y> The type of the target value and resulting attribute/column
	 * value.
	 * @return A list of entities that match the given criteria.
	 */
	protected <Y> List<T> getListByAttribute(QueryPathProvider<T, Y> provider, Y value) {
		EntityManager entityManager = this.getEntityManager();
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = builder.createQuery(this.entityClass);
		Root<T> root = criteriaQuery.from(this.entityClass);
		Path<Y> path = provider.getPath(root, builder);
		TypedQuery<T> typedQuery = entityManager.createQuery(criteriaQuery.where(
				builder.equal(path, value)));
		return typedQuery.getResultList();
	}

	/**
	 * Returns an entity manager instance that can be used to interact with the
	 * data source.
	 *
	 * @return An instance of EntityManager.
	 */
	protected final EntityManager getEntityManager() {
		return this.managerProvider.get();
		//return this.entityManager;
	}

	/**
	 * Provides an interface for the subclasses to easily perform queries
	 * without having to deal with a lot of boiler-plate code. The subclasses
	 * can simply provide the path to a value and the target value using this
	 * interface, and have this superclass perform the query.
	 *
	 * @param <E> The entity type.
	 * @param <P> The target value and target column type.
	 */
	protected static interface QueryPathProvider<E, P> {

		/**
		 * Provides a path from the entity to the target attribute.
		 *
		 * @param root The query root, used to build the path.
		 * @param builder The criteria builder for the query.
		 * @return The path from the entity to the target attribute.
		 */
		Path<P> getPath(Root<E> root, CriteriaBuilder builder);
	}
}
