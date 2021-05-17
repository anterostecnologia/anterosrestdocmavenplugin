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
package br.com.anteros.restdoc.maven.plugin.doclet.writer.swagger;


import java.util.Collection;
import java.util.Date;
import java.util.Set;

import static java.util.Collections.emptyList;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import static br.com.anteros.restdoc.maven.plugin.util.CommonUtils.*;

class TypeUtils {

    /**
     * Will return a full data type for Swagger.
     * @param type
     * @return
     */
    public static String dataType(TypeMirror type) {
        if (type == null)
            return null;

        if (isContainer(type)) {
            //treat sets as sets
            if (isType(asTypeElement(type), Set.class)) {
                return "Set[" + internalContainerType(type) + "]";
            }
            return "List[" + internalContainerType(type) + "]";
        }

        //Treat as a basic type.
        return basicType(type);
    }

    /**
     * Checks if the type is an iterable or an array
     * @param type
     * @return
     */
    public static boolean isContainer(TypeMirror type) {

        //first check for arrays
        if (type.getKind().equals(TypeKind.ARRAY)) {
            return true;
        }

        if ((type instanceof NoType) || type.getKind().isPrimitive()) {
            return false;
        }

        //treat iterables as lists
        if (isType(asTypeElement(type), Iterable.class)) {
            return true;
        }
        return false;
    }

    /**
     * This will grab the internal type from an array or a parameterized container.
     * @param type
     * @return
     */
    public static String internalContainerType(TypeMirror type) {
        //treat arrays first
        if (type.getKind().equals(TypeKind.ARRAY)) {
            return basicType(type);
        }

        DeclaredType pType = (DeclaredType)type;
        List<? extends TypeMirror> paramTypes = pType.getTypeArguments();
        if (!isEmpty(paramTypes)) {
            return basicType(paramTypes.get(0));
        }

        //TODO look into supporting models.
        return "Object";
    }

    /**
     * Returns the basic type.  If not one of the supported swagger basic types then it is treated as an Object.
     * @param type
     * @return
     */
    public static String basicType(TypeMirror type) {
        if (type == null)
            return "void";

        //next primitives
        if (type.getKind().isPrimitive()) {
            return type.toString();
        }

        String name = type.toString();

        //Check the java.lang classes
        if (name.equals(String.class.getName()))
            return "string";

        if (name.equals(Boolean.class.getName()))
            return "boolean";

        if (name.equals(Integer.class.getName()))
            return "int";

        if (name.equals(Long.class.getName()))
            return "long";

        if (name.equals(Float.class.getName()))
            return "float";

        if (name.equals(Double.class.getName()))
            return "double";

        if (name.equals(Byte.class.getName()))
            return "byte";

        if (name.equals(Date.class.getName()))
            return "Date";

        //Process enums as strings.
        if (!(type instanceof NoType) && asTypeElement(type).getKind().equals(ElementKind.ENUM_CONSTANT)) {
            return "string";
        }

        //TODO look into supporting models.
        return "object";
    }

    /**
     * This will retrieve all known allowable values from an enum.
     * @param type
     * @return
     */
    public static Collection<String> allowableValues(TypeMirror type) {
        TypeElement te = asTypeElement(type);
        if (type == null || te == null) {
            return emptyList();
        }

        if (te.getKind().equals(ElementKind.ENUM_CONSTANT)) {
            return getEnumValues(te);
        }
        return emptyList();
    }

    public static List<String> getEnumValues(TypeElement enumTypeElement) {
    return enumTypeElement.getEnclosedElements().stream()
            .filter(element -> element.getKind().equals(ElementKind.ENUM_CONSTANT))
            .map(Object::toString)
            .collect(Collectors.toList());
    }

    /**
     * Checks the class doc to see if it is a type or subtype of the provided class or object.
     * @param classDoc
     * @param targetClazz
     * @param <T>
     * @return
     */
    private static <T> boolean isType(TypeElement classDoc, Class<T> targetClazz) {
        if (classDoc == null) {
            return false;
        }

        if (classDoc.getQualifiedName().toString().equals(targetClazz.getName())) {
            return true;
        }

        TypeMirror superClass = classDoc.getSuperclass();
        if (superClass != null && !(superClass instanceof NoType)) {
            if (isType(asTypeElement(superClass), targetClazz)) {
                return true;
            }
        }

        for (TypeMirror iface : classDoc.getInterfaces()) {
            if (isType(asTypeElement(iface), targetClazz)) {
                return true;
            }
        }

        return false;
    }

}
