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
import edu.emory.cci.aiw.cvrg.eureka.common.comm.BinaryOperator;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.Cohort;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.CohortDestination;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.DataElementField;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.Literal;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.Node;
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
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Logger LOGGER = LoggerFactory
			.getLogger(SaveCohortServlet.class);
	private final ServicesClient servicesClient;
	private final WebappAuthenticationSupport authenticationSupport;

	@Inject
	public SaveCohortServlet(ServicesClient inClient) {
		this.servicesClient = inClient;
		this.authenticationSupport = new WebappAuthenticationSupport(this.servicesClient);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		LOGGER.debug("SaveCohortServlet");
		CohortJson cohortJson = MAPPER.readValue(req.getReader(), CohortJson.class);
		CohortDestination cohortDestination = new CohortDestination();
		try {
			User user = this.authenticationSupport.getMe(req);
			cohortDestination.setId(cohortJson.getId());
			cohortDestination.setOwnerUserId(user.getId());
			cohortDestination.setName(cohortJson.getName());
			cohortDestination.setDescription(cohortJson.getDescription());
			cohortDestination.setCohort(cohortJson.toCohort());
			if (cohortDestination.getId() == null) {
				this.servicesClient.createDestination(cohortDestination);
			} else {
				this.servicesClient.updateDestination(cohortDestination);
			}
		} catch (ClientException e) {
			switch (e.getResponseStatus()) {
				case UNAUTHORIZED:
					this.authenticationSupport.needsToLogin(req, resp);
					break;
				default:
					resp.setStatus(e.getResponseStatus().getStatusCode());
					resp.getWriter().write(e.getMessage());
			}
		}
	}
	
	private static class CohortJson {
		private Long id;
		private String name;
		private String description;
		private DataElementField[] phenotypes;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public DataElementField[] getPhenotypes() {
			return phenotypes;
		}

		public void setPhenotypes(DataElementField[] phenotypes) {
			this.phenotypes = phenotypes;
		}
		
		private Cohort toCohort() {
			Cohort cohort = new Cohort();
			Node node;
			if (phenotypes.length == 1) {
				Literal literal = new Literal();
				literal.setName(phenotypes[0].getDataElementKey());
				node = literal;
			} else if (phenotypes.length > 1) {
				boolean first = true;
				Node prev = null;
				for (int i = phenotypes.length - 1; i >= 0; i--) {
					Literal literal = new Literal();
					literal.setName(phenotypes[i].getDataElementKey());
					if (first) {
						first = false;
						prev = literal;
					} else {
						BinaryOperator binaryOperator = new BinaryOperator();
						binaryOperator.setOp(BinaryOperator.Op.OR);
						binaryOperator.setLeftNode(literal);
						binaryOperator.setRightNode(prev);
						prev = binaryOperator;
					}
				}
				node = prev;
			} else {
				node = null;
			}
			cohort.setNode(node);
			return cohort;
		}
	}

	

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		doPost(req, resp);
	}
}
