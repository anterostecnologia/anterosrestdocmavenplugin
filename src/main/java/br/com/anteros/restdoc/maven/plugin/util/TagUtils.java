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

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.util.DocTrees;
import com.sun.source.util.SimpleDocTreeVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.lang.model.element.Element;
import javax.lang.model.util.ElementScanner9;

public class TagUtils {

    public static final String IGNORE_TAG = "ignore";
    public static final String CONTEXT_TAG = "contextPath";
    public static final String NAME_TAG = "name";
    public static final String PATHVAR_TAG = "pathVar";
    public static final String QUERYPARAM_TAG = "queryParam";
    public static final String REQUESTBODY_TAG = "requestBody";
    public static final String FIRST_SENTENCE_TAG = "firstSentence";
    public static final String FULL_BODY_TAG = "fullBody";

    public static String findParamText(List<String> tags, String name) {
        for (String tag : tags) {
            if (tag.trim().equals(name) || tag.trim().startsWith(name + " ")) {
                return tag.trim().substring(name.length()).trim();
            }
        }
        return null;
    }

    public static String fullBody(Element e, DocTrees treeUtils) {
        TagScanner scanner = new TagScanner(treeUtils);
        scanner.scan(e, 0);
        StringBuilder sb = new StringBuilder();
        if (scanner.common.containsKey(FULL_BODY_TAG)) {
            for (String s : scanner.common.get(FULL_BODY_TAG)) {
                sb.append(s);
            }
        }
        return sb.toString();
    }
    
    public static String firstSentence(Element e, DocTrees treeUtils) {
        TagScanner scanner = new TagScanner(treeUtils);
        scanner.scan(e, 0);
        StringBuilder sb = new StringBuilder();
        if (scanner.common.containsKey(FIRST_SENTENCE_TAG)) {
            for (String s : scanner.common.get(FIRST_SENTENCE_TAG)) {
                sb.append(s);
            }
        }
        return sb.toString();
    }

    public static List<String> getTags(Element e, String tagName, DocTrees treeUtils) {
        TagScanner scanner = new TagScanner(treeUtils);
        scanner.scan(e, 0);
        if (scanner.tags.containsKey(tagName)) {
            return scanner.tags.get(tagName);
        }
        return new ArrayList<>();
    }

    public static Map<String, List<String>> getParams(Element e, DocTrees treeUtils) {
        TagScanner scanner = new TagScanner(treeUtils);
        scanner.scan(e, 0);
        return scanner.params;
    }

    /**
     * Um scanner para procurar elementos com comentários de documentação,
     * e para examinar esses comentários para tags personalizadas.
     */
    private static class TagScanner extends ElementScanner9<Void, Integer> {
        private final DocTrees treeUtils;

        public final Map<String, List<String>> tags = new TreeMap<>();
        public final Map<String, List<String>> common = new TreeMap<>();
        public final Map<String, List<String>> params = new TreeMap<>();

        TagScanner(DocTrees treeUtils) {
            this.treeUtils = treeUtils;
        }

        void show(Set<? extends Element> elements) {
            scan(elements, 0);
        }

        @Override
        public Void scan(Element e, Integer depth) {
            DocCommentTree dcTree = treeUtils.getDocCommentTree(e);
            if (dcTree != null) {
                new TagVisitor(tags, common, params).visit(dcTree, null);
            }
            return super.scan(e, depth + 1);
        }
    }

    /**
     * Um visitante para reunir as tags de bloco encontradas em um comentário.
     */
    private static class TagVisitor extends SimpleDocTreeVisitor<Void, Void> {

        private final Map<String, List<String>> params;
        private final Map<String, List<String>> tags;
        private final Map<String, List<String>> common;

        TagVisitor(Map<String, List<String>> tags, Map<String, List<String>> common, Map<String, List<String>> params) {
            this.tags = tags;
            this.common = common;
            this.params = params;
        }

        /**
         * este é o bloco de comentários completo.
         */
        @Override
        public Void visitDocComment(DocCommentTree tree, Void p) {
            List<String> fs = toString(tree.getFirstSentence());
            common.put(FIRST_SENTENCE_TAG, fs);
            List<String> fb =  toString(tree.getFullBody());
            common.put(FULL_BODY_TAG, fb);
            return visit(tree.getBlockTags(), null);
        }


        /**
         * o @param block.
         */
        @Override
        public Void visitParam(ParamTree node, Void p) {
            List<String> content =  toString(node.getDescription());
            params.put(node.getName().toString(),content);
            return null;
        }

        /**
         * Etiqueta composta específica.
         */
        @Override
        public Void visitUnknownBlockTag(UnknownBlockTagTree tree, Void p) {
            String name = tree.getTagName();
            List<String> content = toString(tree.getContent());
            tags.put(name,content);
            return null;
        }

        private static List<String> toString(List<? extends DocTree> trees) {
            List<String> results = new ArrayList<>();
            for (DocTree t : trees) {
                // provavelmente há outro caso além do Linktree aqui que precisamos lidar
                if (t instanceof LinkTree) {
                    LinkTree lt = (LinkTree) t;
                    results.add(lt.getReference().toString());
                } else if (t != null) {
                    results.add(t.toString());
                }
            }
            return results;
        }
    }
}
