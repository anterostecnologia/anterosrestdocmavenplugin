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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.lang.model.type.TypeMirror;

/**
 * Classe utilizada para representar o endpoint do controller
 *
 * @author Edson Martins
 *
 */

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Endpoint {

	private String path;
	private String httpMethod;
	private Collection<QueryParam> queryParams;
	private Collection<PathVar> pathVars;
	private RequestBody requestBody;
	private Collection<String> consumes;
	private Collection<String> produces;
	private String shortDescription;
	private String description;
	private String type;
	@JsonIgnore
	private TypeMirror typeMirror;

	public Endpoint() {
	}

	public Endpoint(
			String path,
			String httpMethod,
			Collection<QueryParam> queryParams,
			Collection<PathVar> pathVars,
			RequestBody requestBody,
			Collection<String> consumes,
			Collection<String> produces,
			String shortDescription,
			String description,
			TypeMirror typeMirror) {

		this.path = path;
		this.httpMethod = httpMethod;
		this.queryParams = queryParams;
		this.pathVars = pathVars;
		this.requestBody = requestBody;
		this.consumes = consumes;
		this.produces = produces;
		this.shortDescription = shortDescription;
		this.description = description;
		this.type = typeMirror.toString();
		this.typeMirror = typeMirror;
	}

	@JsonIgnore
	public TypeMirror getTypeMirror() {
		return typeMirror;
	}

	@JsonIgnore
	public void setTypeMirror(TypeMirror typeMirror) {
		this.typeMirror = typeMirror;
	}


	public String getPath() {
		return path;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public Collection<QueryParam> getQueryParams() {
		return queryParams;
	}

	public Collection<PathVar> getPathVars() {
		return pathVars;
	}

	public RequestBody getRequestBody() {
		return requestBody;
	}

	public Collection<String> getConsumes() {
		return consumes;
	}

	public Collection<String> getProduces() {
		return produces;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public String getDescription() {
		return description;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Endpoint{" +
				"path='" + path + '\'' +
				", httpMethod='" + httpMethod + '\'' +
				", queryParams=" + queryParams +
				", pathVars=" + pathVars +
				", requestBody=" + requestBody +
				", consumes=" + consumes +
				", produces=" + produces +
				", shortDescription='" + shortDescription + '\'' +
				", description='" + description + '\'' +
				'}';
	}

	@JsonIgnore
	public String getSimpleType() {
		if (type == null)
			return type;
		if (type.lastIndexOf(".") == -1)
			return type;

		return type.substring(type.lastIndexOf(".") + 1);
	}

}
