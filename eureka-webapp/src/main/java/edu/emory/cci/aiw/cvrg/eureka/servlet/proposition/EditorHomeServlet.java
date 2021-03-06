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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import edu.emory.cci.aiw.cvrg.eureka.common.comm.DataElement;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.clients.ClientException;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.clients.ServicesClient;

public class EditorHomeServlet extends HttpServlet {

	private static final Logger LOGGER = LoggerFactory.getLogger
		(EditorHomeServlet.class);

	private final ServicesClient servicesClient;

	@Inject
	public EditorHomeServlet (ServicesClient inClient) {
		this.servicesClient = inClient;
	}

	private JsonTreeData createData(String key, String data) {
		JsonTreeData d = new JsonTreeData();
		d.setData(data);
		d.setKeyVal("key", key);

		return d;
	}

	private String getDisplayName(DataElement e) {
		String displayName = "";

		if (e.getDisplayName() != null && !e.getDisplayName().equals
			("")) {

			displayName = e.getDisplayName() + "(" + e.getKey() + ")";

		} else {

			displayName = e.getKey();

		}

		return displayName;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {

		List<JsonTreeData> l = new ArrayList<>();
		List<DataElement> props;
		try {
			props = this.servicesClient.getUserElements(true);
		} catch (ClientException ex) {
			throw new ServletException("Error getting user-defined data elements", ex);
		}
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		for (DataElement proposition : props) {
			JsonTreeData d = createData(
				proposition.getKey(),
				this.getDisplayName(proposition));
			d.setKeyVal("description",
				proposition.getDescription());
			d.setKeyVal("displayName", proposition.getDisplayName());

			if (proposition.getType() == DataElement.Type
				.CATEGORIZATION) {
				d.setKeyVal("type", "Categorical");
			} else if (proposition.getType() == DataElement.Type
				.SEQUENCE) {
				d.setKeyVal("type", "Sequence");
			} else if (proposition.getType() == DataElement.Type
				.FREQUENCY) {
				d.setKeyVal("type", "Frequency");
			} else if (proposition.getType() == DataElement.Type
				.VALUE_THRESHOLD) {
				d.setKeyVal("type", "Value Threshold");
			}

			if (proposition.getCreated() != null) {
				LOGGER.debug(
					"created date: " + df.format(proposition.getCreated
						()));
				d.setKeyVal("created", df.format(proposition.getCreated
					()));
			}
			if (proposition.getLastModified() != null) {
				d.setKeyVal(
					"lastModified", df.format(proposition
					.getLastModified()));
			}
			l.add(d);
			LOGGER.debug("Added user prop: " + d.getData());
		}

		req.setAttribute("props", l);
		req.getRequestDispatcher("/protected/editor_home.jsp").forward(
			req, resp);
	}
}
