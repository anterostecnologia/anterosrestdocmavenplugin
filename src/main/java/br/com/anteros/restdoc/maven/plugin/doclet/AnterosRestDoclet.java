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

import br.com.anteros.restdoc.maven.plugin.Configuration;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import br.com.anteros.restdoc.maven.plugin.doclet.collector.Collector;
import br.com.anteros.restdoc.maven.plugin.doclet.collector.jaxrs.JaxRSCollector;
import br.com.anteros.restdoc.maven.plugin.doclet.collector.spring.SpringCollector;
import br.com.anteros.restdoc.maven.plugin.doclet.model.ClassDescriptor;
import br.com.anteros.restdoc.maven.plugin.doclet.writer.Writer;
import br.com.anteros.restdoc.maven.plugin.doclet.writer.simple.SimpleHtmlWriter;
import br.com.anteros.restdoc.maven.plugin.doclet.writer.swagger.SwaggerWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;

import jdk.javadoc.doclet.Reporter;

import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.*;
import static br.com.anteros.restdoc.maven.plugin.Configuration.ConfigOption.API_VERSION;
import static br.com.anteros.restdoc.maven.plugin.Configuration.ConfigOption.BASEPATH;
import static br.com.anteros.restdoc.maven.plugin.Configuration.ConfigOption.DISPLAY_ONLY;
import static br.com.anteros.restdoc.maven.plugin.Configuration.ConfigOption.OUTPUT_FORMAT;
import static br.com.anteros.restdoc.maven.plugin.Configuration.ConfigOption.STYLESHEET;
import static br.com.anteros.restdoc.maven.plugin.Configuration.ConfigOption.TITLE;

import static br.com.anteros.restdoc.maven.plugin.Configuration.getOptionLength;

/**
 * @author edsonmartins
 */
public class AnterosRestDoclet implements Doclet, Comparator<ClassDescriptor> {

	@Override
	public void init(Locale locale, Reporter reporter) {
		// nada
	}

	@Override
	public String getName() {
		return "AnterosRestDoclet";
	}

	private Map<String, String> options = new HashMap<>();

	@Override
	public Set<? extends Option> getSupportedOptions() {
		Set<ConfigOption> options = new HashSet<>();
		options.add(new ConfigOption(OUTPUT_FORMAT));
		// Opções de legado
		options.add(new ConfigOption(TITLE));
		options.add(new ConfigOption(STYLESHEET));
		//Opções do Swagger
		options.add(new ConfigOption(API_VERSION));
		options.add(new ConfigOption(DISPLAY_ONLY));
		options.add(new ConfigOption(BASEPATH) );
		return options;
	};

	private VariableElement getFieldWithName(DocletEnvironment env, TypeElement classDoc, String fieldName) {
		String fullyQualifiedName = "";
		for (VariableElement e : ElementFilter.fieldsIn(env.getElementUtils().getAllMembers(classDoc))) {
			if (e.getSimpleName().toString().equals(fieldName)) {
				DeclaredType declaredType = (DeclaredType) e.asType();
				return e;
			}
		}
		return null;
	}


	/**
	 * Gerar a documentação aqui. Este método é obrigatório para todos os doclets.
	 *
	 * @return true on success.
	 */
	@Override
	public boolean run(DocletEnvironment root) {

		Configuration config = new Configuration(options);

		List<ClassDescriptor> classDescriptors = new ArrayList<>();

		final Collection<Collector> collectors = Arrays.<Collector>asList(
				new SpringCollector(root.getDocTrees()),
				new JaxRSCollector(root.getDocTrees())
		);

		for (Collector collector : collectors) {
			classDescriptors.addAll(collector.getDescriptors(root));
		}

		Writer writer;
		if (config.getOutputFormat().equals(SwaggerWriter.OUTPUT_OPTION_NAME)) {
			writer = new SwaggerWriter();
		} else {
			writer = new SimpleHtmlWriter();
		}

		try {
			writer.write(classDescriptors, config);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		/**
		 * Verifica e cria o arquivo json
		 */
		String defaultBaseDir = System.getProperty("java.io.tmpdir");
		String temporaryDirectoryJson = defaultBaseDir + File.separator + ANTEROS_JSONC_JAVADOC;
		File directory = new File(temporaryDirectoryJson);
		directory.mkdirs();

		temporaryDirectoryJson = temporaryDirectoryJson + File.separator+ ANTEROS_JSON;

		if (classDescriptors.isEmpty())
			throw new DocletAbortException("Não foram encontradas classes contendo documentação no sourcepath.");

		/**
		 * Ordena as classes pelo nome
		 */
		Collections.sort(classDescriptors, this);

		/**
		 * Gera o arquivo json e escreve os dados no mesmo
		 */
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
//		mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector(){
//			@Override
//			public boolean hasIgnoreMarker(AnnotatedMember m) {
//				System.out.println(m.getRawType().getName());
//				return m.getRawType().getName().contains("java") || super.hasIgnoreMarker(m);
//			}
//		});
		try {
			File file = new File(temporaryDirectoryJson);
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

	/**
	 * Necessário para validar as opções da linha de comando.
	 *
	 * @param option nome da opção
	 * @return tamanho da opção
	 */
	public static int optionLength(String option) {
		return getOptionLength(option);
	}

	/**
	 * @return versão do idioma (codificado para SourceVersion.RELEASE_11)
	 */
	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.RELEASE_11;
	}

	@Override
	public int compare(ClassDescriptor o1, ClassDescriptor o2) {
		return o1.getName().compareTo(o2.getName());
	}

	public class ConfigOption implements Doclet.Option {

		private final String name;
		private final boolean hasArg;
		private final String description;
		private final String parameters;

		public ConfigOption(Configuration.ConfigOption co) {
			this.name = co.getOption();
			this.hasArg = true;
			this.description = co.getDescription();
			this.parameters = co.getDefaultValue();
		}

		@Override
		public int getArgumentCount() {
			return 1;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public Option.Kind getKind() {
			return Option.Kind.STANDARD;
		}

		@Override
		public List<String> getNames() {
			return List.of(name);
		}

		@Override
		public String getParameters() {
			return hasArg ? parameters : "";
		}

		@Override
		public boolean process(String option, List<String> arguments) {
			options.put(option, arguments.get(0));
			return true;
		}
	}
}