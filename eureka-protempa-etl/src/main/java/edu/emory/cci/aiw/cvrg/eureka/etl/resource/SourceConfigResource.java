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
package edu.emory.cci.aiw.cvrg.eureka.etl.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


import com.google.inject.Inject;
import com.sun.jersey.api.client.ClientResponse;

import edu.emory.cci.aiw.cvrg.eureka.common.comm.SourceConfig;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.EtlUser;
import edu.emory.cci.aiw.cvrg.eureka.common.exception.HttpStatusException;
import edu.emory.cci.aiw.cvrg.eureka.etl.config.EtlProperties;
import edu.emory.cci.aiw.cvrg.eureka.etl.dao.EtlUserDao;
import edu.emory.cci.aiw.cvrg.eureka.etl.dao.SourceConfigDao;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;

@Path("/protected/sourceconfigs")
@RolesAllowed({"researcher"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SourceConfigResource {
	private final EtlProperties etlProperties;
	private final EtlUserDao userDao;
	private final SourceConfigDao sourceConfigDao;

	@Inject
	public SourceConfigResource(EtlProperties inEtlProperties, EtlUserDao inUserDao, SourceConfigDao inSourceConfigDao) {
		this.etlProperties = inEtlProperties;
		this.userDao = inUserDao;
		this.sourceConfigDao = inSourceConfigDao;
	}

	@GET
	@Path("/{sourceConfigId}")
	public SourceConfig getSource(@Context HttpServletRequest req,
			@PathParam("sourceConfigId") String sourceConfigId) {
		String username = req.getUserPrincipal().getName();
		EtlUser user = this.userDao.getByUsername(username);
		if (user == null) {
			user = new EtlUser();
			user.setUsername(username);
			this.userDao.create(user);
		}
		SourceConfigs sourceConfigs = new SourceConfigs(this.etlProperties, user, this.sourceConfigDao);
		SourceConfig sourceConfig = sourceConfigs.getOne(sourceConfigId);
		if (sourceConfig != null) {
			return sourceConfig;
		} else {
			throw new HttpStatusException(Status.NOT_FOUND);
		}
	}

	@GET
	@Path("")
	public List<SourceConfig> getAll(@Context HttpServletRequest req) {
		String username = req.getUserPrincipal().getName();
		EtlUser user = this.userDao.getByUsername(username);
		if (user == null) {
			user = new EtlUser();
			user.setUsername(username);
			this.userDao.create(user);
		}
		SourceConfigs sourceConfigs = new SourceConfigs(this.etlProperties, user, this.sourceConfigDao);
		return sourceConfigs.getAll();
	}

	/*@POST
	 public void create(Source source) {
	 }
	
	 @PUT
	 public void update(Source source) {
	 }*/
}