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
package edu.emory.cci.aiw.cvrg.eureka.common.comm;

import java.util.List;

import edu.emory.cci.aiw.cvrg.eureka.common.entity.SystemProposition.SystemType;
import edu.emory.cci.aiw.cvrg.eureka.common.exception.DataElementHandlingException;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public final class SystemElement extends DataElement {

	private SystemType systemType;
	private List<SystemElement> children;
	private boolean isParent;
	private List<String> properties;

	public SystemElement () {
		super(Type.SYSTEM);
	}

	public SystemType getSystemType() {
		return systemType;
	}

	public void setSystemType(SystemType systemType) {
		this.systemType = systemType;
	}

	public List<SystemElement> getChildren() {
		return children;
	}

	public void setChildren(List<SystemElement> children) {
		this.children = children;
	}

	public boolean isParent() {
		return isParent;
	}

	public void setParent(boolean isParent) {
		this.isParent = isParent;
	}

	public List<String> getProperties() {
		return properties;
	}

	public void setProperties(List<String> properties) {
		this.properties = properties;
	}

	@Override
	public boolean isInSystem() {
		return true;
	}
	
	@Override
    public void accept(DataElementVisitor visitor) 
			throws DataElementHandlingException{
		visitor.visit(this);
    }
	
	@Override
	public String toString() {
		return ReflectionToStringBuilder.reflectionToString(this);
	}
}
