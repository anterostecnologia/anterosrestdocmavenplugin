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
package br.com.anteros.restdoc.maven.plugin.doclet.collector.spring;


import br.com.anteros.restdoc.maven.plugin.doclet.collector.AbstractCollector;
import br.com.anteros.restdoc.maven.plugin.doclet.collector.EndpointMapping;
import br.com.anteros.restdoc.maven.plugin.doclet.model.PathVar;
import br.com.anteros.restdoc.maven.plugin.doclet.model.QueryParam;
import br.com.anteros.restdoc.maven.plugin.doclet.model.RequestBody;
import com.sun.source.util.DocTrees;

import javax.lang.model.element.*;
import java.util.*;

import static br.com.anteros.restdoc.maven.plugin.util.AnnotationUtils.getAnnotationName;
import static br.com.anteros.restdoc.maven.plugin.util.AnnotationUtils.getElementValue;
import static br.com.anteros.restdoc.maven.plugin.util.CommonUtils.firstNonEmpty;
import static br.com.anteros.restdoc.maven.plugin.util.CommonUtils.isEmpty;
import static br.com.anteros.restdoc.maven.plugin.util.TagUtils.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;

public class SpringCollector extends AbstractCollector {

    protected static final List<String> CONTROLLER_ANNOTATION = Arrays.asList("org.springframework.stereotype.Controller",
            "org.springframework.web.bind.annotation.RestController");
    protected static final String MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.RequestMapping";
    protected static final String PATHVAR_ANNOTATION = "org.springframework.web.bind.annotation.PathVariable";
    protected static final String PARAM_ANNOTATION = "org.springframework.web.bind.annotation.RequestParam";
    protected static final String REQUESTBODY_ANNOTATION = "org.springframework.web.bind.annotation.RequestBody";

    public SpringCollector(DocTrees treeUtils) {
        super(treeUtils);
    }

    @Override
    protected boolean shouldIgnoreClass(TypeElement classDoc) {
        //Se for encontrada uma anotação de controlador, não ignore esta classe.
        for (AnnotationMirror classAnnotation : classDoc.getAnnotationMirrors()) {
            if (CONTROLLER_ANNOTATION.contains(getAnnotationName(classAnnotation))) {
                return false;
            }
        }
        //Se não for encontrado, ignore esta classe.
        return true;
    }

    @Override
    protected boolean shouldIgnoreMethod(ExecutableElement methodDoc) {
        //Se for encontrada uma anotação de mapeamento, não ignore esta classe.
        for (AnnotationMirror classAnnotation : methodDoc.getAnnotationMirrors())
            if (MAPPING_ANNOTATION.equals(getAnnotationName(classAnnotation)))
                return false;

        //Se não for encontrado, ignore esta classe.
        return true;
    }

    @Override
    protected EndpointMapping getEndpointMapping(Element doc) {
        //Procure uma anotação de mapeamento de solicitação
        for (AnnotationMirror annotation : doc.getAnnotationMirrors()) {
            //Se encontrado, extraia o valor (caminhos) e os métodos.
            if (MAPPING_ANNOTATION.equals(getAnnotationName(annotation))) {

                //Obtenha métodos http de anotação
                Collection<String> httpMethods = new LinkedHashSet<>();
                for (String value : getElementValue(annotation, "method")) {
                    httpMethods.add(value.substring(value.lastIndexOf(".") + 1));
                }

                return new EndpointMapping(
                        new LinkedHashSet<>(getElementValue(annotation, "value")),
                        httpMethods,
                        new LinkedHashSet<>(getElementValue(annotation, "consumes")),
                        new LinkedHashSet<>(getElementValue(annotation, "produces"))
                );
            }
        }

        //Simplesmente retorne um agrupamento vazio se nenhum mapeamento de solicitação foi encontrado.
        return new EndpointMapping(
                Collections.<String>emptySet(),
                Collections.<String>emptySet(),
                Collections.<String>emptySet(),
                Collections.<String>emptySet()
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
                    Collection<String> values = getElementValue(annotation, "value");
                    if (!values.isEmpty()) {
                        name = values.iterator().next();
                    }

                    //primeiro verifique a tag especial, a seguir verifique a tag de parâmetro regular e, por fim, o padrão é a string vazia
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
        Collection<QueryParam> retVal = new ArrayList<>();

        List<String> tags = getTags(methodDoc, QUERYPARAM_TAG, treeUtils);
        Map<String, List<String>> paramTags = getParams(methodDoc, treeUtils);

        for (VariableElement parameter : methodDoc.getParameters()) {
            for (AnnotationMirror annotation : parameter.getAnnotationMirrors()) {
                if (getAnnotationName(annotation).equals(PARAM_ANNOTATION)) {
                    String name = parameter.getSimpleName().toString();
                    List<String> values = getElementValue(annotation, "value");
                    if (!values.isEmpty())
                        name = values.get(0);

                    List<String> requiredVals = getElementValue(annotation, "required");

                    //Com a consulta de primavera, os parâmetros são necessários por padrão
                    boolean required = TRUE;
                    if (!requiredVals.isEmpty()) {
                        required = Boolean.parseBoolean(requiredVals.get(0));
                    }

                    //Com spring, se defaultValue for fornecido, "required" é definido como false automaticamente
                    List<String> defaultVals = getElementValue(annotation, "defaultValue");

                    if (!defaultVals.isEmpty()) {
                        required = FALSE;
                    }

                    //primeiro verifique a tag especial, a seguir verifique a tag de parâmetro regular e, por fim, o padrão é a string vazia
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

                    retVal.add(new QueryParam(name, required, text, parameter.asType()));
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
            for (AnnotationMirror annotation : parameter.getAnnotationMirrors()) {
                if (getAnnotationName(annotation).equals(REQUESTBODY_ANNOTATION)) {

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
        }
        return null;
    }

    @Override
    protected Collection<String> resolveHttpMethods(EndpointMapping classMapping, EndpointMapping methodMapping) {
        //Se não houver métodos http definidos, basta usar GET
        return firstNonEmpty(super.resolveHttpMethods(classMapping, methodMapping), asList("GET"));
    }
}