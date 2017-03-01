package br.com.anteros.restdoc.maven.plugin;

import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.ANTEROS_JSON;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.ANTEROS_JSONC_JAVADOC;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.CLASSPATH_OPTION;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.DOCLET_OPTION;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.ITEMS_ADOC;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.PROTECTED_OPTION;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.SOURCEPATH_OPTION;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.SPRING_WEB_CONTROLLER;
import static br.com.anteros.restdoc.maven.plugin.AnterosRestConstants.TARGET;
import static org.apache.maven.plugin.javadoc.JavadocUtil.isNotEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import org.apache.maven.plugin.javadoc.AbstractJavadocMojo;
import org.apache.maven.plugin.javadoc.resolver.ResourceResolver;
import org.apache.maven.plugin.javadoc.resolver.SourceResolverConfig;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.shared.artifact.filter.PatternExcludesArtifactFilter;
import org.apache.maven.shared.artifact.filter.PatternIncludesArtifactFilter;
import org.asciidoctor.maven.AsciidoctorMojo;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.util.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.anteros.core.utils.ResourceUtils;
import br.com.anteros.restdoc.maven.plugin.doclet.AnterosRestDoclet;
import br.com.anteros.restdoc.maven.plugin.doclet.model.ClassDescriptor;

/**
 * 
 * @author Edson Martins
 * @author Eduardo Albertini
 *
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE, requiresDependencyCollection = ResolutionScope.COMPILE)
public class AnterosRestDocMojo extends AsciidoctorMojo {

	@Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
	private File outputDirectory;

	@Parameter(defaultValue = "${project}")
	private MavenProject project = null;

	@Parameter(defaultValue = "${project.name}", required = true)
	private String projectDisplayName;

	@Parameter(defaultValue = "${project.compileSourceRoots}")
	private List<String> compileSourceRoots;

	@Parameter(defaultValue = "${project.compileClasspathElements}")
	private List<String> classpathElements;

	@Parameter(required = true)
	private List<String> packageScanEndpoints = new ArrayList<String>();

	@Parameter(required = true)
	private String urlBase;

	@Parameter(defaultValue = "false")
	private boolean includeDependencySources;

	@Parameter(defaultValue = "${project.build.directory}/distro-javadoc-sources")
	private File sourceDependencyCacheDir;

	/**
	 * List of included dependency-source patterns. Example:
	 * <code>org.apache.maven:*</code>
	 *
	 * @see #includeDependencySources
	 */
	@Parameter
	private List<String> dependencySourceIncludes;

	/**
	 * List of excluded dependency-source patterns. Example:
	 * <code>org.apache.maven.shared:*</code>
	 *
	 * @see #includeDependencySources
	 */
	@Parameter
	private List<String> dependencySourceExcludes;

	@Parameter(property = "localRepository")
	private ArtifactRepository localRepository;

	@Parameter(property = "project.remoteArtifactRepositories")
	private List<ArtifactRepository> remoteRepositories;

	@Parameter(defaultValue = "false")
	private boolean includeTransitiveDependencySources;

	/**
	 * Archiver manager
	 *
	 * @since 2.5
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
	 *
	 * @since 2.1
	 */
	@Component
	private ArtifactMetadataSource artifactMetadataSource;

	/**
	 * Used for resolving artifacts
	 */
	@Component
	private ArtifactResolver resolver;

	@Parameter(property = "reactorProjects", readonly = true)
	private List<MavenProject> reactorProjects;

	public void execute() throws MojoExecutionException, MojoFailureException {
		
		/**
		 * Se não informar o nome do documento .adoc principal
		 * copiamos o padrão para a pasta sourceDirectory.
		 */
		if (sourceDocumentName==null){
			sourceDocumentName = "index.adoc";
			try {
				InputStream openStream = ResourceUtils.getResourceAsStream("template_index.adoc");
				FileOutputStream fos = new FileOutputStream(new File(sourceDirectory+File.separator+sourceDocumentName));
				br.com.anteros.core.utils.IOUtils.copy(openStream, fos);
				fos.flush();
				fos.close();
				openStream.close();
			} catch (IOException e) {
				throw new MojoExecutionException("Não foi informado o nome do documento principal para gerar a documentação e não foi encontrado o padrão.",e);
			}
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
				parameters.add(p);
			}
		}
		parameters.add(SPRING_WEB_CONTROLLER);
		parameters.add(DOCLET_OPTION);
		parameters.add(AnterosRestDoclet.class.getName());
		parameters.add(CLASSPATH_OPTION);
		parameters.add(getClassPath());

		/**
		 * Executa o javadoc para obter os comentários referente as classes que
		 * serão geradas na documentação da API REST usando um Doclet
		 * customizado (AnterosRestDoclet)
		 */
		com.sun.tools.javadoc.Main.execute(parameters.toArray(new String[] {}));

		/**
		 * Lê o arquivo JSON contendo informações sobre os endpoints lidos com o
		 * javadoc via AnterosRestDoclet(Doclet customizado).
		 */
		File file = new File(temporaryDirectoryJson + File.separator + ANTEROS_JSON);
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			/**
			 * Le os dados do JSON gerado a partir do javadoc.
			 */
			ObjectMapper mapper = new ObjectMapper();
			ClassDescriptor[] classDescriptors = mapper.readValue(fis, ClassDescriptor[].class);

			/**
			 * Gera o arquivo de items da documentação.
			 */
			String filePath = "";
			if (sourceDirectory != null) {
				filePath = sourceDirectory.getAbsolutePath();
			} else if (sourceDocumentName != null) {
				filePath = sourceDocumentName.substring(0, sourceDocumentName.lastIndexOf(File.separator));
			}

			File itemsFile = new File(filePath, ITEMS_ADOC);

			Writer writer = new FileWriter(itemsFile);
			SnippetGenerator.generate(urlBase, writer, classDescriptors);
			writer.flush();
			writer.close();
			
			fis.close();
			/**
			 * Executa geração dos arquivos da documentação a partir dos
			 * arquivos .adoc (asciidoc)
			 */
			super.execute();

		} catch (Exception e) {
			throw new MojoExecutionException("Não foi possível gerar a documentação da API Rest.", e);
		}
	}

	protected StringBuilder getSourcesPath() {
		StringBuilder classPath = new StringBuilder();
		boolean appendDelimiter = false;

		for (String s : compileSourceRoots) {
			if (appendDelimiter)
				classPath.append(":");
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
				classPath.append(":");
			classPath.append(s);
			appendDelimiter = true;
		}
		return classPath.toString();
	}

	public List<String> getPackageScanEndpoints() {
		return packageScanEndpoints;
	}

	/**
	 * Resolve dependency sources so they can be included directly in the
	 * javadoc process. To customize this, override
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
	 * Override this method to customize the configuration for resolving
	 * dependency sources. The default behavior enables the resolution of
	 * -sources jar files.
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

}
