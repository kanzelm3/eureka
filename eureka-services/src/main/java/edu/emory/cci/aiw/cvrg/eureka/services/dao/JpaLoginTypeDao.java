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
package edu.emory.cci.aiw.cvrg.eureka.services.dao;

import com.google.inject.Inject;
import com.google.inject.Provider;
import edu.emory.cci.aiw.cvrg.eureka.common.authentication.LoginType;
import edu.emory.cci.aiw.cvrg.eureka.common.dao.GenericDao;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.LoginTypeEntity;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.LoginTypeEntity_;

import javax.persistence.EntityManager;

/**
 *
 */
public class JpaLoginTypeDao extends
		GenericDao<LoginTypeEntity, Long> implements LoginTypeDao {

	@Inject
	protected JpaLoginTypeDao(Provider<EntityManager>
															 inManagerProvider) {
		super(LoginTypeEntity.class, inManagerProvider);
	}

	@Override
	public LoginTypeEntity getByName(LoginType inName) {
		return this.getUniqueByAttribute(LoginTypeEntity_.name, inName);
	}
}
