package br.com.anteros.restdoc.maven.plugin.doclet.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 
 * @author Edson Martins
 * @author Eduardo Albertini
 *
 */
public class QueryParam {

    private  String name;
    private  boolean required;
    private  String description;
    private  String type;
    
    public QueryParam(){
    	
    }

    public QueryParam(String name, boolean required, String description, String type) {
        this.name = name;
        this.required = required;
        this.description = description;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "QueryParam{" +
                "name='" + name + '\'' +
                ", required=" + required +
                ", description='" + description + '\'' +
                '}';
    }

	public void setName(String name) {
		this.name = name;
	}

	public void setRequired(boolean required) {
		this.required = required;
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
