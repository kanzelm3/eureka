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
package edu.emory.cci.aiw.cvrg.eureka.services.resource;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;

import edu.emory.cci.aiw.cvrg.eureka.common.entity.Role;
import edu.emory.cci.aiw.cvrg.eureka.services.dao.RoleDao;
import javax.annotation.security.RolesAllowed;

/**
 * A RESTful end-point for working with {@link Role} objects.
 *
 * @author hrathod
 *
 */
@Path("/protected/roles")
@RolesAllowed({"admin"})
public class RoleResource {
	/**
	 * The data access object used to work with Role objects in the data store.
	 */
	private final RoleDao roleDao;

	/**
	 * Create a RoleResource object with the given {@link RoleDao}
	 *
	 * @param inRoleDao The RoleDao object used to work with role objects in the
	 *            data store.
	 */
	@Inject
	public RoleResource(RoleDao inRoleDao) {
		this.roleDao = inRoleDao;
	}

	/**
	 * Get a role by the role's identification number.
	 *
	 * @param inId The identification number for the role to fetch.
	 * @return The role referenced by the identification number.
	 */
	@Path("/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Role getRole(@PathParam("id") Long inId) {
		return this.roleDao.retrieve(inId);
	}

	/**
	 * Get a list of all the roles available in the system.
	 *
	 * @return A list of {@link Role} objects.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Role> getRoles() {
		return this.roleDao.getAll();
	}
}
