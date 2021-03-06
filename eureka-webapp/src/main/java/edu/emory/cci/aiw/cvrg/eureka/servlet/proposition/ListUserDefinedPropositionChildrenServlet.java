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

import javax.servlet.ServletConfig;
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
import edu.emory.cci.aiw.cvrg.eureka.common.comm.DataElementField;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.clients.ClientException;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.clients.ServicesClient;

public class ListUserDefinedPropositionChildrenServlet extends HttpServlet {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ListUserDefinedPropositionChildrenServlet.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private final ServicesClient servicesClient;
	private final PropositionListSupport propListSupport;

	@Inject
	public ListUserDefinedPropositionChildrenServlet(
			ServicesClient inClient) {
		this.servicesClient = inClient;
		this.propListSupport = new PropositionListSupport();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	private JsonTreeData createData(String data, String key) {
		JsonTreeData d = new JsonTreeData();
		d.setData(data);
		d.setKeyVal("key", key);
		d.setKeyVal("data-key", key);

		return d;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	private void getAllData(JsonTreeData d) throws ClientException {
		DataElement dataElement = this.servicesClient.getUserElement(d.getAttr().get("data-key"), false);

		if (dataElement.getType() == DataElement.Type.CATEGORIZATION) {
			Category ce = (Category) dataElement;
			for (DataElementField de : ce.getChildren()) {
				if (de.isInSystem()) {
					JsonTreeData newData = createData(this.propListSupport.getDisplayName(de),
							de.getDataElementKey());
					newData.setType("system");
					LOGGER.debug("add sysTarget {}", de.getDataElementKey());
					d.setChildren(true);

				}
			}

			for (DataElementField userDataElement : ce.getChildren()) {
				if (!userDataElement.isInSystem()) {

					JsonTreeData newData = createData(
							userDataElement.getDataElementDescription(),
							userDataElement.getDataElementKey());
					getAllData(newData);
					newData.setType("user");
					LOGGER.debug("add user defined {}",
							userDataElement.getDataElementKey());
					d.setChildren(true);
				}
			}
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		List<JsonTreeData> l = new ArrayList<>();
		String propKey = req.getParameter("propKey");

		DataElement dataElement;
		try {
			dataElement = this.servicesClient.getUserElement(propKey, false);

			JsonTreeData newData = createData(this.propListSupport.getDisplayName(dataElement),
					propKey);
			getAllData(newData);
			l.add(newData);

			resp.setContentType("application/json");
			PrintWriter out = resp.getWriter();
			MAPPER.writeValue(out, l);
		} catch (ClientException ex) {
			throw new ServletException(
					"error getting user defined data element " + propKey, ex);
		}
	}
}
