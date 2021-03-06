package edu.emory.cci.aiw.cvrg.eureka.etl.dao;

/*
 * #%L
 * Eureka Protempa ETL
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

import com.google.inject.Inject;
import com.google.inject.Provider;
import edu.emory.cci.aiw.cvrg.eureka.common.dao.GenericDao;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.SourceConfigEntity;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.SourceConfigEntity_;
import javax.persistence.EntityManager;

/**
 *
 * @author Andrew Post
 */
public class JpaSourceConfigDao extends GenericDao<SourceConfigEntity, Long> implements SourceConfigDao {

	@Inject
	public JpaSourceConfigDao(Provider<EntityManager> inManagerProvider) {
		super(SourceConfigEntity.class, inManagerProvider);
	}

	@Override
	public SourceConfigEntity getByName(String name) {
		return getUniqueByAttribute(SourceConfigEntity_.name, name);
	}
	
	
	
}
