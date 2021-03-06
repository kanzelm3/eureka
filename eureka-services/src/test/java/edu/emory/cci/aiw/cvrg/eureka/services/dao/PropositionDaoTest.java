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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.emory.cci.aiw.cvrg.eureka.services.dao;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import edu.emory.cci.aiw.cvrg.eureka.common.entity.DataElementEntity;
import edu.emory.cci.aiw.cvrg.eureka.services.test.AbstractServiceDataTest;

/**
 *
 * @author hrathod
 */
public class PropositionDaoTest extends AbstractServiceDataTest {

	@Test
	public void testDao() {
		DataElementEntityDao dao = this.getInstance(DataElementEntityDao.class);
		List<DataElementEntity> propositions = dao.getAll();
		Assert.assertEquals(6, propositions.size());
	}
}
