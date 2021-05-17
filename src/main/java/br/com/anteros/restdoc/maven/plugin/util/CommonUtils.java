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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import static java.util.Collections.emptySet;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * Utilitários simples para reduzir o número de dependências necessárias para o projeto
 *
 * @author edsonmartins
 */
public class CommonUtils {

    public static <T> boolean isEmpty(T[] items) {
        return items == null || items.length == 0;
    }

    public static <T> boolean isEmpty(Collection<T> items) {
        return items == null || items.isEmpty();
    }

    public static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public static void close(Closeable... closeables) throws IOException{
        if (isEmpty(closeables))
            return;

        Exception first = null;
        for (Closeable closeable : closeables) {
            if (closeable == null)
                continue;

            try {
                closeable.close();
            } catch (Exception e) {
                first = (first == null ? e : first);
            }
        }

        if (first != null)
            throw (first instanceof IOException ? (IOException)first : new IOException(first));

    }

    public static <T> Collection<T> firstNonEmpty(Collection<T>... collections) {
        for (Collection<T> collection : collections)
            if (!isEmpty(collection))
                return collection;

        return emptySet();
    }

    public static void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024 * 4];
        int len;

        while ((len = input.read(buffer)) > 0 ) {
            output.write(buffer, 0, len);
        }
    }

    public static String fixPath(String path) {
        if (isEmpty(path)) {
            return "/";
        }

        //remove duplicates path seperators
        int len = 0;
        while (path.length() != len) {
            len = path.length();
            path = path.replaceAll("//", "/");
        }
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 2);
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }

    public static List<ExecutableElement> getMethods(TypeElement classDoc) {
        final List<ExecutableElement> results = new ArrayList<>();
        for (Element ee : classDoc.getEnclosedElements()) {
            if (ee instanceof ExecutableElement) {
                results.add((ExecutableElement) ee);
            }
        }
        return results;
    }

    public static TypeElement asTypeElement(TypeMirror type) {
        if (type instanceof DeclaredType) {
            return (TypeElement)((DeclaredType)type).asElement();
        }
        return null;
    }
}
