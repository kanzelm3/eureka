/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.emory.cci.aiw.cvrg.eureka.common.comm.clients;

/*
 * #%L
 * Eureka Common
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
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Andrew Post
 */
public abstract class AbstractWebResourceWrapper implements WebResourceWrapper {

	private WebResource webResource;

	public AbstractWebResourceWrapper(WebResource webResource) {
		this.webResource = webResource;
	}

	protected WebResource getWebResource() {
		return webResource;
	}
	
	@Override
	public WebResource rewritten(String path, String method) throws ClientException {
		return rewritten(path, method, null);
	}

	@Override
	public WebResource rewritten(String path, String method, MultivaluedMap<String, String> queryParams) throws ClientException {
		return webResource.path(path);
	}

}
