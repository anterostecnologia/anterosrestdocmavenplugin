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
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.ENTITY_DESCRIPTION;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.ENTITY_ID;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.ENTITY_NAME;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.FALSE;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.FIELD_DESCRIPTION;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.FIELD_NAME;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.FIELD_OPTIONAL;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.FIELD_TYPE;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.FIELD_VALUES;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.GENERAL;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.HTTP_METHOD;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.HTTP_METHOD_COLOR;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.HTTP_URL;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.INTEGRATION;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.JSON_SCHEMA;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.MOBILE;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.MOBILE_DESCRIPTION;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.MOBILE_NAME;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.MODEL_ITEM;
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
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.SCHEMA;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.SECURITY;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_ENDPOINT;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_INTEGRATION_MOBILE;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_INTEGRATION_PERSISTENCE;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_INTEGRATION_PERSISTENCE_ITEM;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_MOBILE_PERSISTENCE;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_MOBILE_PERSISTENCE_ITEM;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_PATH_PARAMETERS;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_PATH_PARAMETERS_ITEM;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_PERSISTENCE_ITEM;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_PERSISTENCE_SECURITY;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_QUERY_PARAMETERS;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_QUERY_PARAMETERS_ITEM;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_REQUEST_BODY;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_REQUEST_EXAMPLE;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_RESOURCE;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_RESOURCE_MOBILE;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_RESOURCE_SECURITY;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TRUE;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.UNKNOWN;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.URL_MAPPING;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.USER;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thoughtworks.qdox.model.JavaClass;

import br.com.anteros.bean.validation.constraints.Required;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.metadata.annotation.Code;
import br.com.anteros.persistence.metadata.annotation.Column;
import br.com.anteros.persistence.metadata.annotation.CompanyId;
import br.com.anteros.persistence.metadata.annotation.CompositeId;
import br.com.anteros.persistence.metadata.annotation.DiscriminatorColumn;
import br.com.anteros.persistence.metadata.annotation.DiscriminatorValue;
import br.com.anteros.persistence.metadata.annotation.Fetch;
import br.com.anteros.persistence.metadata.annotation.ForeignKey;
import br.com.anteros.persistence.metadata.annotation.Id;
import br.com.anteros.persistence.metadata.annotation.Label;
import br.com.anteros.persistence.metadata.annotation.MapKeyColumn;
import br.com.anteros.persistence.metadata.annotation.Temporal;
import br.com.anteros.persistence.metadata.annotation.TenantId;
import br.com.anteros.persistence.metadata.annotation.Transient;
import br.com.anteros.persistence.metadata.annotation.Version;
import br.com.anteros.persistence.metadata.annotation.type.DiscriminatorType;
import br.com.anteros.persistence.metadata.annotation.type.FetchMode;
import br.com.anteros.persistence.metadata.annotation.type.TemporalType;
import br.com.anteros.remote.synch.annotation.DataSynchDirection;
import br.com.anteros.remote.synch.annotation.RemoteSynchDataIntegration;
import br.com.anteros.remote.synch.annotation.RemoteSynchIntegrationIgnore;
import br.com.anteros.remote.synch.annotation.RemoteSynchMobile;
import br.com.anteros.remote.synch.annotation.RemoteSynchMobileIgnore;
import br.com.anteros.restdoc.maven.plugin.doclet.AnterosFreeMarkerTemplateLoader;
import br.com.anteros.restdoc.maven.plugin.doclet.AnterosRestDoclet;
import br.com.anteros.restdoc.maven.plugin.doclet.model.ClassDescriptor;
import br.com.anteros.restdoc.maven.plugin.doclet.model.Endpoint;
import br.com.anteros.restdoc.maven.plugin.doclet.model.PathVar;
import br.com.anteros.restdoc.maven.plugin.doclet.model.QueryParam;
import br.com.anteros.restdoc.maven.plugin.util.Curl;
import edu.emory.mathcs.backport.java.util.Arrays;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

/**
 * 
 * @author Edson Martins
 *
 */
public class SnippetGenerator {

	private static final String ENTIDADE = "Entidade";
	private static final String BOOLEANO = "Booleano";
	private static final String VALUES = "values";
	private static final String ENUMERACAO = "Enumeração";
	private static final String ENTIDADES = " Entidades";
	private static final String COLECAO = "Coleção";
	private static final String TEXTO_BASE_64 = "Texto(Base 64)";
	private static final String TYPE = "type";
	private static final String TEXTO = "Texto";
	private static final String NUMERICO = "Numérico";
	private static final String NAO = "Não";
	private static final String SIM = "Sim";
	private static ObjectMapper objectMapper;

	private SnippetGenerator() {
	}

	public static void generateResources(String baseUrl, Writer w, String type, Map<String, String> anchors,
			ClassDescriptor... classDescriptors) throws TemplateNotFoundException, MalformedTemplateNameException,
			ParseException, IOException, TemplateException, InstantiationException, IllegalAccessException,
			NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		Configuration configuration = new Configuration();
		configuration.setEncoding(Locale.getDefault(), "UTF-8");
		configuration.setTemplateLoader(new AnterosFreeMarkerTemplateLoader(AnterosRestDoclet.class, "/"));
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

		for (ClassDescriptor cld : classDescriptors) {
			Class parameterizedClass = null;
			try {
				Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(cld.getClazzName());
				parameterizedClass = ReflectionUtils.getParameterizedClass(clazz);
				if (parameterizedClass != null) {
					if (type.equals(SECURITY)) {
						if (!parameterizedClass.getCanonicalName().contains("br.com.anteros.security")) {
							continue;
						}
					} else if (type.equals(GENERAL)) {
						if (parameterizedClass.getCanonicalName().contains("br.com.anteros.security")
								|| (parameterizedClass.getCanonicalName().contains("br.com.anteros.remote.synch"))) {
							continue;
						}
					} else if (type.equals(MOBILE)) {
						if (!parameterizedClass.getCanonicalName()
								.contains("br.com.anteros.remote.synch.resource.RemoteSynchMobileResource")) {
							continue;
						}

					} else if (type.equals(INTEGRATION)) {
						if (!parameterizedClass.getCanonicalName()
								.contains("br.com.anteros.remote.synch.resource.RemoteSynchDataIntegrationResource")) {
							continue;
						}

					}
				} else {
					if (type.equals(SECURITY)) {
						if (!clazz.getCanonicalName().contains("br.com.anteros.security")) {
							continue;
						}
					} else if (type.equals(GENERAL)) {
						if (clazz.getCanonicalName().contains("br.com.anteros.security")
								|| (clazz.getCanonicalName().contains("br.com.anteros.remote.synch"))) {
							continue;
						}
					} else if (type.equals(MOBILE)) {
						if (!clazz.getCanonicalName()
								.contains("br.com.anteros.remote.synch.resource.RemoteSynchMobileResource")) {
							continue;
						}

					} else if (type.equals(INTEGRATION)) {
						if (!clazz.getCanonicalName()
								.contains("br.com.anteros.remote.synch.resource.RemoteSynchDataIntegrationResource")) {
							continue;
						}

					}
				}
			} catch (Exception e) {
			}

			/**
			 * Resource
			 */
			Template template = null;
			
			if (type.equals(SECURITY)) {
				template = configuration.getTemplate(TEMPLATE_RESOURCE_SECURITY);
			} else if (type.equals(GENERAL)) {
				template = configuration.getTemplate(TEMPLATE_RESOURCE);
			} else if (type.equals(MOBILE)) {
				template = configuration.getTemplate(TEMPLATE_RESOURCE_MOBILE);
			} else if (type.equals(INTEGRATION)) {
				template = configuration.getTemplate(TEMPLATE_INTEGRATION_MOBILE);
			}

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

				String httpMethodColor = "";
				if (endpoint.getHttpMethod().toUpperCase().equals("GET")) {
					httpMethodColor = "{http-get}";
				} else if (endpoint.getHttpMethod().toUpperCase().equals("PUT")) {
					httpMethodColor = "{http-put}";
				} else if (endpoint.getHttpMethod().toUpperCase().equals("POST")) {
					httpMethodColor = "{http-post}";
				} else if (endpoint.getHttpMethod().toUpperCase().equals("DELETE")) {
					httpMethodColor = "{http-delete}";
				} else if (endpoint.getHttpMethod().toUpperCase().equals("HEAD")) {
					httpMethodColor = "{http-head}";
				} else if (endpoint.getHttpMethod().toUpperCase().equals("OPTIONS")) {
					httpMethodColor = "{http-options}";
				} else if (endpoint.getHttpMethod().toUpperCase().equals("PATCH")) {
					httpMethodColor = "{http-patch}";
				}

				dataModel.put(ENDPOINT_DESCRIPTION, (StringUtils.isEmpty(newDescription) ? UNKNOWN : newDescription));
				dataModel.put(HTTP_METHOD_COLOR, httpMethodColor);
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
						dataModelItem.put(PARAMETER_DESCRIPTION,
								(StringUtils.isEmpty(pQueryParam.getDescription()) ? NULL
										: pQueryParam.getDescription()));
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
				dataModel.put(JSON_SCHEMA, "");
				dataModel.put(CURL, curl.toCurl());
				dataModel.put(HTTP_URL, curl.toUrl().replace("\"", ""));
				template.process(dataModel, w);
			}
		}
	}

	public static void generatePersistence(String baseUrl, Writer w, boolean onlySecurity, Map<String, String> anchors,
			List<JavaClass> persistenceClasses, List<JavaClass> allClasses)
			throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException,
			TemplateException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException,
			IllegalArgumentException, InvocationTargetException {
		Configuration configuration = new Configuration();
		configuration.setEncoding(Locale.getDefault(), "UTF-8");
		configuration.setTemplateLoader(new AnterosFreeMarkerTemplateLoader(AnterosRestDoclet.class, "/"));
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

		for (JavaClass cld : persistenceClasses) {
			Class<?> clazz = null;
			try {
				clazz = Thread.currentThread().getContextClassLoader().loadClass(cld.getCanonicalName());
				if (onlySecurity) {
					if (!clazz.getCanonicalName().contains("br.com.anteros.security")) {
						continue;
					}
				} else {
					if (clazz.getCanonicalName().contains("br.com.anteros.security")) {
						continue;
					}
				}
			} catch (Exception e) {
			}

			/**
			 * Persistence
			 */
			Template template = configuration.getTemplate(TEMPLATE_PERSISTENCE_SECURITY);

			String description = " ";
			if (StringUtils.isEmpty(cld.getComment())) {
				description = clazz.getCanonicalName();
			} else {
				description = cld.getComment();
			}

			Map<String, Object> dataModel = new HashMap<String, Object>();
			dataModel.put(ENTITY_NAME, clazz.getSimpleName());
			dataModel.put(ENTITY_DESCRIPTION, description);
			dataModel.put(ENTITY_ID, anchors.get(clazz.getCanonicalName()));

			Template itemResourceTemplate = configuration.getTemplate(TEMPLATE_PERSISTENCE_ITEM);
			StringBuilder resourceItems = new StringBuilder();

			if (clazz != null) {
				if (clazz.isAnnotationPresent(DiscriminatorColumn.class)) {
					String fieldName = TYPE;
					String fieldType = TEXTO;
					DiscriminatorColumn dc = (DiscriminatorColumn) clazz.getAnnotation(DiscriminatorColumn.class);
					if (dc.discriminatorType().equals(DiscriminatorType.STRING)
							|| dc.discriminatorType().equals(DiscriminatorType.CHAR)) {
						if (dc.length() > 0) {
							fieldType += "(" + dc.length() + ")";
						}
					} else {
						fieldType = NUMERICO;
						fieldType += "(" + dc.length() + ")";
					}

					String fieldValues = "";
					boolean appendDelimiter = false;
					for (JavaClass cld2 : allClasses) {
						try {
							Class<?> clazz2 = Thread.currentThread().getContextClassLoader()
									.loadClass(cld2.getCanonicalName());
							if (ReflectionUtils.isExtendsClass(clazz, clazz2)) {
								if (clazz2.isAnnotationPresent(DiscriminatorValue.class)) {
									if (appendDelimiter) {
										fieldValues += ", ";
									}
									String key = anchors.get(clazz2.getCanonicalName());

									DiscriminatorValue dv = clazz2.getAnnotation(DiscriminatorValue.class);
									fieldValues += "<<" + key + ", " + dv.value() + ">>";
									appendDelimiter = true;
								}
							}
						} catch (Exception e) {
						}
					}
					String fieldDescription = "Tipo de identificação para classes concretas.";
					Map<String, Object> dataModelItem = new HashMap<String, Object>();
					StringWriter sw = new StringWriter();

					dataModelItem.put(FIELD_NAME, "*" + fieldName + "* -> " + "{anteros-type}");
					dataModelItem.put(FIELD_TYPE, fieldType);
					dataModelItem.put(FIELD_VALUES, fieldValues);
					dataModelItem.put(FIELD_OPTIONAL, NAO);
					dataModelItem.put(FIELD_DESCRIPTION, fieldDescription);
					itemResourceTemplate.process(dataModelItem, sw);
					sw.flush();
					resourceItems.append(sw.toString()).append("\n");
					sw.close();
				}

				Field[] fields = ReflectionUtils.getAllDeclaredFields(clazz);
				for (Field field : fields) {
					if (field.isAnnotationPresent(Transient.class) || field.isAnnotationPresent(JsonIgnore.class)) {
						continue;
					}

					Map<String, Object> dataModelItem = new HashMap<String, Object>();
					StringWriter sw = new StringWriter();

					String fieldName = field.getName();
					String fieldType = "";
					String fieldDescription = "";
					String fieldOptional = SIM;
					String fieldValues = "";

					if (field.isAnnotationPresent(Column.class)) {
						Column ann = field.getAnnotation(Column.class);
						fieldDescription = ann.label();
						if (ann.required()) {
							fieldOptional = NAO;
						}
						Required requiredAnn = field.getAnnotation(Required.class);
						if (requiredAnn != null) {
							fieldOptional = NAO;
						}
					}

					if (field.isAnnotationPresent(Label.class)) {
						Label ann = field.getAnnotation(Label.class);
						fieldDescription = ann.value();
					}

					if (ReflectionUtils.isExtendsClass(BigInteger.class, field.getType())) {
						fieldType = NUMERICO;
						if (field.isAnnotationPresent(Column.class)) {
							Column ann = field.getAnnotation(Column.class);
							if (ann.precision() > 0 == ann.scale() > 0) {
								fieldType = fieldType + "(" + ann.precision() + "," + ann.scale() + ")";
							} else if (ann.precision() > 0) {
								fieldType = fieldType + "(" + ann.precision() + ")";
							}
						}
					} else if (ReflectionUtils.isExtendsClass(BigDecimal.class, field.getType())) {
						fieldType = NUMERICO;
						if (field.isAnnotationPresent(Column.class)) {
							Column ann = field.getAnnotation(Column.class);
							if (ann.precision() > 0 && ann.scale() > 0) {
								fieldType = fieldType + "(" + ann.precision() + "," + ann.scale() + ")";
							} else if (ann.precision() > 0) {
								fieldType = fieldType + "(" + ann.precision() + ")";
							}
						}
					} else if (ReflectionUtils.isExtendsClass(Number.class, field.getType())) {
						fieldType = NUMERICO;
						if (field.isAnnotationPresent(Column.class)) {
							Column ann = field.getAnnotation(Column.class);
							if (ann.precision() > 0 && ann.scale() > 0) {
								fieldType = fieldType + "(" + ann.precision() + "," + ann.scale() + ")";
							} else if (ann.precision() > 0) {
								fieldType = fieldType + "(" + ann.precision() + ")";
							}
						}
					} else if (ReflectionUtils.isExtendsClass(String.class, field.getType())) {
						fieldType = TEXTO;
						if (field.isAnnotationPresent(Column.class)) {
							Column ann = field.getAnnotation(Column.class);
							if (ann.length() > 0) {
								fieldType = fieldType + "(" + ann.length() + ")";
							}
						}
					} else if (ReflectionUtils.isExtendsClass(Date.class, field.getType())
							|| (ReflectionUtils.isExtendsClass(java.sql.Date.class, field.getType()))) {
						if (field.isAnnotationPresent(Temporal.class)) {
							Temporal temp = field.getAnnotation(Temporal.class);
							if (temp.value() == TemporalType.DATE) {
								fieldType = "Data : yyyy-MM-dd";
							}
							if (temp.value() == TemporalType.DATE_TIME) {
								fieldType = "Data/hora : yyyy-MM-dd'T'HH:mm:ss.SSS";
							}
							if (temp.value() == TemporalType.TIME) {
								fieldType = "Hora : HH:mm:ss.SSS";
							}
						}
					} else if ((field.getType() == byte[].class) || (field.getType() == Byte[].class)) {
						fieldType = TEXTO_BASE_64;
					} else if (((ReflectionUtils.isImplementsInterface(field.getType(), Collection.class)
							|| ReflectionUtils.isImplementsInterface(field.getType(), Set.class)))) {
						fieldType = COLECAO;

						if (field.isAnnotationPresent(Fetch.class) && (((Fetch) field.getAnnotation(Fetch.class)).mode()
								.equals(FetchMode.ELEMENT_COLLECTION))) {
							if (field.isAnnotationPresent(MapKeyColumn.class)) {
								Class<?> clazz2 = ReflectionUtils.getGenericMapTypes(field).get(1);
								fieldValues = clazz2.getCanonicalName();
							} else {
								ParameterizedType listType = (ParameterizedType) field.getGenericType();
								Class<?> clazz2 = (Class<?>) listType.getActualTypeArguments()[0];
								fieldValues = clazz2.getCanonicalName();
							}
						} else {
							Class<?> genericType = ReflectionUtils.getGenericType(field);
							String key = null;
							if (!anchors.containsKey(genericType.getCanonicalName())) {
								anchors.put(genericType.getCanonicalName(), UUID.randomUUID().toString());
							} else {
								key = anchors.get(genericType.getCanonicalName());
							}
							fieldType += ENTIDADES;
							fieldValues = "<<" + key + ", " + genericType.getCanonicalName() + ">>";
						}
					} else if (field.getType().isEnum()) {
						fieldType = ENUMERACAO;
						boolean appendDelimiter = false;
						if (field.isEnumConstant()) {
							for (Object ev : field.getType().getEnumConstants()) {
								if (appendDelimiter)
									fieldValues += ", ";
								fieldValues += ev.toString();
								appendDelimiter = true;
							}
						} else {
							Method method = field.getType().getDeclaredMethod(VALUES);
							Object obj = method.invoke(null);
							if (obj != null) {
								for (Object ev : (Object[]) obj) {
									if (appendDelimiter)
										fieldValues += ", ";
									fieldValues += ev.toString();
									appendDelimiter = true;
								}
							}
						}
					} else if ((field.getType() == boolean.class) || (field.getType() == Boolean.class)) {
						fieldType = BOOLEANO;
						fieldValues = "True, False";
					} else {
						fieldType = ENTIDADE;
						String key = null;
						if (!anchors.containsKey(field.getType().getCanonicalName())) {
							anchors.put(field.getType().getCanonicalName(), UUID.randomUUID().toString());
						} else {
							key = anchors.get(field.getType().getCanonicalName());
						}
						fieldValues = "<<" + key + ", " + field.getType().getCanonicalName() + ">>";
					}

					if (field.isAnnotationPresent(Id.class)) {
						fieldName = "*" + fieldName + "*" + " -> {anteros-id}";
					} else if (field.isAnnotationPresent(TenantId.class)) {
						fieldName = "*" + fieldName + "*" + " -> {anteros-tenant-id}";
					} else if (field.isAnnotationPresent(CompanyId.class)) {
						fieldName = "*" + fieldName + "*" + " -> {anteros-company-id}";
					} else if (field.isAnnotationPresent(Code.class)) {
						fieldName = "*" + fieldName + "*" + " -> {anteros-code}";
					} else if (field.isAnnotationPresent(Version.class)) {
						fieldName = "*" + fieldName + "*" + " -> {anteros-version}";
					} else {
						fieldName = "*" + fieldName + "*";
					}
					dataModelItem.put(FIELD_NAME, fieldName);
					dataModelItem.put(FIELD_TYPE, fieldType);
					dataModelItem.put(FIELD_VALUES, fieldValues);
					dataModelItem.put(FIELD_OPTIONAL, fieldOptional);
					dataModelItem.put(FIELD_DESCRIPTION, fieldDescription);
					itemResourceTemplate.process(dataModelItem, sw);
					sw.flush();
					resourceItems.append(sw.toString()).append("\n");
					sw.close();
				}

				dataModel.put(MODEL_ITEM, resourceItems.toString());
			} else {
				dataModel.put(MODEL_ITEM, "");
			}
			template.process(dataModel, w);
			w.flush();
		}
	}

	public static void generateDataIntegrationPersistence(String baseUrl, Writer w, List<JavaClass> integrationClasses,
			List<JavaClass> allClasses, Map<String, String> anchors)
			throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException,
			TemplateException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException,
			IllegalArgumentException, InvocationTargetException {
		Configuration configuration = new Configuration();
		configuration.setEncoding(Locale.getDefault(), "UTF-8");
		configuration.setTemplateLoader(new AnterosFreeMarkerTemplateLoader(AnterosRestDoclet.class, "/"));
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

		for (JavaClass cld : integrationClasses) {

			try {
				Template template = configuration.getTemplate(TEMPLATE_INTEGRATION_PERSISTENCE);
				RemoteSynchDataIntegration remoteIntegration;
				Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(cld.getCanonicalName());
				Field[] fields = ReflectionUtils.getAllDeclaredFields(clazz);
				if (clazz.isAnnotationPresent(DiscriminatorColumn.class)) {
					fields = getAllFields(clazz, allClasses);
				}

				Template itemMobileTemplate = configuration.getTemplate(TEMPLATE_INTEGRATION_PERSISTENCE_ITEM);
				StringBuilder mobileItems = new StringBuilder();

				if (clazz.isAnnotationPresent(DiscriminatorColumn.class)) {
					String fieldName = TYPE;
					String fieldType = TEXTO;
					DiscriminatorColumn dc = (DiscriminatorColumn) clazz.getAnnotation(DiscriminatorColumn.class);
					if (dc.discriminatorType().equals(DiscriminatorType.STRING)
							|| dc.discriminatorType().equals(DiscriminatorType.CHAR)) {
						if (dc.length() > 0) {
							fieldType += "(" + dc.length() + ")";
						}
					} else {
						fieldType = NUMERICO;
						fieldType += "(" + dc.length() + ")";
					}

					String fieldValues = "";
					boolean appendDelimiter = false;
					for (JavaClass cld2 : allClasses) {
						try {
							Class<?> clazz2 = Thread.currentThread().getContextClassLoader()
									.loadClass(cld2.getCanonicalName());
							if (ReflectionUtils.isExtendsClass(clazz, clazz2)) {
								if (clazz2.isAnnotationPresent(DiscriminatorValue.class)) {
									if (appendDelimiter) {
										fieldValues += ", ";
									}
									String key = anchors.get("_" + clazz2.getCanonicalName());

									DiscriminatorValue dv = clazz2.getAnnotation(DiscriminatorValue.class);
									fieldValues += "<<" + key + ", " + dv.value() + ">>";
									appendDelimiter = true;
								}
							}
						} catch (Exception e) {
						}
					}
					String fieldDescription = "Tipo de identificação para classes concretas.";
					Map<String, Object> dataModelItem = new HashMap<String, Object>();
					StringWriter sw = new StringWriter();

					dataModelItem.put(FIELD_NAME, "*" + fieldName + "*" + " -> {anteros-type}");
					dataModelItem.put(FIELD_TYPE, fieldType);
					dataModelItem.put(FIELD_VALUES, fieldValues);
					dataModelItem.put(FIELD_OPTIONAL, NAO);
					dataModelItem.put(FIELD_DESCRIPTION, fieldDescription);
					itemMobileTemplate.process(dataModelItem, sw);
					sw.flush();
					mobileItems.append(sw.toString()).append("\n");
					sw.close();
				}

				for (Field field : fields) {
					if (field.isAnnotationPresent(Transient.class) || field.isAnnotationPresent(JsonIgnore.class)
							|| field.isAnnotationPresent(RemoteSynchIntegrationIgnore.class)
							|| field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(CompositeId.class)
							|| field.isAnnotationPresent(Version.class)) {
						continue;
					}

					Map<String, Object> dataModelItem = new HashMap<String, Object>();
					StringWriter sw = new StringWriter();
					
					if (!generateField(field,dataModelItem, anchors)){
						continue;
					}
					
					itemMobileTemplate.process(dataModelItem, sw);
					sw.flush();
					mobileItems.append(sw.toString()).append("\n");
					sw.close();
				}

				remoteIntegration = getRemoteSynchDataIntegrationAnnotation(clazz);
				
				String direction = "";
				
				if (remoteIntegration.direction()[0].equals(DataSynchDirection.SEND)) {
					direction = " {anteros-send}";
				}
				
				if (remoteIntegration.direction().length==2) {
					direction = " {anteros-send} {anteros-receive}";
				} else if (remoteIntegration.direction()[0].equals(DataSynchDirection.RECEIVE)) {
					direction = " {anteros-receive}";
				}

				Map<String, Object> dataModel = new HashMap<String, Object>();
				dataModel.put(ENTITY_NAME, remoteIntegration.name()+direction);
				dataModel.put(ENTITY_ID, anchors.get("_" + remoteIntegration.name()));
				dataModel.put(ENTITY_DESCRIPTION,
						(StringUtils.isEmpty(cld.getComment()) ? remoteIntegration.description() : cld.getComment()));
				dataModel.put(MODEL_ITEM, mobileItems.toString());
				template.process(dataModel, w);
				w.flush();
				
				/*
				 * Processando JoinTables
				 */
				for (Field field : fields) {
					if (field.isAnnotationPresent(RemoteSynchDataIntegration.class)) {						
						RemoteSynchDataIntegration annotation = field.getAnnotation(RemoteSynchDataIntegration.class);
						remoteIntegration = getRemoteSynchDataIntegrationAnnotation(field.getClass());
						generateDataIntegrationJoinTablePersistence(integrationClasses, fields, field, anchors,configuration, annotation, w);
					}
				}				
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	
	private static boolean generateField(Field field, Map<String, Object> dataModelItem, Map<String, String> anchors) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String fieldName = field.getName();
		String fieldType = "";
		String fieldDescription = "";
		String fieldOptional = SIM;
		String fieldValues = "";

		if (field.isAnnotationPresent(Column.class) && !field.isAnnotationPresent(Version.class)) {
			Column ann = field.getAnnotation(Column.class);
			fieldDescription = ann.label();
			if (ann.required()) {
				fieldOptional = NAO;
			}
			Required requiredAnn = field.getAnnotation(Required.class);
			if (requiredAnn != null) {
				fieldOptional = NAO;
			}
		}

		if (field.isAnnotationPresent(Label.class)) {
			Label ann = field.getAnnotation(Label.class);
			fieldDescription = ann.value();
		}

		if (ReflectionUtils.isExtendsClass(BigInteger.class, field.getType())) {
			fieldType = NUMERICO;
			if (field.isAnnotationPresent(Column.class)) {
				Column ann = field.getAnnotation(Column.class);
				if (ann.precision() > 0 == ann.scale() > 0) {
					fieldType = fieldType + "(" + ann.precision() + "," + ann.scale() + ")";
				} else if (ann.precision() > 0) {
					fieldType = fieldType + "(" + ann.precision() + ")";
				}
			}
		} else if (ReflectionUtils.isExtendsClass(BigDecimal.class, field.getType())) {
			fieldType = NUMERICO;
			if (field.isAnnotationPresent(Column.class)) {
				Column ann = field.getAnnotation(Column.class);
				if (ann.precision() > 0 && ann.scale() > 0) {
					fieldType = fieldType + "(" + ann.precision() + "," + ann.scale() + ")";
				} else if (ann.precision() > 0) {
					fieldType = fieldType + "(" + ann.precision() + ")";
				}
			}
		} else if (ReflectionUtils.isExtendsClass(Number.class, field.getType())) {
			fieldType = NUMERICO;
			if (field.isAnnotationPresent(Column.class)) {
				Column ann = field.getAnnotation(Column.class);
				if (ann.precision() > 0 && ann.scale() > 0) {
					fieldType = fieldType + "(" + ann.precision() + "," + ann.scale() + ")";
				} else if (ann.precision() > 0) {
					fieldType = fieldType + "(" + ann.precision() + ")";
				}
			}
		} else if (ReflectionUtils.isExtendsClass(String.class, field.getType())) {
			fieldType = TEXTO;
			if (field.isAnnotationPresent(Column.class)) {
				Column ann = field.getAnnotation(Column.class);
				if (ann.length() > 0) {
					fieldType = fieldType + "(" + ann.length() + ")";
				}
			}
		} else if (ReflectionUtils.isExtendsClass(Date.class, field.getType())
				|| (ReflectionUtils.isExtendsClass(java.sql.Date.class, field.getType()))) {
			if (field.isAnnotationPresent(Temporal.class)) {
				Temporal temp = field.getAnnotation(Temporal.class);
				if (temp.value() == TemporalType.DATE) {
					fieldType = "Data : yyyy-MM-dd";
				}
				if (temp.value() == TemporalType.DATE_TIME) {
					fieldType = "Data/hora : yyyy-MM-dd'T'HH:mm:ss.SSS";
				}
				if (temp.value() == TemporalType.TIME) {
					fieldType = "Hora : HH:mm:ss.SSS";
				}
			}
		} else if ((field.getType() == byte[].class) || (field.getType() == Byte[].class)) {
			fieldType = TEXTO_BASE_64;
			
		} else if (((ReflectionUtils.isImplementsInterface(field.getType(), Collection.class)
				|| ReflectionUtils.isImplementsInterface(field.getType(), Set.class)))) {
			fieldType = COLECAO;

			if (field.isAnnotationPresent(Fetch.class) && (((Fetch) field.getAnnotation(Fetch.class)).mode()
					.equals(FetchMode.ELEMENT_COLLECTION))) {
				if (field.isAnnotationPresent(MapKeyColumn.class)) {
					Class<?> clazzG = ReflectionUtils.getGenericMapTypes(field).get(1);
					fieldValues = clazzG.getCanonicalName();
				}						
			} else {
				return false;
			}
		} else if (field.getType().isEnum()) {
			fieldType = ENUMERACAO;
			boolean appendDelimiter = false;
			if (field.isEnumConstant()) {
				for (Object ev : field.getType().getEnumConstants()) {
					if (appendDelimiter)
						fieldValues += ", ";
					fieldValues += ev.toString();
					appendDelimiter = true;
				}
			} else {
				Method method = field.getType().getDeclaredMethod(VALUES);
				Object obj = method.invoke(null);
				if (obj != null) {
					for (Object ev : (Object[]) obj) {
						if (appendDelimiter)
							fieldValues += ", ";
						fieldValues += ev.toString();
						appendDelimiter = true;
					}
				}
			}
		} else if ((field.getType() == boolean.class) || (field.getType() == Boolean.class)) {
			fieldType = BOOLEANO;
			fieldValues = "True, False";
		} else {
			Field codeField = getCodeField(field.getType());
			if (codeField == null)
				return false;

			RemoteSynchDataIntegration remoteIntegration = getRemoteSynchDataIntegrationAnnotation(field.getType());
			fieldType += ENTIDADE;
			if (remoteIntegration != null) {
				fieldValues = "Chave {" + codeField.getName() + "} -> <<"
						+ anchors.get("_" + remoteIntegration.name()) + ", " + remoteIntegration.name()
						+ ">>";
			} else {
				fieldValues = "Chave {" + codeField.getName() + "}";
			}
		}

		if (field.isAnnotationPresent(Id.class)) {
			fieldName = "*" + fieldName + "*" + " -> {anteros-id}";
		} else if (field.isAnnotationPresent(TenantId.class)) {
			fieldName = "*" + fieldName + "*" + " -> {anteros-tenant-id}";
		} else if (field.isAnnotationPresent(CompanyId.class)) {
			fieldName = "*" + fieldName + "*" + " -> {anteros-company-id}";
		} else if (field.isAnnotationPresent(Code.class)) {
			fieldName = "*" + fieldName + "*" + " -> {anteros-code}";
		} else if (field.isAnnotationPresent(Version.class)) {
			fieldName = "*" + fieldName + "*" + " -> {anteros-version}";
		} else {
			fieldName = "*" + fieldName + "*";
		}
		dataModelItem.put(FIELD_NAME, fieldName);
		dataModelItem.put(FIELD_TYPE, fieldType);
		dataModelItem.put(FIELD_VALUES, fieldValues);
		dataModelItem.put(FIELD_OPTIONAL, fieldOptional);
		dataModelItem.put(FIELD_DESCRIPTION, fieldDescription);
		return true;
	}
	
	
	public static void generateDataIntegrationJoinTablePersistence(List<JavaClass> integrationClasses, Field[] fields, Field field, Map<String, String> anchors, Configuration configuration, RemoteSynchDataIntegration annotation, Writer w) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Template template = configuration.getTemplate(TEMPLATE_INTEGRATION_PERSISTENCE);
		
		Template itemMobileTemplate = configuration.getTemplate(TEMPLATE_INTEGRATION_PERSISTENCE_ITEM);
		StringBuilder mobileItems = new StringBuilder();
		
		
		Map<String, Object> dataModelItem = new HashMap<String, Object>();
		StringWriter sw = new StringWriter();
		
		
		for (Field fld : fields) {
			if (fld.isAnnotationPresent(Code.class)) {
				generateField(fld,dataModelItem, anchors);
				dataModelItem.put(FIELD_OPTIONAL, NAO);
			}
		}
		
		itemMobileTemplate.process(dataModelItem, sw);
		sw.flush();
		mobileItems.append(sw.toString()).append("\n");
		sw.close();
		
		
		dataModelItem = new HashMap<String, Object>();
		sw = new StringWriter();
		
		ParameterizedType listType = (ParameterizedType) field.getGenericType();
		Class<?> clazz2 = (Class<?>) listType.getActualTypeArguments()[0];
		
		for (JavaClass jc : integrationClasses) {
			if (jc.getCanonicalName().equals(clazz2.getCanonicalName())) {
				Field[] fieldsTarget = ReflectionUtils.getAllDeclaredFields(clazz2);
				for (Field fd : fieldsTarget) {
					if (fd.isAnnotationPresent(Code.class)) {
						generateField(fd,dataModelItem, anchors);
						dataModelItem.put(FIELD_OPTIONAL, NAO);
					}
				}
			}
		}	
		
		itemMobileTemplate.process(dataModelItem, sw);
		sw.flush();
		mobileItems.append(sw.toString()).append("\n");
		sw.close();
		
		
		Map<String, Object> dataModel = new HashMap<String, Object>();
		dataModel.put(ENTITY_NAME, annotation.name());
		dataModel.put(ENTITY_ID, UUID.randomUUID().toString());
		dataModel.put(ENTITY_DESCRIPTION,annotation.description());
		dataModel.put(MODEL_ITEM, mobileItems.toString());
		template.process(dataModel, w);
		w.flush();

		
	}

	public static void generateMobilePersistence(String baseUrl, Writer w, List<JavaClass> mobileClasses,
			List<JavaClass> allClasses, Map<String, String> anchors)
			throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException,
			TemplateException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException,
			IllegalArgumentException, InvocationTargetException {
		Configuration configuration = new Configuration();
		configuration.setEncoding(Locale.getDefault(), "UTF-8");
		configuration.setTemplateLoader(new AnterosFreeMarkerTemplateLoader(AnterosRestDoclet.class, "/"));
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

		for (JavaClass cld : mobileClasses) {

			try {
				Template template = configuration.getTemplate(TEMPLATE_MOBILE_PERSISTENCE);
				Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(cld.getCanonicalName());
				RemoteSynchMobile remoteSynchMobile = clazz.getAnnotation(RemoteSynchMobile.class);
				Field[] fields = ReflectionUtils.getAllDeclaredFields(clazz);
				if (clazz.isAnnotationPresent(DiscriminatorColumn.class)) {
					fields = getAllFields(clazz, allClasses);
				}
				Template itemIntegrationTemplate = configuration.getTemplate(TEMPLATE_MOBILE_PERSISTENCE_ITEM);
				StringBuilder mobileItems = new StringBuilder();
				StringBuilder schemaRealm = new StringBuilder();
				schemaRealm.append("class " + remoteSynchMobile.name() + " extends AnterosRealmModel {\n");
				schemaRealm.append("    static schema = {\n");
				schemaRealm.append("        name: '" + remoteSynchMobile.name() + "',\n");

				for (Field field : fields) {
					if (field.isAnnotationPresent(Transient.class) || field.isAnnotationPresent(JsonIgnore.class)
							|| field.isAnnotationPresent(RemoteSynchMobileIgnore.class)) {
						continue;
					}
					if (field.isAnnotationPresent(Id.class)) {
						schemaRealm.append("        primaryKey: '" + field.getName() + "',\n");
					}
				}
				schemaRealm.append("        properties: {\n");

				if (clazz.isAnnotationPresent(DiscriminatorColumn.class)) {
					String fieldName = TYPE;
					String fieldType = TEXTO;
					DiscriminatorColumn dc = (DiscriminatorColumn) clazz.getAnnotation(DiscriminatorColumn.class);
					if (dc.discriminatorType().equals(DiscriminatorType.STRING)
							|| dc.discriminatorType().equals(DiscriminatorType.CHAR)) {
						if (dc.length() > 0) {
							fieldType += "(" + dc.length() + ")";
						}
					} else {
						fieldType = NUMERICO;
						fieldType += "(" + dc.length() + ")";
					}

					String fieldValues = "";
					boolean appendDelimiter = false;
					for (JavaClass cld2 : allClasses) {
						try {
							Class<?> clazz2 = Thread.currentThread().getContextClassLoader()
									.loadClass(cld2.getCanonicalName());
							if (ReflectionUtils.isExtendsClass(clazz, clazz2)) {
								if (clazz2.isAnnotationPresent(DiscriminatorValue.class)) {
									if (appendDelimiter) {
										fieldValues += ", ";
									}
									String key = anchors.get(clazz2.getCanonicalName());

									DiscriminatorValue dv = clazz2.getAnnotation(DiscriminatorValue.class);
									fieldValues += "<<" + key + ", " + dv.value() + ">>";
									appendDelimiter = true;
								}
							}
						} catch (Exception e) {
						}
					}
					String fieldDescription = "Tipo de identificação para classes concretas.";
					Map<String, Object> dataModelItem = new HashMap<String, Object>();
					StringWriter sw = new StringWriter();

					dataModelItem.put(FIELD_NAME, "*" + fieldName + "* -> {anteros-version}");
					dataModelItem.put(FIELD_TYPE, fieldType);
					dataModelItem.put(FIELD_VALUES, fieldValues);
					dataModelItem.put(FIELD_OPTIONAL, NAO);
					dataModelItem.put(FIELD_DESCRIPTION, fieldDescription);
					itemIntegrationTemplate.process(dataModelItem, sw);
					sw.flush();
					mobileItems.append(sw.toString()).append("\n");
					sw.close();
					schemaRealm.append("            type: 'string',\n");
				}

				for (Field field : fields) {
					if (field.isAnnotationPresent(Transient.class) || field.isAnnotationPresent(JsonIgnore.class)
							|| field.isAnnotationPresent(RemoteSynchMobileIgnore.class)) {
						continue;
					}

					Map<String, Object> dataModelItem = new HashMap<String, Object>();
					StringWriter sw = new StringWriter();

					String fieldName = field.getName();
					String fieldType = "";
					String fieldDescription = "";
					String fieldOptional = SIM;
					String fieldValues = "";
					String required = "?";

					if (field.isAnnotationPresent(Column.class)) {
						Column ann = field.getAnnotation(Column.class);
						fieldDescription = ann.label();
						if (ann.required()) {
							fieldOptional = NAO;
							required = "";
						}
					}

					if (field.isAnnotationPresent(Label.class)) {
						Label ann = field.getAnnotation(Label.class);
						fieldDescription = ann.value();
					}

					if (ReflectionUtils.isExtendsClass(BigInteger.class, field.getType())) {
						fieldType = NUMERICO;
						if (field.isAnnotationPresent(Column.class)) {
							Column ann = field.getAnnotation(Column.class);
							if (ann.precision() > 0 == ann.scale() > 0) {
								fieldType = fieldType + "(" + ann.precision() + "," + ann.scale() + ")";
							} else if (ann.precision() > 0) {
								fieldType = fieldType + "(" + ann.precision() + ")";
							}
						}
						schemaRealm.append("            " + fieldName + ": 'int" + required + "',\n");
					} else if (ReflectionUtils.isExtendsClass(BigDecimal.class, field.getType())) {
						fieldType = NUMERICO;
						if (field.isAnnotationPresent(Column.class)) {
							Column ann = field.getAnnotation(Column.class);
							if (ann.precision() > 0 && ann.scale() > 0) {
								fieldType = fieldType + "(" + ann.precision() + "," + ann.scale() + ")";
							} else if (ann.precision() > 0) {
								fieldType = fieldType + "(" + ann.precision() + ")";
							}
						}
						schemaRealm.append("            " + fieldName + ": 'double" + required + "',\n");
					} else if (ReflectionUtils.isExtendsClass(Number.class, field.getType())) {
						fieldType = NUMERICO;
						if (field.isAnnotationPresent(Column.class)) {
							Column ann = field.getAnnotation(Column.class);
							if (ann.precision() > 0 && ann.scale() > 0) {
								fieldType = fieldType + "(" + ann.precision() + "," + ann.scale() + ")";
							} else if (ann.precision() > 0) {
								fieldType = fieldType + "(" + ann.precision() + ")";
							}
						}
						if (ReflectionUtils.isExtendsClass(Long.class, field.getType())
								|| ReflectionUtils.isExtendsClass(Integer.class, field.getType())
								|| ReflectionUtils.isExtendsClass(BigInteger.class, field.getType())) {
							schemaRealm.append("            " + fieldName + ": 'int',\n");
						} else {
							schemaRealm.append("            " + fieldName + ": 'double" + required + "',\n");
						}
					} else if (ReflectionUtils.isExtendsClass(String.class, field.getType())) {
						fieldType = TEXTO;
						if (field.isAnnotationPresent(Column.class)) {
							Column ann = field.getAnnotation(Column.class);
							if (ann.length() > 0) {
								fieldType = fieldType + "(" + ann.length() + ")";
							}
						}
						schemaRealm.append("            " + fieldName + ": 'string" + required + "',\n");
					} else if (ReflectionUtils.isExtendsClass(Date.class, field.getType())
							|| (ReflectionUtils.isExtendsClass(java.sql.Date.class, field.getType()))) {
						if (field.isAnnotationPresent(Temporal.class)) {
							Temporal temp = field.getAnnotation(Temporal.class);
							if (temp.value() == TemporalType.DATE) {
								fieldType = "Data : yyyy-MM-dd";
							}
							if (temp.value() == TemporalType.DATE_TIME) {
								fieldType = "Data/hora : yyyy-MM-dd'T'HH:mm:ss.SSS";
							}
							if (temp.value() == TemporalType.TIME) {
								fieldType = "Hora : HH:mm:ss.SSS";
							}
						}
						schemaRealm.append("            " + fieldName + ": 'date',\n");
					} else if ((field.getType() == byte[].class) || (field.getType() == Byte[].class)) {
						fieldType = TEXTO_BASE_64;
						schemaRealm.append("            " + fieldName + ": 'string" + required + "',\n");
					} else if (((ReflectionUtils.isImplementsInterface(field.getType(), Collection.class)
							|| ReflectionUtils.isImplementsInterface(field.getType(), Set.class)))) {
						fieldType = COLECAO;

						if (field.isAnnotationPresent(Fetch.class) && (((Fetch) field.getAnnotation(Fetch.class)).mode()
								.equals(FetchMode.ELEMENT_COLLECTION))) {
							if (field.isAnnotationPresent(MapKeyColumn.class)) {
								Class<?> clazzG = ReflectionUtils.getGenericMapTypes(field).get(1);
								fieldValues = clazzG.getCanonicalName();
								if (ReflectionUtils.isExtendsClass(clazzG, Number.class)) {
									if (ReflectionUtils.isExtendsClass(clazzG, Long.class)
											|| ReflectionUtils.isExtendsClass(clazzG, Integer.class)
											|| ReflectionUtils.isExtendsClass(clazzG, BigInteger.class)) {
										schemaRealm.append("            " + fieldName + ": 'int[]" + required + "',\n");
									} else {
										schemaRealm
												.append("            " + fieldName + ": 'double[]" + required + "',\n");
									}
								} else if (ReflectionUtils.isExtendsClass(clazzG, Date.class)) {
									schemaRealm.append("            " + fieldName + ": 'date[]" + required + "',\n");
								} else if (ReflectionUtils.isExtendsClass(clazzG, String.class)) {
									schemaRealm.append("            " + fieldName + ": 'string[]" + required + "',\n");
								}
							} else {
								ParameterizedType listType = (ParameterizedType) field.getGenericType();
								Class<?> clazzG = (Class<?>) listType.getActualTypeArguments()[0];
								fieldValues = clazzG.getCanonicalName();
								if (ReflectionUtils.isExtendsClass(clazzG, Number.class)) {
									if (ReflectionUtils.isExtendsClass(clazzG, Long.class)
											|| ReflectionUtils.isExtendsClass(clazzG, Integer.class)
											|| ReflectionUtils.isExtendsClass(clazzG, BigInteger.class)) {
										schemaRealm.append("            " + fieldName + ": 'int[]" + required + "',\n");
									} else {
										schemaRealm
												.append("            " + fieldName + ": 'double[]" + required + "',\n");
									}
								} else if (ReflectionUtils.isExtendsClass(clazzG, Date.class)) {
									schemaRealm.append("            " + fieldName + ": 'date[]" + required + "',\n");
								} else if (ReflectionUtils.isExtendsClass(clazzG, String.class)) {
									schemaRealm.append("            " + fieldName + ": 'string[]" + required + "',\n");
								}
							}
						} else {
							Class<?> genericType = ReflectionUtils.getGenericType(field);
							Field[] pkFields = getPkFields(genericType);
							boolean appendDelimiter = false;
							String fds = "";
							for (Field fd : pkFields) {
								if (appendDelimiter) {
									fds += ",";
								}
								fds += fd.getName();
							}

							remoteSynchMobile = getRemoteSynchMobileAnnotation(genericType);
							fieldType += ENTIDADES;
							if (remoteSynchMobile != null) {
								fieldValues = "Chave [{" + fds + "}] -> <<" + anchors.get(remoteSynchMobile.name())
										+ ", " + remoteSynchMobile.name() + ">>";
								schemaRealm.append(
										"            " + fieldName + ": '" + remoteSynchMobile.name() + "[]',\n");
							} else {
								fieldValues = "Chave [{" + fds + "}]";
								schemaRealm.append("            " + fieldName + ": 'string[]" + required + "',\n");
							}
						}
					} else if (field.getType().isEnum()) {
						fieldType = ENUMERACAO;
						boolean appendDelimiter = false;
						if (field.isEnumConstant()) {
							for (Object ev : field.getType().getEnumConstants()) {
								if (appendDelimiter)
									fieldValues += ", ";
								fieldValues += ev.toString();
								appendDelimiter = true;
							}
						} else {
							Method method = field.getType().getDeclaredMethod(VALUES);
							Object obj = method.invoke(null);
							if (obj != null) {
								for (Object ev : (Object[]) obj) {
									if (appendDelimiter)
										fieldValues += ", ";
									fieldValues += ev.toString();
									appendDelimiter = true;
								}
							}
						}
						schemaRealm.append("            " + fieldName + ": 'string" + required + "',\n");
					} else if ((field.getType() == boolean.class) || (field.getType() == Boolean.class)) {
						fieldType = BOOLEANO;
						fieldValues = "True, False";
						schemaRealm.append("            " + fieldName + ": 'bool" + required + "',\n");
					} else {
						Field[] pkFields = getPkFields(field.getType());
						boolean appendDelimiter = false;
						String fds = "";
						for (Field fd : pkFields) {
							if (appendDelimiter) {
								fds += ",";
							}
							fds += fd.getName();
						}

						remoteSynchMobile = getRemoteSynchMobileAnnotation(field.getType());
						fieldType += ENTIDADE;
						if (remoteSynchMobile != null) {
							fieldValues = "Chave {" + fds + "} -> <<" + anchors.get(remoteSynchMobile.name()) + ", "
									+ remoteSynchMobile.name() + ">>";
							schemaRealm.append("            " + fieldName + ": '" + remoteSynchMobile.name() + ""
									+ required + "',\n");
						} else {
							fieldValues = "Chave {" + fds + "}";
							schemaRealm.append("            " + fieldName + ": 'string" + required + "',\n");
						}
					}

					if (field.isAnnotationPresent(Id.class)) {
						fieldName = "*" + fieldName + "*" + " -> {anteros-id}";
					} else if (field.isAnnotationPresent(TenantId.class)) {
						fieldName = "*" + fieldName + "*" + " -> {anteros-tenant-id}";
					} else if (field.isAnnotationPresent(CompanyId.class)) {
						fieldName = "*" + fieldName + "*" + " -> {anteros-company-id}";
					} else if (field.isAnnotationPresent(Code.class)) {
						fieldName = "*" + fieldName + "*" + " -> {anteros-code}";
					} else if (field.isAnnotationPresent(Version.class)) {
						fieldName = "*" + fieldName + "*" + " -> {anteros-version}";
					} else {
						fieldName = "*" + fieldName + "*";
					}
					dataModelItem.put(FIELD_NAME, fieldName);
					dataModelItem.put(FIELD_TYPE, fieldType);
					dataModelItem.put(FIELD_VALUES, fieldValues);
					dataModelItem.put(FIELD_OPTIONAL, fieldOptional);
					dataModelItem.put(FIELD_DESCRIPTION, fieldDescription);
					itemIntegrationTemplate.process(dataModelItem, sw);
					sw.flush();
					mobileItems.append(sw.toString()).append("\n");
					sw.close();
				}
				schemaRealm.append("        },\n");
				schemaRealm.append("    };\n");
				schemaRealm.append("}\n");
				remoteSynchMobile = getRemoteSynchMobileAnnotation(clazz);
				Map<String, Object> dataModel = new HashMap<String, Object>();
				dataModel.put(MOBILE_NAME, remoteSynchMobile.name());
				dataModel.put(ENTITY_ID, anchors.get(remoteSynchMobile.name()));
				dataModel.put(MOBILE_DESCRIPTION,
						(StringUtils.isEmpty(cld.getComment()) ? remoteSynchMobile.description() : cld.getComment()));
				dataModel.put(MODEL_ITEM, mobileItems.toString());
				dataModel.put(SCHEMA, schemaRealm.toString());
				template.process(dataModel, w);
				w.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public static RemoteSynchMobile getRemoteSynchMobileAnnotation(Class<?> clazz) {
		while (clazz != Object.class) {
			RemoteSynchMobile annotation = clazz.getAnnotation(RemoteSynchMobile.class);
			if (annotation != null) {
				return annotation;
			}
			clazz = clazz.getSuperclass();
		}
		return null;
	}

	public static RemoteSynchDataIntegration getRemoteSynchDataIntegrationAnnotation(Class<?> clazz) {
		while (clazz != Object.class) {
			RemoteSynchDataIntegration annotation = clazz.getAnnotation(RemoteSynchDataIntegration.class);
			if (annotation != null) {
				return annotation;
			}
			clazz = clazz.getSuperclass();
		}
		return null;
	}

	public static Field[] getPkFields(Class<?> clazz) {
		List<Field> result = new ArrayList<>();
		Field[] fields = ReflectionUtils.getAllDeclaredFieldsAnnotatedWith(clazz, Id.class, CompositeId.class);

		for (Field field : fields) {
			if (field.isAnnotationPresent(ForeignKey.class)) {
				result.addAll(Arrays.asList(getPkFields(field.getType())));
			} else {
				result.add(field);
			}
		}
		return result.toArray(new Field[] {});
	}

	public static Field getCodeField(Class<?> clazz) {
		Field[] fields = ReflectionUtils.getAllDeclaredFieldsAnnotatedWith(clazz, Code.class);

		for (Field field : fields) {
			if (field.isAnnotationPresent(Code.class)) {
				return field;
			}
		}
		return null;
	}

	public static Field[] getAllFields(Class<?> sourceClass, List<JavaClass> allClasses) throws ClassNotFoundException {
		List<Field> result = new ArrayList<>();
		for (JavaClass jc : allClasses) {
			Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(jc.getCanonicalName());
			if (jc.getCanonicalName().equals(sourceClass.getCanonicalName())) {
				result.addAll(Arrays.asList(ReflectionUtils.getAllDeclaredFields(clazz)));
				for (JavaClass drv : jc.getDerivedClasses()) {
					Class<?> clazzDeriv = Thread.currentThread().getContextClassLoader()
							.loadClass(drv.getCanonicalName());
					result.addAll(Arrays.asList(clazzDeriv.getDeclaredFields()));
				}
				break;
			}
		}
		return result.toArray(new Field[] {});
	}

	public static ObjectMapper getObjectMapper() {
		if (objectMapper == null) {
			objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"));
			objectMapper.setSerializationInclusion(Include.NON_NULL);
			objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
			objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		}
		return objectMapper;
	}
}
