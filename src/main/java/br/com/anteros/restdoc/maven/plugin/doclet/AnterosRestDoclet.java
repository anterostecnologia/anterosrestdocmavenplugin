package br.com.anteros.restdoc.maven.plugin.doclet;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.RootDoc;

import br.com.anteros.restdoc.maven.plugin.doclet.collector.Collector;
import br.com.anteros.restdoc.maven.plugin.doclet.collector.jaxrs.JaxRSCollector;
import br.com.anteros.restdoc.maven.plugin.doclet.collector.spring.SpringCollector;
import br.com.anteros.restdoc.maven.plugin.doclet.model.ClassDescriptor;


/**
 * 
 * @author Edson Martins
 * @author Eduardo Albertini
 *
 */
public class AnterosRestDoclet extends Doclet {

	private static final Collection<Collector> collectors = Arrays.<Collector>asList(new SpringCollector(),
			new JaxRSCollector());

	/**
	 * Generate documentation here. This method is required for all doclets.
	 *
	 * @return true on success.
	 */
	public static boolean start(RootDoc root) {
		String tempFile = "";
		String[][] options = root.options();
		
		for (String[] option : options) {
			if (option[0].equals("-sourcepath")){
				String[] split = option[1].split(File.pathSeparator);
				tempFile = split[split.length-1];
				break;
			}
		}
		
		File directory = new File(tempFile);
		directory.mkdirs();
		
		tempFile=tempFile+File.separator+"anteros.json";
		

		Collection<ClassDescriptor> classDescriptors = new ArrayList<ClassDescriptor>();

		for (Collector collector : collectors)
			classDescriptors.addAll(collector.getDescriptors(root));
		
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			File file = new File(tempFile);
			FileOutputStream fos = new FileOutputStream(file);
			mapper.writeValue(fos,classDescriptors.toArray(new ClassDescriptor[]{}));
			fos.flush();
			fos.close();
			
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
		return true;
	}

}
