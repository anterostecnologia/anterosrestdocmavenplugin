# Anteros Rest Documentation

O **Anteros Rest Documentation** é um plugin maven _open source_ baseado no [Spring & JAX-RS Rest Doclet](https://github.com/calrissian/rest-doclet) para geração automática de documentação de API's REST tanto para Spring como para JAX-RS. No entanto o projeto além de utilizar um Doclet customizado para leitura do Javadoc como no outro projeto, ele combina funcionalidades encontradas no [Apache Maven Javadoc Plugin](https://maven.apache.org/plugins/maven-javadoc-plugin/) e ainda herda do [Asciidoctor Maven Plugin](https://github.com/asciidoctor/asciidoctor-maven-plugin) a capacidade de geração documentação em diversos formatos a partir de uma arquivo asciidoc. Isso nos permitiu que combinar todas a necessidades em uma única ferramenta sem perder a flexibilidade.


## Introdução

Não há nenhuma configuração especial necessária no seu código para permitir que esta ferramenta extraia informações básicas sobre seus serviços REST se eles usam anotações Spring ou JAX-RS. Basta usar anotações e a ferramenta pode extrair informações básicas sobre como um **endPoint** deve ser chamado. Além disso, toda a documentação dos **endPoint** incluindo seus parâmetros de consulta e parâmetros de caminho são extraídos diretamente dos comentários Javadoc em cada classe e método.

Usando o exemplo a seguir, a ferramenta reconhecerá um **endPoint** ("/pessoa/nome") com um único parâmetro de caminho ("nome") e um único parâmetro de consulta ("normalise").

```java
/**
 * Examplo Rest com Spring
 */
@Controller
@RequestMapping("/pessoa")
public class Exemplo {

    /**
     * Retorna informações sobre uma pessoa.
     * @param nome Nome da pessoa
     * @param normalise Indica se é para normalizar o nome da pessoa
     */
    @RequestMapping(value = "/{nome}", method = GET)
    @ResponseBody
    public String getInformation(@PathVariable String nome, @RequestParam(required = false) boolean normalise) {
        return (normalise ? nome.toLowerCase() : nome);
    }
}
```

Com base nas informações coletadas sobre os **endPoint's** a ferramenta irá gerar a documentação usando o asciidoc. A saida para a documentação pode ser configurada com os formatos aceitos pelo asciidoc.Ex: HTML,PDF,etc. Se a documentação gerada for no formato HTML irá conter campos para informação dos parâmetros e execução de uma chamada ao **endPoint** (serviço REST) permitindo visualizar o resultado.


## Configuração 

### Configuração do Maven

Para utilizar o plugin adicione a dependência no `pom.xml`:

```xml
    <dependency>
        <groupId>br.com.anteros</groupId>
        <artifactId>Anteros-RestDoc-Maven-Plugin</artifactId>
        <version>[1.0.0,)</version>
    </dependency>
```

Configure o plugin para executar como no exemplo a seguir:

```xml
    <plugin>
		<groupId>br.com.anteros</groupId>
			<artifactId>Anteros-RestDoc-Maven-Plugin</artifactId>
			<executions>
				<execution>
					<id>generate-restdoc</id>
					<goals>
						<goal>generate</goal>
					</goals>
					<phase>package</phase>
					<configuration>
						<backend>html</backend>
						<doctype>book</doctype>
						<outputDirectory>src/main/webapp/doc</outputDirectory>
						<packageScanEndpoints>
							<package>br.com.anteros.exemplo.controller</package>
						</packageScanEndpoints>
						<urlBase>http://localhost:8090/${project.name}</urlBase>
						<includeDependencySources>true</includeDependencySources>
						<dependencySourceIncludes>
							<dependencySourceInclude>br.com.anteros:Anteros-Spring-Web</dependencySourceInclude>
						</dependencySourceIncludes>
						<attributes>
						   <documentTitle>Anteros Exemplo</documentTitle>
						</attributes>
					</configuration>
				</execution>
			</executions>
		</plugin>
```	

Tabela dos principais parâmetros que podem ser configurados, os demais podem ser consultados na documentação do asciidoc:

--		
| Nome | Descrição 
| :---         |     :---:      
| backend   | Formato de saida do documento final conforme documentação asciidoc. Ex: html, pdf, etc
| doctype     | Tipo de documento conforme documentação asciidoc.  
| sourceDirectory | Nome do diretório onde estão os documentos para geração da documentação final. Padrão: ${basedir}/src/main/asciidoc
| sourceDocumentName | Nome do documento asciidoc a ser usado como padrão para geração da documentação da final. Você prefirir criar um documento personalizado pode informar o nome do documento.
| outputDirectory | Diretório onde serão gerados os documentos de saída. Padrão ${project.build.directory}/generated-docs
| packageScanEndpoints | Lista de pacotes onde devem ser encontrados os **endPoint's** REST.
| urlBase | URL base para execução de chamadas aos **endPoint's**(serviços) REST.
| includeDependencySources | True se devem ser incluídos código fonte das dependências caso algum **endPoint** seja herança de outra classe ou caso vc tenha **endPoint's** em outros arquivos externos.
| dependencySourceIncludes | Lista de artefatos que devem ser incluídos. Devem seguir o padrão: **Group id:Artifact id** Ex:  br.com.anteros:Anteros-Spring-Web
| dependencySourceExcludes | Lista de artefatos que devem ser excluídos. Devem seguir o padrão: **Group id:Artifact id** Ex:  br.com.anteros:Anteros-Spring-Web
| attributes |  Um documento asciidoc aceita passagem de atributos. O documento padrão gerado pelo Anteros REST Documentation aceita um atributo **documentTitle** onde vc pode passar o título do documento. Caso vc tenha criado um documento asciidoc personalizado pode passar atributos customizados para ele.


Para a formatação do documento de saída pelo asciidoc você pode usar qualquer parâmetro/atributo de acordo com a documentação do [Asciidoctor Maven Plugin](https://github.com/asciidoctor/asciidoctor-maven-plugin) 

No caso de criar um documento asciidoc personalizado o mesmo deverá conter obrigatoriamente um include para o arquivo de detalhe que será gerado com o nome de items.adoc. O arquivo de detalhe será gerado no mesmo diretório de fontes dos documentos(sourceDirectory).

**include::items.adoc[]**




## Licença ##

Apache 2.0

http://www.apache.org/licenses/LICENSE-2.0


<center>
![alt text](https://avatars0.githubusercontent.com/u/16067889?v=3&u=ab2eb482a16fd90a17d7ce711885f0bdc0640997&s=64)  
Anteros Tecnologia
