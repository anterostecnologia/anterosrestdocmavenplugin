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
package br.com.anteros.restdoc.maven.plugin.util;


import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import java.util.Map.Entry;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;

/**
 * @author edsonmartins
 */
public class AnnotationUtils {

    public static String getAnnotationName(AnnotationMirror annotation) {
        try {
            return annotation.getAnnotationType().toString();
        } catch (ClassCastException e) {
            return null;
        }
    }

    public static List<String> getElementValue(AnnotationMirror annotation, String key) {
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> element : annotation.getElementValues().entrySet()) {
            if (element.getKey().getSimpleName().toString().equals(key)) {
                return resolveAnnotationValue(element.getValue());
            }
        }
        return emptyList();
    }

    private static List<String> resolveAnnotationValue(AnnotationValue value) {
        List<String> retVal = new ArrayList<>();
        /**
         * TODO usar recursão aqui provavelmente é falho.
         */
        if (value.getValue() instanceof AnnotationValue[]) {
            for (AnnotationValue annotationValue : (AnnotationValue[])value.getValue()) {
                retVal.addAll(resolveAnnotationValue(annotationValue));
            }
        } else if (value.getValue() instanceof List) {
            for (AnnotationValue annotationValue : (List<AnnotationValue>)value.getValue()) {
                retVal.addAll(resolveAnnotationValue(annotationValue));
            }
        } else {
            retVal.add(value.getValue().toString());

        }
        return retVal;
    }

}
