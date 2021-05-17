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

/**
 * Classe utilizada para representar os objetos vindos pelo corpo da requisição
 * 
 * @author Edson Martins
 *
 */
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.lang.model.type.TypeMirror;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class RequestBody {

	private String name;
	private String description;
	private String type;
	@JsonIgnore
	private TypeMirror typeMirror;

	public RequestBody() {
	}

	public RequestBody(String name, String description, TypeMirror typeMirror) {
		this.name = name;
		this.description = description;
		this.typeMirror = typeMirror;
		this.type = typeMirror.toString();
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getType() {
		return type;
	}


	@JsonIgnore
	public TypeMirror getTypeMirror() {
		return typeMirror;
	}

	@JsonIgnore
	public void setTypeMirror(TypeMirror typeMirror) {
		this.typeMirror = typeMirror;
	}

	@JsonIgnore
	public String getSimpleType() {
		if (type == null)
			return type;
		if (type.lastIndexOf(".") == -1)
			return type;

		return type.substring(type.lastIndexOf(".") + 1);
	}


	@Override
	public String toString() {
		return "RequestBody{" +
				"name='" + name + '\'' +
				", description='" + description + '\'' +
				'}';
	}
}
