package br.com.anteros.restdoc.maven.plugin.doclet.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 
 * @author Edson Martins
 * @author Eduardo Albertini
 *
 */
public class PathVar {

    private  String name;
    private  String description;
    private  String type;
    
    public PathVar(){
    	
    }

    public PathVar(String name, String description, String type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "PathVar{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

	public void setName(String name) {
		this.name = name;
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
