package br.com.anteros.restdoc.maven.plugin.doclet.model;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * 
 * @author Edson Martins
 * @author Eduardo Albertini
 *
 */
public class Endpoint {

    private  String path;
    private  String httpMethod;
    private  Collection<QueryParam> queryParams;
    private  Collection<PathVar> pathVars;
    private  RequestBody requestBody;
    private  Collection<String> consumes;
    private  Collection<String> produces;
    private  String shortDescription;
    private  String description;
    private  String type;

    
    public Endpoint(){
    	
    }
    public Endpoint(
            String path,
            String httpMethod,
            Collection<QueryParam> queryParams,
            Collection<PathVar> pathVars,
            RequestBody requestBody,
            Collection<String> consumes,
            Collection<String> produces,
            String shortDescription,
            String description,
            String type) {

        this.path = path;
        this.httpMethod = httpMethod;
        this.queryParams = queryParams;
        this.pathVars = pathVars;
        this.requestBody = requestBody;
        this.consumes = consumes;
        this.produces = produces;
        this.shortDescription = shortDescription;
        this.description = description;
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public Collection<QueryParam> getQueryParams() {
        return queryParams;
    }

    public Collection<PathVar> getPathVars() {
        return pathVars;
    }

    public RequestBody getRequestBody() {
        return requestBody;
    }

    public Collection<String> getConsumes() {
        return consumes;
    }

    public Collection<String> getProduces() {
        return produces;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }
   
    @Override
    public String toString() {
        return "Endpoint{" +
                "path='" + path + '\'' + '\n'+
                ", httpMethod='" + httpMethod + '\'' +'\n'+
                ", queryParams=" + queryParams +'\n'+
                ", pathVars=" + pathVars +'\n'+
                ", requestBody=" + requestBody +'\n'+
                ", consumes=" + consumes +'\n'+
                ", produces=" + produces +'\n'+
                ", shortDescription='" + shortDescription + '\'' +'\n'+
                ", description='" + description + '\'' +'\n'+
                '}';
    }
	public void setPath(String path) {
		this.path = path;
	}
	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}
	public void setQueryParams(Collection<QueryParam> queryParams) {
		this.queryParams = queryParams;
	}
	public void setPathVars(Collection<PathVar> pathVars) {
		this.pathVars = pathVars;
	}
	public void setRequestBody(RequestBody requestBody) {
		this.requestBody = requestBody;
	}
	public void setConsumes(Collection<String> consumes) {
		this.consumes = consumes;
	}
	public void setProduces(Collection<String> produces) {
		this.produces = produces;
	}
	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	@JsonIgnore
	public String getSimpleType() {
		if (type==null)
			return type;
		if (type.lastIndexOf(".")==-1)
			return type;
		
		return type.substring(type.lastIndexOf(".")+1);
	}

}
