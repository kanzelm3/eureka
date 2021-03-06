/*
 * #%L
 * Eureka Services
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
package edu.emory.cci.aiw.cvrg.eureka.services.test;

import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.emory.cci.aiw.cvrg.eureka.common.authentication.AuthenticationMethod;
import edu.emory.cci.aiw.cvrg.eureka.common.authentication.LoginType;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.*;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.CategoryEntity.CategoryType;
import edu.emory.cci.aiw.cvrg.eureka.common.test.TestDataException;
import edu.emory.cci.aiw.cvrg.eureka.common.test.TestDataProvider;
import edu.emory.cci.aiw.cvrg.eureka.common.util.StringUtil;
import edu.emory.cci.aiw.cvrg.eureka.services.dao.AuthenticationMethodDao;
import edu.emory.cci.aiw.cvrg.eureka.services.dao.LoginTypeDao;
import edu.emory.cci.aiw.cvrg.eureka.services.entity.UserEntityFactory;

/**
 * Sets up the environment for testing, by bootstrapping the data store with
 * certain items to test against.
 *
 * @author hrathod
 */
public class Setup implements TestDataProvider {

	private static final String ORGANIZATION = "Emory University";
	private static final String PASSWORD = "testpassword";
	public static final String TESTING_TIME_UNIT_NAME = "test";
	public static final String TESTING_FREQ_TYPE_NAME = "at least";
	
	private final Provider<EntityManager> managerProvider;
	private Role researcherRole;
	private Role adminRole;
	private Role superRole;
	private final UserEntityFactory userEntityFactory;
	private List<LoginTypeEntity> loginTypes;
	private List<AuthenticationMethodEntity> authenticationMethods;


	/**
	 * Create a Bootstrap class with an EntityManager.
	 */
	@Inject
	Setup(Provider<EntityManager> inManagerProvider,
			LoginTypeDao inLoginTypeDao,
			AuthenticationMethodDao inAuthenticationMethodDao) {
		this.managerProvider = inManagerProvider;
		this.userEntityFactory = new UserEntityFactory(inLoginTypeDao, inAuthenticationMethodDao);
	}

	private EntityManager getEntityManager() {
		return this.managerProvider.get();
	}

	@Override
	public void setUp() throws TestDataException {
		this.researcherRole = this.createResearcherRole();
		this.adminRole = this.createAdminRole();
		this.superRole = this.createSuperRole();
		this.loginTypes = createLoginTypes();
		this.authenticationMethods = createAuthenticationMethods();
		UserEntity researcherUser = this.createResearcherUser();
		UserEntity adminUser = this.createAdminUser();
		UserEntity superUser = this.createSuperUser();
		this.createDataElements(researcherUser, adminUser, superUser);
		this.createTimeUnits();
		this.createFrequencyTypes();
	}

	@Override
	public void tearDown() throws TestDataException {
		this.remove(DataElementEntity.class);
		this.remove(UserEntity.class);
		this.remove(Role.class);
		this.remove(TimeUnit.class);
		this.remove(ThresholdsOperator.class);
		this.remove(FrequencyType.class);
	}

	private <T> void remove(Class<T> className) {
		EntityManager entityManager = this.getEntityManager();
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = builder.createQuery(className);
		criteriaQuery.from(className);
		TypedQuery<T> query = entityManager.createQuery(criteriaQuery);
		List<T> entities = query.getResultList();
		System.out.println("Deleting " + className.getName() + "; count: " +
				entities.size());
		entityManager.getTransaction().begin();
		int i = 1;
		for (T t : entities) {
			System.out.println("on " + i++ + "; " + t);
			entityManager.flush();
			entityManager.remove(t);
		}
		entityManager.getTransaction().commit();
	}
	
	private List<DataElementEntity> createDataElements(UserEntity... users) {
		System.out.println("Creating data elements...");
		List<DataElementEntity> dataElements =
				new ArrayList<>(users.length);
		EntityManager entityManager = this.getEntityManager();
		Date now = new Date();
		ThresholdsOperator any = new ThresholdsOperator();
		any.setDescription("ANY");
		any.setName("any");
		entityManager.getTransaction().begin();
		entityManager.persist(any);
		entityManager.getTransaction().commit();
		
		for (UserEntity u : users) {
			entityManager.getTransaction().begin();
			System.out.println("Loading user " + u.getEmail());
			CategoryEntity proposition1 = new CategoryEntity();
			proposition1.setKey("test-cat");
			proposition1.setDescription("test");
			proposition1.setDisplayName("Test Proposition");
			proposition1.setUserId(u.getId());
			proposition1.setCategoryType(CategoryType.EVENT);
			proposition1.setCreated(now);
			entityManager.persist(proposition1);
			dataElements.add(proposition1);
			
			SystemProposition sysProp = new SystemProposition();
			sysProp.setUserId(u.getId());
			sysProp.setSystemType(SystemProposition.SystemType.PRIMITIVE_PARAMETER);
			sysProp.setInSystem(true);
			sysProp.setKey("test-prim-param");
			sysProp.setCreated(now);

			ValueThresholdGroupEntity lowLevelAbstraction = 
					new ValueThresholdGroupEntity();
			lowLevelAbstraction.setDescription("test-low-level");
			lowLevelAbstraction.setKey("test-low-level");
			lowLevelAbstraction.setCreated(now);
			lowLevelAbstraction.setThresholdsOperator(any);
			lowLevelAbstraction.setUserId(u.getId());
			entityManager.persist(lowLevelAbstraction);
			dataElements.add(lowLevelAbstraction);
			entityManager.getTransaction().commit();
		}
		
		System.err.println("Done creating data elements!");
		return dataElements;
	}

	private UserEntity createResearcherUser() throws TestDataException {
		return this.createAndPersistUser("user@emory.edu", "Regular", "User",
				this.researcherRole);
	}

	private UserEntity createAdminUser() throws TestDataException {
		return this.createAndPersistUser("admin.user@emory.edu", "Admin",
				"User", this.researcherRole, this.adminRole);
	}

	private UserEntity createSuperUser() throws TestDataException {
		return this.createAndPersistUser("super.user@emory.edu", "Super",
				"User", this.researcherRole, this.adminRole,
				this.superRole);
	}

	private UserEntity createAndPersistUser(String email, String firstName,
	                                  String lastName,
	                                  Role... roles) throws
			TestDataException {
		EntityManager entityManager = this.getEntityManager();
		LocalUserEntity user = this.userEntityFactory.getLocalUserEntityInstance();
		try {
			user.setActive(true);
			user.setVerified(true);
			user.setEmail(email);
			user.setUsername(email);
			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setOrganization(ORGANIZATION);
			user.setPassword(StringUtil.md5(PASSWORD));
			user.setLastLogin(new Date());
			user.setRoles(Arrays.asList(roles));
		} catch (NoSuchAlgorithmException nsaex) {
			throw new TestDataException(nsaex);
		}
		entityManager.getTransaction().begin();
		entityManager.persist(user);
		entityManager.flush();
		entityManager.getTransaction().commit();
		
		entityManager.getTransaction().begin();
		entityManager.flush();
		entityManager.getTransaction().commit();
		return user;
	}

	private Role createResearcherRole() {
		return this.createAndPersistRole("researcher", Boolean.TRUE);
	}

	private Role createAdminRole() {
		return this.createAndPersistRole("admin", Boolean.FALSE);
	}

	private Role createSuperRole() {
		return this.createAndPersistRole("superuser", Boolean.FALSE);
	}

	private Role createAndPersistRole(String name, Boolean isDefault) {
		EntityManager entityManager = this.getEntityManager();
		Role role = new Role();
		role.setName(name);
		role.setDefaultRole(isDefault);
		entityManager.getTransaction().begin();
		entityManager.persist(role);
		entityManager.flush();
		entityManager.getTransaction().commit();
		return role;
	}

	private List<TimeUnit> createTimeUnits () {
		EntityManager entityManager = this.getEntityManager();
		TimeUnit timeUnit = new TimeUnit();
		timeUnit.setName(TESTING_TIME_UNIT_NAME);
		timeUnit.setDescription("test timeunit");
		entityManager.getTransaction().begin();
		entityManager.persist(timeUnit);
		entityManager.flush();
		entityManager.getTransaction().commit();
		return Collections.singletonList(timeUnit);
	}
	
	private List<FrequencyType> createFrequencyTypes() {
		EntityManager entityManager = getEntityManager();
		FrequencyType freqType = new FrequencyType();
		freqType.setName(TESTING_FREQ_TYPE_NAME);
		freqType.setDescription("test frequency type");
		freqType.setRank(1);
		freqType.setDefault(true);
		entityManager.getTransaction().begin();
		entityManager.persist(freqType);
		entityManager.getTransaction().commit();
		return Collections.singletonList(freqType);
	}
	
	private List<LoginTypeEntity> createLoginTypes() {
		EntityManager entityManager = getEntityManager();
		LoginTypeEntity loginType = new LoginTypeEntity();
		loginType.setName(LoginType.INTERNAL);
		loginType.setDescription(LoginType.INTERNAL.name());
		entityManager.getTransaction().begin();
		entityManager.persist(loginType);
		entityManager.getTransaction().commit();
		return Collections.singletonList(loginType);
	}
	
	private List<AuthenticationMethodEntity> createAuthenticationMethods() {
		EntityManager entityManager = getEntityManager();
		AuthenticationMethodEntity authenticationMethod = new AuthenticationMethodEntity();
		authenticationMethod.setName(AuthenticationMethod.LOCAL);
		authenticationMethod.setDescription(AuthenticationMethod.LOCAL.name());
		entityManager.getTransaction().begin();
		entityManager.persist(authenticationMethod);
		entityManager.getTransaction().commit();
		return Collections.singletonList(authenticationMethod);
	}
}
