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
package edu.emory.cci.aiw.cvrg.eureka.servlet.proposition;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import edu.emory.cci.aiw.cvrg.eureka.common.comm.Category;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.DataElement;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.clients.ClientException;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.clients.ServicesClient;

public class UserPropositionListServlet extends HttpServlet {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(UserPropositionListServlet.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private final ServicesClient servicesClient;
	private final PropositionListSupport propListSupport;

	@Inject
	public UserPropositionListServlet(ServicesClient inClient) {
		this.servicesClient = inClient;
		this.propListSupport = new PropositionListSupport();
	}

	private JsonTreeData createData(DataElement element) {
		JsonTreeData d = new JsonTreeData();
		d.setData(this.propListSupport.getDisplayName(element));
		d.setText(this.propListSupport.getDisplayName(element));
		d.setKeyVal("id", String.valueOf(element.getId()));
		d.setKeyVal("data-key", element.getKey());
		d.setKeyVal("data-space", "user");
		d.setKeyVal("data-type", element.getType().toString());
		if (element.getType() == DataElement.Type.CATEGORIZATION) {
			d.setKeyVal("data-subtype", ((Category) element)
					.getCategoricalType().toString());
		}

		return d;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		LOGGER.debug("doGet");
		List<DataElement> props;
		try {
			props = this.servicesClient.getUserElements(false);
		} catch (ClientException ex) {
			throw new ServletException("Error getting user-defined data element list", ex);
		}
		List<JsonTreeData> l = new ArrayList<>(props.size());
		for (DataElement proposition : props) {
			JsonTreeData d = createData(proposition);
			l.add(d);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Added user prop: {}", d.getData());
			}
		}
		LOGGER.debug("executed resource get");

		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		MAPPER.writeValue(out, l);
	}
}
