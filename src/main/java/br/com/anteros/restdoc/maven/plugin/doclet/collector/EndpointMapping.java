package br.com.anteros.restdoc.maven.plugin.doclet.collector;


import java.util.Collection;


/**
 * 
 * @author Edson Martins
 * @author Eduardo Albertini
 *
 */
public class EndpointMapping {
    private final Collection<String> paths;
    private final Collection<String> httpMethods;
    private final Collection<String> consumes;
    private final Collection<String> produces;

    public EndpointMapping(
            Collection<String> paths,
            Collection<String> httpMethods,
            Collection<String> consumes,
            Collection<String> produces) {

        this.paths = paths;
        this.httpMethods = httpMethods;
        this.consumes = consumes;
        this.produces = produces;
    }

    public Collection<String> getPaths() {
        return paths;
    }

    public Collection<String> getHttpMethods() {
        return httpMethods;
    }

    public Collection<String> getConsumes() {
        return consumes;
    }

    public Collection<String> getProduces() {
        return produces;
    }

	@Override
	public String toString() {
		return "EndpointMapping [paths=" + paths + ", httpMethods=" + httpMethods + ", consumes=" + consumes
				+ ", produces=" + produces + "]";
	}
    
    
}
