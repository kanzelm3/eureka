package edu.emory.cci.aiw.cvrg.eureka.common.entity;

/*
 * #%L
 * Eureka Common
 * %%
 * Copyright (C) 2012 - 2014 Emory University
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

import edu.emory.cci.aiw.cvrg.eureka.common.comm.LdapUser;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.LocalUser;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.OAuthUser;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Andrew Post
 */
public class UserEntityToUserVisitor implements UserEntityVisitor {

	private final List<User> results;

	public UserEntityToUserVisitor() {
		results = new ArrayList<>();
	}
	
	public void visit(Collection<? extends UserEntity> userEntities) {
		for (UserEntity user : userEntities) {
			user.accept(this);
		}
	}

	@Override
	public void visit(LocalUserEntity localUserEntity) {
		LocalUser localUser = new LocalUser();
		toUser(localUser, localUserEntity);
		localUser.setPassword(localUserEntity.getPassword());
		localUser.setPasswordExpiration(localUserEntity.getPasswordExpiration());
		localUser.setVerificationCode(localUserEntity.getVerificationCode());
		localUser.setVerified(localUserEntity.isVerified());
		this.results.add(localUser);
	}

	@Override
	public void visit(LdapUserEntity ldapUserEntity) {
		LdapUser ldapUser = new LdapUser();
		toUser(ldapUser, ldapUserEntity);
		this.results.add(ldapUser);
	}

	@Override
	public void visit(OAuthUserEntity oauthUserEntity) {
		OAuthUser oauthUser = new OAuthUser();
		toUser(oauthUser, oauthUserEntity);
		oauthUser.setProviderUsername(oauthUserEntity.getProviderUsername());
		oauthUser.setOAuthProvider(oauthUserEntity.getOAuthProvider().getId());
		this.results.add(oauthUser);
	}
	
	public List<User> getUsers() {
		return new ArrayList<>(this.results);
	}
	
	public User getUser() {
		return this.results.isEmpty() ? null : this.results.get(0);
	}

	private void toUser(User user, UserEntity inUserEntity) {
		if (inUserEntity != null) {
			user.setId(inUserEntity.getId());
			user.setActive(inUserEntity.isActive());
			user.setUsername(inUserEntity.getUsername());
			user.setEmail(inUserEntity.getEmail());
			user.setFirstName(inUserEntity.getFirstName());
			user.setLastName(inUserEntity.getLastName());
			user.setFullName(inUserEntity.getFullName());
			user.setLastLogin(inUserEntity.getLastLogin());
			user.setOrganization(inUserEntity.getOrganization());
			user.setRoles(rolesToRoleIds(inUserEntity.getRoles()));
			user.setDepartment(inUserEntity.getDepartment());
			user.setTitle(inUserEntity.getTitle());
		}
	}

	private List<Long> rolesToRoleIds(List<Role> inRoles) {
		List<Long> roleIds = new ArrayList<>(inRoles.size());
		for (Role role : inRoles) {
			roleIds.add(role.getId());
		}
		return roleIds;
	}
	
}
