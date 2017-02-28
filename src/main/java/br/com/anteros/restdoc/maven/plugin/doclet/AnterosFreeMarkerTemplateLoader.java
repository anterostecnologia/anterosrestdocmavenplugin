package br.com.anteros.restdoc.maven.plugin.doclet;

import java.net.URL;

import br.com.anteros.core.utils.ResourceUtils;
import freemarker.cache.ClassTemplateLoader;


/**
 * 
 * @author Edson Martins
 * @author Eduardo Albertini
 *
 */
public class AnterosFreeMarkerTemplateLoader extends ClassTemplateLoader {

	private Class<?> cl;

	public AnterosFreeMarkerTemplateLoader(Class<?> clazz, String string) {
		super(clazz, string);
		this.cl = clazz;
	}

	@Override
	protected URL getURL(String name) {
		return ResourceUtils.getResource(name, cl);
	}

}
