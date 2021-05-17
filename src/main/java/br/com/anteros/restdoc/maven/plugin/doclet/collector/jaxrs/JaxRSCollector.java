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
package br.com.anteros.restdoc.maven.plugin.doclet.collector.jaxrs;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.util.DocTrees;
import br.com.anteros.restdoc.maven.plugin.doclet.collector.AbstractCollector;
import br.com.anteros.restdoc.maven.plugin.doclet.collector.EndpointMapping;
import br.com.anteros.restdoc.maven.plugin.doclet.model.PathVar;
import br.com.anteros.restdoc.maven.plugin.doclet.model.QueryParam;
import br.com.anteros.restdoc.maven.plugin.doclet.model.RequestBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import static br.com.anteros.restdoc.maven.plugin.util.AnnotationUtils.getAnnotationName;
import static br.com.anteros.restdoc.maven.plugin.util.AnnotationUtils.getElementValue;
import static br.com.anteros.restdoc.maven.plugin.util.TagUtils.*;
import static br.com.anteros.restdoc.maven.plugin.util.CommonUtils.*;

public class JaxRSCollector extends AbstractCollector {

	protected static final String ANNOTATION_PACKAGE = "javax.ws.rs.";

	protected static final String PATH_ANNOTATION = ANNOTATION_PACKAGE + "Path";

	protected static final String GET_ANNOTATION = ANNOTATION_PACKAGE + "GET";
	protected static final String POST_ANNOTATION = ANNOTATION_PACKAGE + "POST";
	protected static final String PUT_ANNOTATION = ANNOTATION_PACKAGE + "PUT";
	protected static final String DELETE_ANNOTATION = ANNOTATION_PACKAGE + "DELETE";
	protected static final String HEAD_ANNOTATION = ANNOTATION_PACKAGE + "HEAD";

	protected static final String CONSUMES_ANNOTATION = ANNOTATION_PACKAGE + "Consumes";
	protected static final String PRODUCES_ANNOTATION = ANNOTATION_PACKAGE + "Produces";

	protected static final String PATHVAR_ANNOTATION = ANNOTATION_PACKAGE + "PathParam";
	protected static final String PARAM_ANNOTATION = ANNOTATION_PACKAGE + "QueryParam";

	public JaxRSCollector(DocTrees treeUtils) {
		super(treeUtils);
	}

	@Override
	protected boolean shouldIgnoreClass(TypeElement classDoc) {

		// Procure por quaisquer anotações JAXRS na classe ou nos métodos. Se for encontrado, não ignore esta classe.
		for (AnnotationMirror classAnnotation : classDoc.getAnnotationMirrors()) {
			String annotationName = getAnnotationName(classAnnotation);
			if (annotationName != null && annotationName.startsWith(ANNOTATION_PACKAGE)) {
				return false;
			}
		}

		for (ExecutableElement methodDoc : getMethods(classDoc)) {
			if (!shouldIgnoreMethod(methodDoc)) {
				return false;
			}
		}

		return true;
	}

	@Override
	protected boolean shouldIgnoreMethod(ExecutableElement methodDoc) {

		// Os métodos Jax RS precisam de uma anotação de método para serem usados, portanto, basta procurá-los.
		for (AnnotationMirror methodAnnotation : methodDoc.getAnnotationMirrors()) {
			String annotationName = getAnnotationName(methodAnnotation);
			if (GET_ANNOTATION.equals(annotationName) ||
					POST_ANNOTATION.equals(annotationName) ||
					PUT_ANNOTATION.equals(annotationName) ||
					DELETE_ANNOTATION.equals(annotationName) ||
					HEAD_ANNOTATION.equals(annotationName)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected EndpointMapping getEndpointMapping(Element doc) {
		Collection<String> paths = new LinkedHashSet<>();
		Collection<String> httpMethods = new LinkedHashSet<>();
		Collection<String> consumes = new LinkedHashSet<>();
		Collection<String> produces = new LinkedHashSet<>();

		// Procure uma anotação de mapeamento de solicitação
		for (AnnotationMirror annotation : doc.getAnnotationMirrors()) {

			String annotationName = getAnnotationName(annotation);

			if (GET_ANNOTATION.equals(annotationName) ||
					POST_ANNOTATION.equals(annotationName) ||
					PUT_ANNOTATION.equals(annotationName) ||
					DELETE_ANNOTATION.equals(annotationName) ||
					HEAD_ANNOTATION.equals(annotationName)) {

				httpMethods.add(annotationName.replace(ANNOTATION_PACKAGE, ""));

			} else if (PATH_ANNOTATION.equals(annotationName)) {
				paths.addAll(getElementValue(annotation, "value"));
			} else if (CONSUMES_ANNOTATION.equals(annotationName)) {
				consumes.addAll(getElementValue(annotation, "value"));
			} else if (PRODUCES_ANNOTATION.equals(annotationName)) {
				produces.addAll(getElementValue(annotation, "value"));
			}
		}

		return new EndpointMapping(
				paths,
				httpMethods,
				consumes,
				produces
		);
	}

	@Override
	protected Collection<PathVar> generatePathVars(ExecutableElement methodDoc) {
		Collection<PathVar> retVal = new ArrayList<>();

		List<String> tags = getTags(methodDoc, PATHVAR_TAG, treeUtils);
		Map<String, List<String>> paramTags = getParams(methodDoc, treeUtils);

		for (VariableElement parameter : methodDoc.getParameters()) {
			for (AnnotationMirror annotation : parameter.getAnnotationMirrors()) {
				if (getAnnotationName(annotation).equals(PATHVAR_ANNOTATION)) {
					String name = parameter.getSimpleName().toString();
					List<String> values = getElementValue(annotation, "value");
					if (!values.isEmpty())
						name = values.get(0);

					// primeiro verifique a tag especial, depois verifique a tag de parâmetro regular, finalmente o padrão é a string vazia
					String text = findParamText(tags, name);
					if (text == null) {
						String paramName = parameter.getSimpleName().toString();
						if (paramTags.containsKey(paramName) && !paramTags.get(paramName).isEmpty()) {
							text = paramTags.get(paramName).get(0);
						}
					}
					if (text == null) {
						text = "";
					}

					retVal.add(new PathVar(name, text, parameter.asType()));
				}
			}
		}

		return retVal;
	}

	@Override
	protected Collection<QueryParam> generateQueryParams(ExecutableElement methodDoc) {
		Collection<QueryParam> retVal = new ArrayList<> ();

		List<String> tags = getTags(methodDoc, QUERYPARAM_TAG, treeUtils);
		Map<String, List<String>> paramTags = getParams(methodDoc, treeUtils);

		for (VariableElement parameter : methodDoc.getParameters()) {
			for (AnnotationMirror annotation : parameter.getAnnotationMirrors()) {
				if (getAnnotationName(annotation).equals(PARAM_ANNOTATION)) {
					String name = parameter.getSimpleName().toString();
					List<String> values = getElementValue(annotation, "value");
					if (!values.isEmpty())
						name = values.get(0);

					// primeiro verifique a tag especial, depois verifique a tag de parâmetro regular, finalmente o padrão é a string vazia
					String text = findParamText(tags, name);
					if (text == null) {
						String paramName = parameter.getSimpleName().toString();
						if (paramTags.containsKey(paramName) && !paramTags.get(paramName).isEmpty()) {
							text = paramTags.get(paramName).get(0);
						}
					}
					if (text == null) {
						text = "";
					}

					retVal.add(new QueryParam(name, false, text, parameter.asType()));
				}
			}
		}
		return retVal;
	}

	@Override
	protected RequestBody generateRequestBody(ExecutableElement methodDoc) {
		List<String> tags = getTags(methodDoc, REQUESTBODY_TAG, treeUtils);
		Map<String, List<String>> paramTags = getParams(methodDoc, treeUtils);

		for (VariableElement parameter : methodDoc.getParameters()) {

			//TODO, precisa verificar essa lógica um pouco mais.
			//ignore qualquer coisa nas anotações e que comece com javax. Então é só aceitar o primeiro.
			if (isEmpty(parameter.getAnnotationMirrors()) && !parameter.asType().toString().startsWith("javax.")) {
				//primeiro verifique a tag especial, a seguir verifique a tag de parâmetro regular e, por fim, o padrão é a string vazia
				String text = (isEmpty(tags) ? null : tags.get(0));
				if (text == null) {
					String paramName = parameter.getSimpleName().toString();
					if (paramTags.containsKey(paramName) && !paramTags.get(paramName).isEmpty()) {
						text = paramTags.get(paramName).get(0);
					}
				}
				if (text == null) {
					text = "";
				}

				return new RequestBody(parameter.getSimpleName().toString(), text, parameter.asType());
			}
		}
		return null;
	}

	@Override
	protected Collection<String> resolveHttpMethods(EndpointMapping classMapping, EndpointMapping methodMapping) {
		//Apenas os métodos devem ter métodos http.
		return methodMapping.getHttpMethods();
	}
}
