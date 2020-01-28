/*******************************************************************************
 *  Copyright 2017 Anteros Tecnologia
 *   
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *   
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *******************************************************************************/
package br.com.anteros.restdoc.maven.plugin.doclet.model;

import java.util.Collection;

/**
 * Classe utilizada para representar um controller
 * 
 * @author Edson Martins
 * @author Eduardo Albertini
 *
 */
public class ClassDescriptor {

	private String name;
	private String contextPath;
	private Collection<Endpoint> endpoints;
	private String description;
	private String clazzName;

	public ClassDescriptor() {
	}

	public ClassDescriptor(String name, String contextPath, Collection<Endpoint> endpoints, String description) {
		this.name = name;
		this.contextPath = contextPath;
		this.endpoints = endpoints;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getContextPath() {
		return contextPath;
	}

	public Collection<Endpoint> getEndpoints() {
		return endpoints;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return "ClassDescriptor{" + "name='" + name + '\'' + ", contextPath='" + contextPath + '\'' + ", endpoints="
				+ endpoints + ", description='" + description + '\'' + '}';
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public void setEndpoints(Collection<Endpoint> endpoints) {
		this.endpoints = endpoints;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	public String getClazzName() {
		return clazzName;
	}

	public void setClazzName(String clazzName) {
		this.clazzName = clazzName;
	}
}
