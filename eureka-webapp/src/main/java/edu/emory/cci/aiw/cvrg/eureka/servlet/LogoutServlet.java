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
package edu.emory.cci.aiw.cvrg.eureka.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.inject.Inject;

import edu.emory.cci.aiw.cvrg.eureka.webapp.config.WebappProperties;

public class LogoutServlet extends HttpServlet {

	private final WebappProperties webappProperties;

	@Inject
	public LogoutServlet (WebappProperties inProperties) {
		this.webappProperties = inProperties;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		/*
		 * We need to redirect here rather than forward so that 
		 * logout.jsp gets a request object without a user. Otherwise,
		 * the button bar will think we're still logged in.
		 */
		resp.sendRedirect(webappProperties.getCasLogoutUrl());
	}
}
