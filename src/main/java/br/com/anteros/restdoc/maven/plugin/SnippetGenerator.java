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
package br.com.anteros.restdoc.maven.plugin;

import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.CURL;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.ENDPOINT_DESCRIPTION;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.FALSE;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.HTTP_METHOD;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.HTTP_URL;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.NULL;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.PARAMETER_DESCRIPTION;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.PARAMETER_NAME;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.PARAMETER_OPTIONAL;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.PARAMETER_TYPE;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.PASSWORD;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.PATH_PARAMETERS_ITEM;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.QUERY_PARAMETERS_ITEM;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.RESOURCE_DESCRIPTION;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.RESOURCE_NAME;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_ENDPOINT;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_PATH_PARAMETERS;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_PATH_PARAMETERS_ITEM;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_QUERY_PARAMETERS;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_QUERY_PARAMETERS_ITEM;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_REQUEST_BODY;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_REQUEST_EXAMPLE;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_RESOURCE;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TRUE;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.UNKNOWN;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.URL_MAPPING;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.USER;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.restdoc.maven.plugin.doclet.AnterosFreeMarkerTemplateLoader;
import br.com.anteros.restdoc.maven.plugin.doclet.AnterosRestDoclet;
import br.com.anteros.restdoc.maven.plugin.doclet.model.ClassDescriptor;
import br.com.anteros.restdoc.maven.plugin.doclet.model.Endpoint;
import br.com.anteros.restdoc.maven.plugin.doclet.model.PathVar;
import br.com.anteros.restdoc.maven.plugin.doclet.model.QueryParam;
import br.com.anteros.restdoc.maven.plugin.util.Curl;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

/**
 * 
 * @author Edson Martins
 * @author Eduardo Albertini
 *
 */
public class SnippetGenerator {

	private SnippetGenerator() {
	}

	public static void generate(String baseUrl, Writer w, ClassDescriptor... classDescriptors)
			throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException,
			TemplateException {
		Configuration configuration = new Configuration();
		configuration.setTemplateLoader(new AnterosFreeMarkerTemplateLoader(AnterosRestDoclet.class, "/"));
		for (ClassDescriptor cld : classDescriptors) {

			/**
			 * Resource
			 */
			Template template = configuration.getTemplate(TEMPLATE_RESOURCE);

			Map<String, Object> dataModel = new HashMap<String, Object>();
			dataModel.put(RESOURCE_NAME, cld.getName());
			dataModel.put(RESOURCE_DESCRIPTION, cld.getDescription());
			template.process(dataModel, w);
			w.flush();

			/**
			 * EndPoint
			 */
			for (Endpoint endpoint : cld.getEndpoints()) {
				template = configuration.getTemplate(TEMPLATE_ENDPOINT);
				dataModel = new HashMap<String, Object>();
				String newDescription = endpoint.getDescription().replaceAll("(\\r|\\n)", "");
				dataModel.put(ENDPOINT_DESCRIPTION, (StringUtils.isEmpty(newDescription) ? UNKNOWN : newDescription));
				dataModel.put(HTTP_METHOD, endpoint.getHttpMethod().toUpperCase());
				dataModel.put(URL_MAPPING, endpoint.getPath());
				template.process(dataModel, w);
				w.flush();

				/**
				 * Path Variables
				 */
				if (!endpoint.getPathVars().isEmpty()) {
					Template itemTemplate = configuration.getTemplate(TEMPLATE_PATH_PARAMETERS_ITEM);
					StringBuilder pathVariable = new StringBuilder();
					for (PathVar pVar : endpoint.getPathVars()) {
						Map<String, Object> dataModelItem = new HashMap<String, Object>();
						StringWriter sw = new StringWriter();
						dataModelItem.put(PARAMETER_NAME, pVar.getName());
						dataModelItem.put(PARAMETER_TYPE, pVar.getSimpleType());
						dataModelItem.put(PARAMETER_DESCRIPTION,
								(StringUtils.isEmpty(pVar.getDescription()) ? NULL : pVar.getDescription()));
						itemTemplate.process(dataModelItem, sw);
						sw.flush();
						pathVariable.append(sw.toString()).append("\n");
						sw.close();
					}

					template = configuration.getTemplate(TEMPLATE_PATH_PARAMETERS);
					dataModel = new HashMap<String, Object>();
					dataModel.put(PATH_PARAMETERS_ITEM, pathVariable.toString());
					template.process(dataModel, w);
				}

				/**
				 * Query Parameters
				 */
				if (!endpoint.getQueryParams().isEmpty()) {
					Template itemTemplate = configuration.getTemplate(TEMPLATE_QUERY_PARAMETERS_ITEM);
					StringBuilder queryVariable = new StringBuilder();
					for (QueryParam pQueryParam : endpoint.getQueryParams()) {
						Map<String, Object> dataModelItem = new HashMap<String, Object>();
						StringWriter sw = new StringWriter();
						dataModelItem.put(PARAMETER_NAME, pQueryParam.getName());
						dataModelItem.put(PARAMETER_TYPE, pQueryParam.getSimpleType());
						dataModelItem.put(PARAMETER_OPTIONAL, (pQueryParam.isRequired() ? FALSE : TRUE));
						dataModelItem.put(PARAMETER_DESCRIPTION, (StringUtils.isEmpty(pQueryParam.getDescription())
								? NULL : pQueryParam.getDescription()));
						itemTemplate.process(dataModelItem, sw);
						sw.flush();
						queryVariable.append(sw.toString()).append("\n");
						sw.close();
					}

					template = configuration.getTemplate(TEMPLATE_QUERY_PARAMETERS);
					dataModel = new HashMap<String, Object>();
					dataModel.put(QUERY_PARAMETERS_ITEM, queryVariable.toString());
					template.process(dataModel, w);
				}

				/**
				 * Request body
				 */
				if (endpoint.getRequestBody() != null) {
					template = configuration.getTemplate(TEMPLATE_REQUEST_BODY);
					dataModel = new HashMap<String, Object>();
					dataModel.put(PARAMETER_NAME, endpoint.getRequestBody().getName());
					dataModel.put(PARAMETER_TYPE, endpoint.getRequestBody().getSimpleType());
					dataModel.put(PARAMETER_DESCRIPTION, endpoint.getRequestBody().getDescription());

					template.process(dataModel, w);
				}

				/**
				 * Request example
				 */
				Curl curl = Curl.of(baseUrl).endPoint(endpoint.getPath()).basicAuthentication(USER, PASSWORD)
						.enableResponseLogging().timeOut(300)
						.type(Curl.Type.valueOf(endpoint.getHttpMethod().toUpperCase()));
				if (!endpoint.getQueryParams().isEmpty()) {
					for (QueryParam qp : endpoint.getQueryParams()) {
						curl.parameter(qp.getName(), "{" + qp.getName() + "}");
					}
				}

				if (endpoint.getRequestBody() != null)
					curl.body("{" + endpoint.getRequestBody().getName() + "}");

				template = configuration.getTemplate(TEMPLATE_REQUEST_EXAMPLE);
				dataModel = new HashMap<String, Object>();
				dataModel.put(CURL, curl.toCurl());
				dataModel.put(HTTP_URL, curl.toUrl());
				template.process(dataModel, w);
			}
		}
	}
}
