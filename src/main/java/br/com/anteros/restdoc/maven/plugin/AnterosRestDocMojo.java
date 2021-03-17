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
package br.com.anteros.restdoc.maven.plugin;

import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.ANTEROS_JSON;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.ANTEROS_JSONC_JAVADOC;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.CLASSPATH_OPTION;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.DOCLET_OPTION;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.GENERAL;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.INTEGRATION;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.ITEMS_ADOC;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.ITEMS_DATA_INTEGRATION_ADOC;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.ITEMS_MOBILE_ADOC;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.ITEMS_PERSISTENCE_ADOC;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.ITEMS_PERSISTENCE_SECURITY_ADOC;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.ITEMS_SECURITY_ADOC;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.MOBILE;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.PROTECTED_OPTION;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.SECURITY;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.SOURCEPATH_OPTION;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.SPRING_WEB_CONTROLLER;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TARGET;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_INTEGRATION_PERSISTENCE_TOPIC;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TEMPLATE_MOBILE_PERSISTENCE_TOPIC;
import static br.com.anteros.restdoc.maven.plugin.util.JavadocUtil.isNotEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.IncludesArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.plugins.javadoc.AbstractJavadocMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.shared.artifact.filter.PatternExcludesArtifactFilter;
import org.apache.maven.shared.artifact.filter.PatternIncludesArtifactFilter;
import org.asciidoctor.maven.AsciidoctorMojo;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.util.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.library.ClassLibraryBuilder;
import com.thoughtworks.qdox.library.SortedClassLibraryBuilder;
import com.thoughtworks.qdox.model.JavaClass;

import br.com.anteros.core.scanner.ClassFilter;
import br.com.anteros.core.scanner.ClassPathScanner;
import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.core.utils.ResourceUtils;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.persistence.metadata.annotation.Entity;
import br.com.anteros.remote.synch.annotation.RemoteSynchDataIntegration;
import br.com.anteros.remote.synch.annotation.RemoteSynchMobile;
import br.com.anteros.restdoc.maven.plugin.doclet.AnterosFreeMarkerTemplateLoader;
import br.com.anteros.restdoc.maven.plugin.doclet.AnterosRestDoclet;
import br.com.anteros.restdoc.maven.plugin.doclet.model.ClassDescriptor;
import br.com.anteros.restdoc.maven.plugin.util.ResourceResolver;
import br.com.anteros.restdoc.maven.plugin.util.SearchField;
import br.com.anteros.restdoc.maven.plugin.util.SourceResolverConfig;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

/**
 * 
 * @author Edson Martins
 *
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE, requiresDependencyCollection = ResolutionScope.COMPILE)
public class AnterosRestDocMojo extends AsciidoctorMojo {

	/** 
	 * Diretório de saída do arquivo gerado (.html, .pdf, etc.)
	 */
	@Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
	private File outputDirectory;

	/**
	 * Projeto maven
	 */
	@Parameter(defaultValue = "${project}")
	private MavenProject project = null;

	/**
	 * Nome do projeto
	 */
	@Parameter(defaultValue = "${project.name}", required = true)
	private String projectDisplayName;

	/**
	 * Diretórios com os códigos fonte
	 */
	@Parameter(defaultValue = "${project.compileSourceRoots}")
	private List<String> compileSourceRoots;

	/**
	 * Diretórios com os códigos fonte compilados
	 */
	@Parameter(defaultValue = "${project.compileClasspathElements}")
	private List<String> classpathElements;

	/**
	 * Nome dos pacotes para procurar os controllers
	 */
	@Parameter(required = true)
	private List<String> packageScanEndpoints = new ArrayList<String>();

	/**
	 * Nome dos pacotes para procurar as Entidades
	 */
	@Parameter(required = true)
	private List<String> packageScanEntities = new ArrayList<String>();

	/**
	 * Base para gerar as URL de execução dos exemplos
	 */
	@Parameter(required = true)
	private String urlBase;

	/**
	 * Verifica se deve incluir o código fonte das dependências
	 */
	@Parameter(defaultValue = "false")
	private boolean includeDependencySources;

	/**
	 * Diretório para fazer cache do código fonte
	 */
	@Parameter(defaultValue = "${project.build.directory}/distro-javadoc-sources")
	private File sourceDependencyCacheDir;

	/**
	 * Lista para incluir o código fonte das dependências. Exemplo:
	 * <code>org.apache.maven:*</code>
	 *
	 * @see #includeDependencySources
	 */
	@Parameter
	private List<String> dependencySourceIncludes;

	/**
	 * Lista para excluir o código fonte das dependências. Exemplo:
	 * <code>org.apache.maven.shared:*</code>
	 *
	 * @see #includeDependencySources
	 */
	@Parameter
	private List<String> dependencySourceExcludes;

	/**
	 * Repositório local
	 */
	@Parameter(property = "localRepository")
	private ArtifactRepository localRepository;

	/**
	 * Repositórios remoto
	 */
	@Parameter(property = "project.remoteArtifactRepositories")
	private List<ArtifactRepository> remoteRepositories;

	/**
	 * Verifica se deve incluir o código fonte das dependências transitivas
	 */
	@Parameter(defaultValue = "false")
	private boolean includeTransitiveDependencySources;

	/**
	 * Archiver manager
	 */
	@Component
	private ArchiverManager archiverManager;

	/**
	 * Factory for creating artifact objects
	 */
	@Component
	private ArtifactFactory factory;

	/**
	 * Used to resolve artifacts of aggregated modules
	 */
	@Component
	private ArtifactMetadataSource artifactMetadataSource;

	/**
	 * Used for resolving artifacts
	 */
	@Component
	private ArtifactResolver resolver;

	/**
	 * 
	 */
	@Parameter(property = "reactorProjects", readonly = true)
	private List<MavenProject> reactorProjects;

	private Map<String, String> anchors = new HashMap<>();
	private Map<String, String> anchorsMobile = new HashMap<>();
	private Map<String, String> anchorsIntegration = new HashMap<>();

	public void execute() throws MojoExecutionException, MojoFailureException {

		/**
		 * Se não informar o nome do documento .adoc principal copiamos o padrão para a
		 * pasta sourceDirectory.
		 */
		if (sourceDocumentName == null) {
			sourceDocumentName = "index.adoc";
			try {
				InputStream openStream = ResourceUtils.getResourceAsStream("template_index.adoc");
				if (!sourceDirectory.exists())
					sourceDirectory.mkdirs();

				FileOutputStream fos = new FileOutputStream(
						new File(sourceDirectory + File.separator + sourceDocumentName));
				br.com.anteros.core.utils.IOUtils.copy(openStream, fos);
				fos.flush();
				fos.close();
				openStream.close();
			} catch (IOException e) {
				throw new MojoExecutionException(
						"Não foi informado o nome do documento principal para gerar a documentação e não foi encontrado o padrão.",
						e);
			}
		}
		try {
			InputStream openStream = ResourceUtils.getResourceAsStream("images/arquitetura_oauth2.png");
			if (!sourceDirectory.exists())
				sourceDirectory.mkdirs();

			File subdirImages = new File(sourceDirectory + File.separator + "images");
			if (!subdirImages.exists())
				subdirImages.mkdirs();

			FileOutputStream fos = new FileOutputStream(
					new File(subdirImages + File.separator + "arquitetura_oauth2.png"));
			br.com.anteros.core.utils.IOUtils.copy(openStream, fos);
			fos.flush();
			fos.close();
			openStream.close();
		} catch (IOException e) {
			throw new MojoExecutionException("Não foi possível copiar imagens.", e);
		}

		List<String> dependencySourcePaths = null;
		if (includeDependencySources) {
			try {
				dependencySourcePaths = getDependencySourcePaths();
			} catch (MavenReportException e1) {
				throw new MojoExecutionException("Ocorreu um erro obtendo source code das dependências.", e1);
			}
		}

		String temporaryDirectoryJson = rootDir + File.separator + TARGET + File.separator + ANTEROS_JSONC_JAVADOC;

		/**
		 * Monta a lista de parâmetros que serão usados para ler a
		 * documentação(javadoc).
		 */
		List<String> parameters = new ArrayList<String>();

		parameters.add(PROTECTED_OPTION);
		parameters.add(SOURCEPATH_OPTION);

		StringBuilder sbSourcesPath = new StringBuilder();
		sbSourcesPath.append(getSourcesPath());
		if (dependencySourcePaths != null) {
			for (String ds : dependencySourcePaths) {
				sbSourcesPath.append(File.pathSeparator);
				sbSourcesPath.append(ds);
			}
		}
		sbSourcesPath.append(File.pathSeparator).append(temporaryDirectoryJson);
		parameters.add(sbSourcesPath.toString());

		if (packageScanEndpoints != null) {
			for (String p : packageScanEndpoints) {
				if (p.contains("*"))
					throw new MojoExecutionException("Pacotes não podem conter asteriscos(*) no nome " + p);
				parameters.add(p);
			}
		}
		parameters.add(SPRING_WEB_CONTROLLER);
		parameters.add(DOCLET_OPTION);
		parameters.add(AnterosRestDoclet.class.getName());
		parameters.add("-verbose");
		parameters.add(CLASSPATH_OPTION);
		parameters.add(getClassPath());

		/**
		 * Executa o javadoc para obter os comentários referente as classes que serão
		 * geradas na documentação da API REST usando um Doclet customizado
		 * (AnterosRestDoclet)
		 */
		
		com.sun.tools.javadoc.Main.execute(parameters.toArray(new String[] {}));

		/**
		 * Lê o arquivo JSON contendo informações sobre os endpoints lidos com o javadoc
		 * via AnterosRestDoclet(Doclet customizado).
		 */
		File temporaryJsonPath = new File(temporaryDirectoryJson);
		if (!temporaryJsonPath.exists())
			temporaryJsonPath.mkdirs();

		File file = new File(temporaryJsonPath + File.separator + ANTEROS_JSON);
		FileInputStream fis=null;
		try {

			fis = new FileInputStream(file);
			/**
			 * Le os dados do JSON gerado a partir do javadoc.
			 */
			ObjectMapper mapper = new ObjectMapper();
			List<ClassDescriptor> classDescriptors = Arrays.asList(mapper.readValue(fis, ClassDescriptor[].class));
			
			fis.close();

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
			 * Gera o arquivo de items da documentação.
			 */
			String filePath = "";
			if (sourceDirectory != null) {
				filePath = sourceDirectory.getAbsolutePath();
			} else if (sourceDocumentName != null) {
				filePath = sourceDocumentName.substring(0, sourceDocumentName.lastIndexOf(File.separator));
			}

			this.anchors.clear();
			this.anchorsMobile.clear();
			this.anchorsIntegration.clear();

			URLClassLoader urlClassLoader = new URLClassLoader(createClassPath().toArray(new URL[] {}),
					Thread.currentThread().getContextClassLoader());
			Thread.currentThread().setContextClassLoader(urlClassLoader);

			for (ClassDescriptor cld : classDescriptors) {
				try {
					Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(cld.getClazzName());
					Class<?> parameterizedClass = ReflectionUtils.getParameterizedClass(clazz);
					if (parameterizedClass != null) {
						if (!anchors.containsKey(parameterizedClass.getCanonicalName())) {
							anchors.put(parameterizedClass.getCanonicalName(), UUID.randomUUID().toString());
						}
						if (!anchorsMobile.containsKey(parameterizedClass.getCanonicalName())) {
							anchorsMobile.put(parameterizedClass.getCanonicalName(), UUID.randomUUID().toString());
						}
						if (!anchorsIntegration.containsKey(parameterizedClass.getCanonicalName())) {
							anchorsIntegration.put(parameterizedClass.getCanonicalName(), UUID.randomUUID().toString());
						}
					}
				} catch (Exception e) {
				}
			}

			this.buildItemsPersistenceAdoc(classDescriptors.toArray(new ClassDescriptor[] {}), filePath);
			this.buildItemsSecurityAdoc(classDescriptors.toArray(new ClassDescriptor[] {}), filePath);
			this.buildItemsResourceAdoc(classDescriptors.toArray(new ClassDescriptor[] {}), filePath);
			this.buildItemsResourceSecurityAdoc(classDescriptors.toArray(new ClassDescriptor[] {}), filePath);
			this.buildMobileAdoc(classDescriptors.toArray(new ClassDescriptor[] {}), filePath);
			this.buildDataIntegrationAdoc(classDescriptors.toArray(new ClassDescriptor[] {}), filePath);

			

			/**
			 * Altera o CSS default do plugin para o tema azul
			 */

			if (!attributes.containsKey("stylesheet")) {
				File stylesheetFile = new File(filePath, "asciidoc_stylesheet.css");

				try {
					InputStream openStream = ResourceUtils.getResourceAsStream("asciidoc_stylesheet.css");
					FileOutputStream fos = new FileOutputStream(stylesheetFile);
					br.com.anteros.core.utils.IOUtils.copy(openStream, fos);
					fos.flush();
					fos.close();
					openStream.close();
				} catch (IOException e) {
					throw new MojoExecutionException("Não foi possível copiar o template padrão de CSS.", e);
				}

				this.attributes.put("stylesheet", stylesheetFile.getAbsolutePath());
			}

			/**
			 * Executa geração dos arquivos da documentação a partir dos arquivos .adoc
			 * (asciidoc)
			 */
			super.execute();

			Path path = Paths.get(super.outputDirectory + "/index.html");
			try {
				SearchField.create(path);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
			if (fis !=null)
				try {
					fis.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			throw new MojoExecutionException("Não foi possível gerar a documentação da API Rest.", e);
		}
	}

	private void buildDataIntegrationAdoc(ClassDescriptor[] classDescriptors, String filePath)
			throws MojoExecutionException, IOException, InstantiationException, IllegalAccessException,
			NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException,
			TemplateException {
		List<JavaClass> integrationClasses = getAllClasses(true, this.getPackageScanEntity(), this.createClassPath(),
				RemoteSynchDataIntegration.class);

		if (integrationClasses.size()==0) {
			return;
		}
		
		/**
		 * Ordena as classes pelo nome
		 */
		Collections.sort(integrationClasses, new Comparator<JavaClass>() {

			@Override
			public int compare(JavaClass o1, JavaClass o2) {

				try {
					Class<?> clazz1 = Thread.currentThread().getContextClassLoader().loadClass(o1.getCanonicalName());
					RemoteSynchDataIntegration remoteSynch1 = clazz1.getAnnotation(RemoteSynchDataIntegration.class);

					Class<?> clazz2 = Thread.currentThread().getContextClassLoader().loadClass(o2.getCanonicalName());
					RemoteSynchDataIntegration remoteSynch2 = clazz2.getAnnotation(RemoteSynchDataIntegration.class);

					return (remoteSynch1.name().compareTo(remoteSynch2.name()));
				} catch (ClassNotFoundException e) {
				}
				return 0;
			}
		});

		List<JavaClass> allClasses = getAllClasses(true, this.getPackageScanEntity(), this.createClassPath(),
				Entity.class);
		
		for (JavaClass jc : allClasses) {
			if (!anchorsIntegration.containsKey(jc.getCanonicalName())) {
				anchorsIntegration.put(jc.getCanonicalName(), UUID.randomUUID().toString());
			}
		}

		File itemsFile = new File(filePath, ITEMS_DATA_INTEGRATION_ADOC);

		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(itemsFile), StandardCharsets.UTF_8);
		URLClassLoader urlClassLoader = new URLClassLoader(createClassPath().toArray(new URL[] {}),
				Thread.currentThread().getContextClassLoader());
		Thread.currentThread().setContextClassLoader(urlClassLoader);

		Configuration configuration = new Configuration();
		configuration.setEncoding(Locale.getDefault(), "UTF-8");
		configuration.setTemplateLoader(new AnterosFreeMarkerTemplateLoader(AnterosRestDoclet.class, "/"));
		Template template = configuration.getTemplate(TEMPLATE_INTEGRATION_PERSISTENCE_TOPIC);
		template.process(new HashMap<>(), writer);
		writer.flush();

		for (JavaClass cld : allClasses) {
			try {
				Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(cld.getCanonicalName());
				RemoteSynchDataIntegration remoteSynchIntegration = clazz
						.getAnnotation(RemoteSynchDataIntegration.class);
				if (remoteSynchIntegration != null) {
					String anchor = UUID.randomUUID().toString();
					if (!anchorsIntegration.containsKey("_" + remoteSynchIntegration.name()))
						anchorsIntegration.put("_" + remoteSynchIntegration.name(), anchor);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		SnippetGenerator.generateDataIntegrationPersistence(urlBase, writer, integrationClasses, allClasses, anchorsIntegration);

		SnippetGenerator.generateResources(urlBase, writer, INTEGRATION, anchorsIntegration, classDescriptors);
		writer.flush();
		writer.close();
		
		urlClassLoader.close();
	}

	private void buildMobileAdoc(ClassDescriptor[] classDescriptors, String filePath) throws MojoExecutionException,
			IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException,
			IllegalArgumentException, InvocationTargetException, TemplateException {
		List<JavaClass> mobileClasses = getAllClasses(true, this.getPackageScanEntity(), this.createClassPath(),
				RemoteSynchMobile.class);
		if (mobileClasses.size()==0) {
			return;
		}

		/**
		 * Ordena as classes pelo nome
		 */
		Collections.sort(mobileClasses, new Comparator<JavaClass>() {

			@Override
			public int compare(JavaClass o1, JavaClass o2) {

				try {
					Class<?> clazz1 = Thread.currentThread().getContextClassLoader().loadClass(o1.getCanonicalName());
					RemoteSynchMobile remoteSynchMobile1 = clazz1.getAnnotation(RemoteSynchMobile.class);

					Class<?> clazz2 = Thread.currentThread().getContextClassLoader().loadClass(o2.getCanonicalName());
					RemoteSynchMobile remoteSynchMobile2 = clazz2.getAnnotation(RemoteSynchMobile.class);

					return (remoteSynchMobile1.name().compareTo(remoteSynchMobile2.name()));
				} catch (ClassNotFoundException e) {
				}
				return 0;
			}
		});

		List<JavaClass> allClasses = getAllClasses(true, this.getPackageScanEntity(), this.createClassPath(),
				Entity.class);
		
		for (JavaClass jc : allClasses) {
			if (!anchorsMobile.containsKey(jc.getCanonicalName())) {
				anchorsMobile.put(jc.getCanonicalName(), UUID.randomUUID().toString());
			}
		}

		File itemsFile = new File(filePath, ITEMS_MOBILE_ADOC);

		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(itemsFile), StandardCharsets.UTF_8);
		URLClassLoader urlClassLoader = new URLClassLoader(createClassPath().toArray(new URL[] {}),
				Thread.currentThread().getContextClassLoader());
		Thread.currentThread().setContextClassLoader(urlClassLoader);

		Configuration configuration = new Configuration();
		configuration.setEncoding(Locale.getDefault(), "UTF-8");
		configuration.setTemplateLoader(new AnterosFreeMarkerTemplateLoader(AnterosRestDoclet.class, "/"));
		Template template = configuration.getTemplate(TEMPLATE_MOBILE_PERSISTENCE_TOPIC);
		template.process(new HashMap<>(), writer);
		writer.flush();

		for (JavaClass cld : mobileClasses) {
			try {
				RemoteSynchMobile remoteSynchMobile;
				Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(cld.getCanonicalName());
				remoteSynchMobile = clazz.getAnnotation(RemoteSynchMobile.class);
				String anchor = UUID.randomUUID().toString();
				if (!anchorsMobile.containsKey(remoteSynchMobile.name()))
					anchorsMobile.put(remoteSynchMobile.name(), anchor);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		SnippetGenerator.generateMobilePersistence(urlBase, writer, mobileClasses, allClasses, anchorsMobile);

		SnippetGenerator.generateResources(urlBase, writer, MOBILE, anchorsMobile, classDescriptors);
		writer.flush();
		writer.close();
		urlClassLoader.close();
	}

	public String getPackageScanEntity() {
		StringBuilder result = new StringBuilder();
		StringBuilder sb = new StringBuilder();

		if (packageScanEntities.size() > 0) {
			for (int i = 0; i < packageScanEntities.size(); i++) {
				boolean boAppendDelimiter = (i == packageScanEntities.size() - 1) ? false : true;

				sb.append(packageScanEntities.get(i));

				if (boAppendDelimiter)
					sb.append(";");
			}
			result.append(sb);
		}
		return result.toString();
	}

	protected void buildItemsResourceAdoc(ClassDescriptor[] classDescriptors, String filePath)
			throws IOException, TemplateNotFoundException, MalformedTemplateNameException, ParseException,
			TemplateException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException,
			IllegalArgumentException, InvocationTargetException {
		File itemsFile = new File(filePath, ITEMS_ADOC);

		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(itemsFile), StandardCharsets.UTF_8);
		URLClassLoader urlClassLoader = new URLClassLoader(createClassPath().toArray(new URL[] {}),
				Thread.currentThread().getContextClassLoader());
		Thread.currentThread().setContextClassLoader(urlClassLoader);

		SnippetGenerator.generateResources(urlBase, writer, GENERAL, anchors, classDescriptors);
		writer.flush();
		writer.close();
		urlClassLoader.close();
	}

	protected void buildItemsResourceSecurityAdoc(ClassDescriptor[] classDescriptors, String filePath)
			throws IOException, TemplateNotFoundException, MalformedTemplateNameException, ParseException,
			TemplateException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException,
			IllegalArgumentException, InvocationTargetException {
		File itemsFile = new File(filePath, ITEMS_SECURITY_ADOC);

		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(itemsFile), StandardCharsets.UTF_8);
		URLClassLoader urlClassLoader = new URLClassLoader(createClassPath().toArray(new URL[] {}),
				Thread.currentThread().getContextClassLoader());
		Thread.currentThread().setContextClassLoader(urlClassLoader);

		SnippetGenerator.generateResources(urlBase, writer, SECURITY, anchors, classDescriptors);
		writer.flush();
		writer.close();
		urlClassLoader.close();
	}

	protected void buildItemsPersistenceAdoc(ClassDescriptor[] classDescriptors, String filePath)
			throws IOException, TemplateNotFoundException, MalformedTemplateNameException, ParseException,
			TemplateException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException,
			IllegalArgumentException, InvocationTargetException {
		List<JavaClass> persistenceClasses = getAllClasses(true, this.getPackageScanEntity(), this.createClassPath(),
				Entity.class);

		List<JavaClass> allClasses = getAllClasses(true, this.getPackageScanEntity(), this.createClassPath(),
				Entity.class);
		
		for (JavaClass jc : allClasses) {
			if (!anchors.containsKey(jc.getCanonicalName())) {
				anchors.put(jc.getCanonicalName(), UUID.randomUUID().toString());
			}
		}

		File itemsFile = new File(filePath, ITEMS_PERSISTENCE_ADOC);
		
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(itemsFile), StandardCharsets.UTF_8);
		
		URLClassLoader urlClassLoader = new URLClassLoader(createClassPath().toArray(new URL[] {}),
				Thread.currentThread().getContextClassLoader());
		Thread.currentThread().setContextClassLoader(urlClassLoader);

		SnippetGenerator.generatePersistence(urlBase, writer, false, anchors, persistenceClasses, allClasses);
		writer.flush();
		writer.close();
		urlClassLoader.close();
	}

	protected void buildItemsSecurityAdoc(ClassDescriptor[] classDescriptors, String filePath)
			throws IOException, TemplateNotFoundException, MalformedTemplateNameException, ParseException,
			TemplateException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException,
			IllegalArgumentException, InvocationTargetException {
		List<JavaClass> persistenceClasses = getAllClasses(true, this.getPackageScanEntity(), this.createClassPath(),
				Entity.class);

		List<JavaClass> allClasses = getAllClasses(true, this.getPackageScanEntity(), this.createClassPath(),
				Entity.class);
		
		for (JavaClass jc : allClasses) {
			if (!anchors.containsKey(jc.getCanonicalName())) {
				anchors.put(jc.getCanonicalName(), UUID.randomUUID().toString());
			}
		}
		
		File itemsFile = new File(filePath, ITEMS_PERSISTENCE_SECURITY_ADOC);

		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(itemsFile), StandardCharsets.UTF_8);
		URLClassLoader urlClassLoader = new URLClassLoader(createClassPath().toArray(new URL[] {}),
				Thread.currentThread().getContextClassLoader());
		Thread.currentThread().setContextClassLoader(urlClassLoader);

		SnippetGenerator.generatePersistence(urlBase, writer, true, anchors, persistenceClasses, allClasses);
		writer.flush();
		writer.close();
		urlClassLoader.close();
	}

	private List<JavaClass> getAllClasses(boolean generateForAbstractClass, String sourcesToScanEntities,
			List<URL> urls, Class<? extends Annotation> remoteClazz) throws IOException {
		List<JavaClass> result = new ArrayList<JavaClass>();
		URLClassLoader urlClassLoader = new URLClassLoader(urls.toArray(new URL[] {}),
				Thread.currentThread().getContextClassLoader());

		Thread.currentThread().setContextClassLoader(urlClassLoader);
		ClassLibraryBuilder libraryBuilder = new SortedClassLibraryBuilder();
		libraryBuilder.appendClassLoader(urlClassLoader);
		JavaProjectBuilder docBuilder = new JavaProjectBuilder(libraryBuilder);

		String[] packages = StringUtils.tokenizeToStringArray(sourcesToScanEntities, ", ;");
		List<Class<?>> scanClasses = ClassPathScanner
				.scanClasses(new ClassFilter().packages(packages).annotation(remoteClazz));
		/**
		 * Ordena as classes pelo nome
		 */

		/**
		 * Ordena as classes pelo nome
		 */
		Collections.sort(scanClasses, new Comparator<Class<?>>() {

			@Override
			public int compare(Class<?> o1, Class<?> o2) {
				return o1.getSimpleName().compareTo(o2.getSimpleName());
			}
		});
		for (Class<?> cl : scanClasses) {
			if (Modifier.isAbstract(cl.getModifiers()) && !generateForAbstractClass) {
				continue;
			}

			JavaClass javaClass = docBuilder.getClassByName(cl.getName());
			if (javaClass != null)
				result.add(javaClass);
		}
		
		urlClassLoader.close();

		return result;
	}

	protected StringBuilder getSourcesPath() {
		StringBuilder classPath = new StringBuilder();
		boolean appendDelimiter = false;

		for (String s : compileSourceRoots) {
			if (appendDelimiter)
				classPath.append(File.pathSeparator);
			classPath.append(s);
			appendDelimiter = true;
		}
		return classPath;
	}

	protected String getClassPath() {
		StringBuilder classPath;
		boolean appendDelimiter;
		classPath = new StringBuilder();
		appendDelimiter = false;

		for (String s : classpathElements) {
			if (appendDelimiter)
				classPath.append(File.pathSeparator);
			classPath.append(s);
			appendDelimiter = true;
		}
		return classPath.toString();
	}

	protected List<URL> createClassPath() {
		List<URL> list = new ArrayList<URL>();
		if (classpathElements != null) {
			for (String cpel : classpathElements) {
				try {
					list.add(new File(cpel).toURI().toURL());
				} catch (MalformedURLException mue) {
				}
			}
		}
		return list;
	}

	public List<String> getPackageScanEndpoints() {
		return packageScanEndpoints;
	}

	/**
	 * Resolve dependency sources so they can be included directly in the javadoc
	 * process. To customize this, override
	 * {@link AbstractJavadocMojo#configureDependencySourceResolution(SourceResolverConfig)}.
	 */
	protected final List<String> getDependencySourcePaths() throws MavenReportException {
		try {
			if (sourceDependencyCacheDir.exists()) {
				FileUtils.forceDelete(sourceDependencyCacheDir);
				sourceDependencyCacheDir.mkdirs();
			}
		} catch (IOException e) {
			throw new MavenReportException(
					"Failed to delete cache directory: " + sourceDependencyCacheDir + "\nReason: " + e.getMessage(), e);
		}

		final SourceResolverConfig config = getDependencySourceResolverConfig();

		final AndArtifactFilter andFilter = new AndArtifactFilter();

		final List<String> dependencyIncludes = dependencySourceIncludes;
		final List<String> dependencyExcludes = dependencySourceExcludes;

		if (!includeTransitiveDependencySources || isNotEmpty(dependencyIncludes) || isNotEmpty(dependencyExcludes)) {
			if (!includeTransitiveDependencySources) {
				andFilter.add(createDependencyArtifactFilter());
			}

			if (isNotEmpty(dependencyIncludes)) {
				andFilter.add(new PatternIncludesArtifactFilter(dependencyIncludes, false));
			}

			if (isNotEmpty(dependencyExcludes)) {
				andFilter.add(new PatternExcludesArtifactFilter(dependencyExcludes, false));
			}

			config.withFilter(andFilter);
		}

		try {
			return ResourceResolver.resolveDependencySourcePaths(config);
		} catch (final ArtifactResolutionException e) {
			throw new MavenReportException(
					"Failed to resolve one or more javadoc source/resource artifacts:\n\n" + e.getMessage(), e);
		} catch (final ArtifactNotFoundException e) {
			throw new MavenReportException(
					"Failed to resolve one or more javadoc source/resource artifacts:\n\n" + e.getMessage(), e);
		}
	}

	/**
	 * Construct a SourceResolverConfig for resolving dependency sources and
	 * resources in a consistent way, so it can be reused for both source and
	 * resource resolution.
	 *
	 */
	private SourceResolverConfig getDependencySourceResolverConfig() {
		return configureDependencySourceResolution(
				new SourceResolverConfig(getLog(), project, localRepository, sourceDependencyCacheDir, resolver,
						factory, artifactMetadataSource, archiverManager).withReactorProjects(reactorProjects));
	}

	/**
	 * Override this method to customize the configuration for resolving dependency
	 * sources. The default behavior enables the resolution of -sources jar files.
	 */
	protected SourceResolverConfig configureDependencySourceResolution(final SourceResolverConfig config) {
		return config.withCompileSources();
	}

	/**
	 * Returns a ArtifactFilter that only includes direct dependencies of this
	 * project (verified via groupId and artifactId).
	 *
	 * @return
	 */
	private ArtifactFilter createDependencyArtifactFilter() {
		Set<Artifact> dependencyArtifacts = project.getDependencyArtifacts();

		List<String> artifactPatterns = new ArrayList<String>(dependencyArtifacts.size());
		for (Artifact artifact : dependencyArtifacts) {
			artifactPatterns.add(artifact.getGroupId() + ":" + artifact.getArtifactId());
		}

		return new IncludesArtifactFilter(artifactPatterns);
	}

	public static class DependencyComparator implements Comparator<Class<?>> {

		public int compare(Class<?> c1, Class<?> c2) {

			if (c1 == null) {
				if (c2 == null) {
					return 0;
				} else {
					// Sort nullos primeiro
					return 1;
				}
			} else if (c2 == null) {
				// Sort nulos primeiro
				return -1;
			}

			// Neste ponto, sabemos que c1 e c2 não são nulos
			if (c1.equals(c2)) {
				return 0;
			}

			Field[] fields = ReflectionUtils.getAllDeclaredFields(c1);
			for (Field field : fields) {
				if (field.getType().equals(c2) || ReflectionUtils.isExtendsClass(c2, field.getType())) {
					return 1;
				}
			}

			fields = ReflectionUtils.getAllDeclaredFields(c2);
			for (Field field : fields) {
				if (field.getType().equals(c1) || ReflectionUtils.isExtendsClass(c1, field.getType())) {
					return -1;
				}
			}

			// Neste ponto, c1 e c2 não são nulos e não iguais, vamos
			// compará-los para ver qual é "superior" na hierarquia de classes
			boolean c1Lower = c2.isAssignableFrom(c1);
			boolean c2Lower = c1.isAssignableFrom(c2);

			if (c1Lower && !c2Lower) {
				return 1;
			} else if (c2Lower && !c1Lower) {
				return -1;
			}

			return c1.getName().compareTo(c2.getName());
		}
	}

}
