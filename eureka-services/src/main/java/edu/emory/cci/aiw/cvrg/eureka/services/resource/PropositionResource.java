package edu.emory.cci.aiw.cvrg.eureka.services.resource;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;

import edu.emory.cci.aiw.cvrg.eureka.common.comm.PropositionWrapper;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.Proposition;
import edu.emory.cci.aiw.cvrg.eureka.services.dao.PropositionDao;

/**
 * REST Web Service
 *
 * @author hrathod
 */
@Path("/proposition")
public class PropositionResource {

	private final PropositionDao dao;

	/**
	 * Creates a new instance of PropositionResource
	 */
	@Inject
	public PropositionResource(PropositionDao inDao) {
		this.dao = inDao;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public PropositionWrapper getProposition(@PathParam("id") String inId) {
		PropositionWrapper wrapper = null;
		try {
			Long id = Long.valueOf(inId);
			Proposition proposition = this.dao.retrieve(id);
			if (proposition != null) {
				wrapper = wrap(proposition);
			}
		} catch (NumberFormatException nfe) {
			// if the ID is not a number, we assume that it is a
			// system-level element.
			wrapper = new PropositionWrapper();
			wrapper.setId(inId);
		}

		return wrapper;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateProposition(PropositionWrapper inWrapper) {
		this.dao.update(unwrap(inWrapper));
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void insertProposition(PropositionWrapper inWrapper) {
		this.dao.create(unwrap(inWrapper));
	}

	private List<String> extractIds(List<Proposition> inPropositions) {
		List<String> ids = new ArrayList<String>();
		for (Proposition proposition : inPropositions) {
			ids.add(String.valueOf(proposition.getId().longValue()));
		}
		return ids;
	}

	private Proposition unwrap(PropositionWrapper inWrapper) {
		Proposition proposition;
		if (inWrapper.getId() != null) {
			proposition = this.dao.retrieve(Long.valueOf(inWrapper.getId()));
		} else {
			proposition = new Proposition();
		}
		proposition.setAbbrevDisplayName(inWrapper.getAbbrevDisplayName());
		proposition.setDisplayName(inWrapper.getDisplayName());
		List<Proposition> targetPropositions = new ArrayList<Proposition>();
		for (String id : inWrapper.getTargets()) {
			targetPropositions.add(this.dao.retrieve(Long.valueOf(id)));
		}
		if (inWrapper.getType() == PropositionWrapper.Type.AND) {
			proposition.setAbstractedFrom(targetPropositions);
		} else {
			proposition.setInverseIsA(targetPropositions);
		}
		return proposition;
	}

	private PropositionWrapper wrap(Proposition inProposition) {
		PropositionWrapper wrapper = new PropositionWrapper();
		wrapper.setId(String.valueOf(inProposition.getId()));
		List<Proposition> targets;
		if ((inProposition.getTemporalPattern() != null)
				|| ((inProposition.getAbstractedFrom() != null)
				&& (!inProposition.getAbstractedFrom().isEmpty()))) {
			targets = inProposition.getAbstractedFrom();
			wrapper.setType(PropositionWrapper.Type.AND);
		} else {
			targets = inProposition.getInverseIsA();
			wrapper.setType(PropositionWrapper.Type.OR);
		}
		wrapper.setUserId(inProposition.getUser().getId());
		wrapper.setTargets(extractIds(targets));
		wrapper.setAbbrevDisplayName(inProposition.getAbbrevDisplayName());
		wrapper.setDisplayName(inProposition.getDisplayName());
		return wrapper;
	}
}
