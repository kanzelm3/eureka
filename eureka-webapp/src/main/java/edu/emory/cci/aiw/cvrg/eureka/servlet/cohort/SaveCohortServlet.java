/*
 * #%L
 * Eureka WebApp
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
package edu.emory.cci.aiw.cvrg.eureka.servlet.cohort;

import com.google.inject.Inject;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.CohortDestination;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.DataElement;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.User;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.clients.ClientException;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.clients.ServicesClient;
import edu.emory.cci.aiw.cvrg.eureka.webapp.authentication.WebappAuthenticationSupport;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveCohortServlet extends HttpServlet {

	private static final Logger LOGGER = LoggerFactory
	        .getLogger(SaveCohortServlet.class);
	private final ServicesClient servicesClient;
	private final WebappAuthenticationSupport authenticationSupport;

	@Inject
	public SaveCohortServlet (ServicesClient inClient) {
		this.servicesClient = inClient;
		this.authenticationSupport = new WebappAuthenticationSupport(this.servicesClient);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	        throws ServletException, IOException {
		LOGGER.debug("SaveCohortServlet");
		ObjectMapper objectMapper = new ObjectMapper();
		CohortDestination cohortDestination = objectMapper.readValue(req.getReader(),
				CohortDestination.class);
		try {
			User user = this.authenticationSupport.getMe(req);
			cohortDestination.setOwnerUserId(user.getId());
			if (cohortDestination.getId() == null) {
				this.servicesClient.createDestination(cohortDestination);
			} else {
				this.servicesClient.updateDestination(cohortDestination);
			}
		} catch (ClientException e) {
			resp.setStatus(e.getResponseStatus().getStatusCode());
			resp.getWriter().write(e.getMessage());
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	        throws ServletException, IOException {

		doPost(req, resp);
	}
}