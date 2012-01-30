package edu.emory.cci.aiw.cvrg.eureka.services.resource;

import java.util.List;

import javax.servlet.ServletException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;

import edu.emory.cci.aiw.cvrg.eureka.common.entity.Role;
import edu.emory.cci.aiw.cvrg.eureka.services.dao.RoleDao;

/**
 * A RESTful end-point for working with {@link Role} objects.
 * 
 * @author hrathod
 * 
 */
@Path("/role")
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
	 * @throws ServletException Thrown if the identification number string can
	 *             not be properly converted to a {@link Long}.
	 */
	@Path("/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Role getRole(@PathParam("id") String inId) throws ServletException {
		Role role;
		try {
			Long id = Long.valueOf(inId);
			role = this.roleDao.getRoleById(id);
		} catch (NumberFormatException nfe) {
			throw new ServletException(nfe);
		}
		return role;
	}

	/**
	 * Get a list of all the roles available in the system.
	 * 
	 * @return A list of {@link Role} objects.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/list")
	public List<Role> getRoles() {
		List<Role> roles = this.roleDao.getRoles();
		return roles;
	}
}