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
package edu.emory.cci.aiw.cvrg.eureka.services.config;

import java.util.HashMap;
import java.util.Map;

import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import edu.emory.cci.aiw.cvrg.eureka.common.test.UserRoleAdderFilter;

/**
 * Configure Guice for testing.
 * 
 * @author hrathod
 * 
 */
class ServletTestModule extends JerseyServletModule {

	@Override
	protected void configureServlets() {

		Map<String, String> params = new HashMap<>();
		params.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
		params.put(PackagesResourceConfig.PROPERTY_PACKAGES,
				"edu.emory.cci.aiw.cvrg.eureka.services.resource;edu.emory.cci.aiw.cvrg.eureka.common.json");
		params.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
				RolesAllowedResourceFilterFactory.class.getName());
		filter("/api/*").through(UserRoleAdderFilter.class);
		serve("/api/*").with(GuiceContainer.class, params);
	}

}
