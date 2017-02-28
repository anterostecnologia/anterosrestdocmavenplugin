package br.com.anteros.restdoc.maven.plugin.doclet.model;

import java.util.Collection;


/**
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
}
