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
package br.com.anteros.restdoc.maven.plugin.doclet.collector;

import static br.com.anteros.restdoc.maven.plugin.util.CommonUtils.firstNonEmpty;
import static br.com.anteros.restdoc.maven.plugin.util.CommonUtils.fixPath;
import static br.com.anteros.restdoc.maven.plugin.util.CommonUtils.isEmpty;
import static br.com.anteros.restdoc.maven.plugin.util.TagUtils.CONTEXT_TAG;
import static br.com.anteros.restdoc.maven.plugin.util.TagUtils.IGNORE_TAG;
import static br.com.anteros.restdoc.maven.plugin.util.TagUtils.NAME_TAG;
import static br.com.anteros.restdoc.maven.plugin.util.TagUtils.firstSentence;
import static java.util.Collections.emptyList;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.restdoc.maven.plugin.doclet.model.ClassDescriptor;
import br.com.anteros.restdoc.maven.plugin.doclet.model.Endpoint;
import br.com.anteros.restdoc.maven.plugin.doclet.model.PathVar;
import br.com.anteros.restdoc.maven.plugin.doclet.model.QueryParam;
import br.com.anteros.restdoc.maven.plugin.doclet.model.RequestBody;
import com.sun.source.util.DocTrees;


import static java.util.Collections.emptyList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;
import jdk.javadoc.doclet.DocletEnvironment;
import static br.com.anteros.restdoc.maven.plugin.util.CommonUtils.*;
import static br.com.anteros.restdoc.maven.plugin.util.TagUtils.*;

public abstract class AbstractCollector implements Collector {

	protected final DocTrees treeUtils;

	public AbstractCollector(DocTrees treeUtils) {
		this.treeUtils = treeUtils;
	}

	protected abstract boolean shouldIgnoreClass(TypeElement classDoc);
	protected abstract boolean shouldIgnoreMethod(ExecutableElement methodDoc);
	protected abstract EndpointMapping getEndpointMapping(Element doc);
	protected abstract Collection<PathVar> generatePathVars(ExecutableElement methodDoc);
	protected abstract Collection<QueryParam> generateQueryParams(ExecutableElement methodDoc);
	protected abstract RequestBody generateRequestBody(ExecutableElement methodDoc);

	/**
	 * Irá gerar e agregar todos os descritores de classe de endpoint restantes.
	 * @param rootDoc
	 * @return
	 */
	@Override
	public Collection<ClassDescriptor> getDescriptors(DocletEnvironment rootDoc) {
		Collection<ClassDescriptor> classDescriptors = new ArrayList<>();

		//Faça um loop por todas as classes e, se contiver pontos de extremidade, adicione-o ao conjunto de descritores.
		for (Element e : rootDoc.getIncludedElements()) {
			if (e instanceof TypeElement) {
				TypeElement classDoc = (TypeElement) e;
				ClassDescriptor descriptor = getClassDescriptor(classDoc);
				if (descriptor != null && !isEmpty(descriptor.getEndpoints())) {
					descriptor.setClazzName(classDoc.getQualifiedName().toString());
					classDescriptors.add(descriptor);
				}
			}
		}
		return classDescriptors;
	}

	/**
	 * Irá gerar um único descritor de classe e todos os terminais para essa classe.
	 *
	 * Se alguma classe contém a tag especial javadoc {@link br.com.anteros.restdoc.maven.plugin.util.TagUtils.IGNORE_TAG} it will be excluded.
	 * @param classDoc
	 * @return
	 */
	protected ClassDescriptor getClassDescriptor(TypeElement classDoc) {

		// Se a tag ignore estiver presente ou este tipo de classe deve ser ignorado, simplesmente ignore esta classe
		if (!isEmpty(getTags(classDoc, IGNORE_TAG, treeUtils)) || shouldIgnoreClass(classDoc)) {
			return null;
		}
		String contextPath = getContextPath(classDoc);
		Collection<Endpoint> endpoints = getAllEndpoints(contextPath, classDoc, getEndpointMapping(classDoc));

		// Se não houver terminais, não adianta fornecer documentação.
		if (isEmpty(endpoints)) {
			return null;
		}

		String name = getClassName(classDoc);
		String description = getClassDescription(classDoc);

		return new ClassDescriptor(
				(name == null ? "" : name),
				(contextPath == null ? "" : contextPath),
				endpoints,
				(description == null ? "" : description)
		);
	}

	/**
	 * Recupera todos os pontos finais fornecidos no documento de classe especificado.
	 * @param contextPath
	 * @param classDoc
	 * @param classMapping
	 * @return
	 */
	protected Collection<Endpoint> getAllEndpoints(String contextPath, TypeElement classDoc, EndpointMapping classMapping) {
		Collection<Endpoint> endpoints = new ArrayList<>();

		for (ExecutableElement method : getMethods(classDoc)) {
			endpoints.addAll(getEndpoint(contextPath, classMapping, method));
		}

		//Verifique as superclasses para métodos herdados
		TypeMirror superClass = classDoc.getSuperclass();
		if (superClass != null && !(superClass instanceof NoType)) {
			TypeElement te = asTypeElement(classDoc.getSuperclass());
			if (te != null) {
				endpoints.addAll(getAllEndpoints(contextPath, te, classMapping));
			}
		}
		return endpoints;
	}

	/**
	 * Recupera o ponto de extremidade para um único método.
	 *
	 * Se algum método contém a tag especial javadoc {@link br.com.anteros.restdoc.maven.plugin.util.TagUtils.IGNORE_TAG} será excluído.
	 * @param contextPath
	 * @param classMapping
	 * @param method
	 * @return
	 */
	protected Collection<Endpoint> getEndpoint(String contextPath, EndpointMapping classMapping, ExecutableElement method) {

		//Se a tag de ignorar estiver presente, simplesmente não retorne nada para este terminal.
		if (!isEmpty(getTags(method, IGNORE_TAG, treeUtils)) || shouldIgnoreMethod(method))
			return emptyList();

		Collection<Endpoint> endpoints = new ArrayList<>();
		EndpointMapping methodMapping = getEndpointMapping(method);

		Collection<String> paths = resolvePaths(contextPath, classMapping, methodMapping);
		Collection<String> httpMethods = resolveHttpMethods(classMapping, methodMapping);
		Collection<String> consumes = resolveConsumesInfo(classMapping, methodMapping);
		Collection<String> produces = resolvesProducesInfo(classMapping, methodMapping);
		Collection<PathVar> pathVars = generatePathVars(method);
		Collection<QueryParam> queryParams = generateQueryParams(method);
		RequestBody requestBody = generateRequestBody(method);
		String firstSentence = firstSentence(method, treeUtils);
		String body = fullBody(method, treeUtils);

		for (String httpMethod : httpMethods) {
			for (String path : paths) {
				Endpoint ep = new Endpoint(
						path,
						httpMethod,
						queryParams,
						pathVars,
						requestBody,
						consumes,
						produces,
						firstSentence,
						body,
						method.getReturnType());
				endpoints.add(ep);
			}
		}

		return endpoints;
	}

	/**
	 * Obterá o caminho de contexto inicial a ser usado para todos os terminais restantes.
	 *
	 * Isso procura o valor em uma tag especial do javadoc {@link br.com.anteros.restdoc.maven.plugin.util.TagUtils.CONTEXT_TAG}
	 *
	 * @param classDoc
	 * @return
	 */
	protected String getContextPath(TypeElement classDoc) {
		List<String> tags = getTags(classDoc, CONTEXT_TAG, treeUtils);
		if(!isEmpty(tags)) {
			return tags.get(0);
		}
		return "";
	}

	/**
	 * Receberá o nome de exibição da classe.
	 *
	 * Isso procura o valor em uma tag especial do javadoc {@link br.com.anteros.restdoc.maven.plugin.util.TagUtils.NAME_TAG}
	 *
	 * @param classDoc
	 * @return
	 */
	protected String getClassName(TypeElement classDoc) {
		List<String> tags = getTags(classDoc, NAME_TAG, treeUtils);
		if(!isEmpty(tags)) {
			return tags.get(0);
		}
		return classDoc.getQualifiedName().toString();
	}

	/**
	 * Receberá a descrição da classe.
	 * @param classDoc
	 * @return
	 */
	protected String getClassDescription(TypeElement classDoc) {
		return fullBody(classDoc, treeUtils);
	}

	/**
	 * Gerará todos os caminhos especificados nos mapeamentos de classe e método.
	 * Cada caminho deve começar com o caminho do contexto, seguido por um dos caminhos da classe,
	 * e, finalmente, o caminho do método.
	 *
	 * @param contextPath
	 * @param classMapping
	 * @param methodMapping
	 * @return
	 */
	protected Collection<String> resolvePaths(String contextPath, EndpointMapping classMapping, EndpointMapping methodMapping) {

		contextPath = (contextPath == null ? "" : contextPath);

		// Constrói todos os caminhos com base no nível da classe, mais as extensões do método.
		LinkedHashSet<String> paths = new LinkedHashSet<>();

		if (isEmpty(classMapping.getPaths())) {
			for (String path : methodMapping.getPaths()) {
				paths.add(fixPath(contextPath + path));
			}
		} else if (isEmpty(methodMapping.getPaths())) {
			for (String path : classMapping.getPaths()) {
				paths.add(fixPath(contextPath + path));
			}
		} else {
			for (String defaultPath : classMapping.getPaths()) {
				for (String path : methodMapping.getPaths()) {
					paths.add(fixPath(contextPath + defaultPath + path));
				}
			}
		}
		return paths;
	}

	/**
	 * Usará as informações mapeadas do método se não estiverem vazias, caso contrário, usará as informações de mapeamento da classe
	 * para recuperar todos os métodos https.
	 * @param classMapping
	 * @param methodMapping
	 * @return
	 */
	protected Collection<String> resolveHttpMethods(EndpointMapping classMapping, EndpointMapping methodMapping) {
		return firstNonEmpty(
				methodMapping.getHttpMethods(),
				classMapping.getHttpMethods()
		);
	}

	/**
	 * Usará as informações mapeadas do método se não estiverem vazias, caso contrário, usará as informações de mapeamento da classe
	 * para recuperar todas as informações consumíveis.
	 * @param classMapping
	 * @param methodMapping
	 * @return
	 */
	protected Collection<String> resolveConsumesInfo(EndpointMapping classMapping, EndpointMapping methodMapping) {
		return firstNonEmpty(
				methodMapping.getConsumes(),
				classMapping.getConsumes()
		);
	}

	/**
	 * Usará as informações mapeadas do método se não estiverem vazias, caso contrário, usará as informações de mapeamento da classe
	 * para recuperar todas as informações que podem ser produzidas.
	 * @param classMapping
	 * @param methodMapping
	 * @return
	 */
	protected Collection<String> resolvesProducesInfo(EndpointMapping classMapping, EndpointMapping methodMapping) {
		return firstNonEmpty(
				methodMapping.getProduces(),
				classMapping.getProduces()
		);
	}
}