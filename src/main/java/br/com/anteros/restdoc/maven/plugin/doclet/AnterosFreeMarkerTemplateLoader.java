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
package br.com.anteros.restdoc.maven.plugin.doclet;

import java.net.URL;

import br.com.anteros.core.utils.ResourceUtils;
import freemarker.cache.ClassTemplateLoader;

/**
 * 
 * @author Edson Martins
 * @author Eduardo Albertini
 *
 */
public class AnterosFreeMarkerTemplateLoader extends ClassTemplateLoader {

	private Class<?> cl;

	public AnterosFreeMarkerTemplateLoader(Class<?> clazz, String string) {
		super(clazz, string);
		this.cl = clazz;
	}

	@Override
	protected URL getURL(String name) {
		return ResourceUtils.getResource(name, cl);
	}

}
