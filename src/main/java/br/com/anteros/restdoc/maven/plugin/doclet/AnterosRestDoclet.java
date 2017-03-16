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
package br.com.anteros.restdoc.maven.plugin.doclet;

import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.ANTEROS_JSON;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.SOURCEPATH_OPTION;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.internal.toolkit.util.DocletAbortException;

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
		String jsonTempFile = "";
		String[][] options = root.options();
		StringBuilder sourcePath = new StringBuilder();

		/**
		 * Procura no sourcepath qual o local para salvar o arquivo com o json
		 * do javadoc. Geralmente vai ser o utlimo path.
		 */
		for (String[] option : options) {
			if (option[0].equals(SOURCEPATH_OPTION)) {
				sourcePath.append(option[1]);
				String[] split = option[1].split(File.pathSeparator);
				jsonTempFile = split[split.length - 1];
				String tmp = ":" + jsonTempFile;
				sourcePath.delete(option[1].indexOf(tmp), option[1].indexOf(tmp) + tmp.length());
				break;
			}
		}

		/**
		 * Verifica e cria o arquivo json
		 */
		File directory = new File(jsonTempFile);
		directory.mkdirs();

		jsonTempFile = jsonTempFile + File.separator + ANTEROS_JSON;

		/**
		 * Busca todos as classes do tipo controller para serem adicionados ao
		 * arquivo json
		 */
		List<ClassDescriptor> classDescriptors = new ArrayList<ClassDescriptor>();

		for (Collector collector : collectors)
			classDescriptors.addAll(collector.getDescriptors(root));

		if (classDescriptors.isEmpty())
			throw new DocletAbortException("Não foram encontradas classes contendo documentação no path " + sourcePath);

		/**
		 * Ordena as classes pelo nome
		 */
		Collections.sort(classDescriptors, new Comparator<ClassDescriptor>() {

			@Override
			public int compare(ClassDescriptor o1, ClassDescriptor o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		/**
		 * Gera o arquivo json e escreve os dados no mesmo
		 */
		ObjectMapper mapper = new ObjectMapper();
		try {
			File file = new File(jsonTempFile);
			FileOutputStream fos = new FileOutputStream(file);
			mapper.writeValue(fos, classDescriptors.toArray(new ClassDescriptor[] {}));
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
