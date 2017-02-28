package br.com.anteros.restdoc.maven.plugin.doclet.collector;

import java.util.Collection;

import com.sun.javadoc.RootDoc;

import br.com.anteros.restdoc.maven.plugin.doclet.model.ClassDescriptor;


/**
 * 
 * @author Edson Martins
 * @author Eduardo Albertini
 *
 */
public interface Collector {

    Collection<ClassDescriptor> getDescriptors(RootDoc rootDoc);


}
