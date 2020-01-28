package br.com.anteros.restdoc.maven.plugin.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SearchField {
	
	static public String inputField = "<div><input type=\"text\" id=\"searchInMainMenu\" disabled placeholder=\"Procure na documentação\" title=\"Procure na documentação\"></div>";
	static public String searchFunction = "<script>$(document).ready(function(){$(\"#searchInMainMenu\").prop(\"disabled\",!1),$(\"#searchInMainMenu\").keyup(function(){var i=$(this).val();$(\".sectlevel0\").find(\"li\").each(function(){\"\"==i?($(this).css(\"visibility\",\"visible\"),$(this).fadeIn()):$(this).text().search(new RegExp(i,\"i\"))<0?($(this).css(\"visibility\",\"hidden\"),$(this).fadeOut()):($(this).css(\"visibility\",\"visible\"),$(this).fadeIn())})})});</script>";
	static public String jqueryCDN = "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js\"></script>";
	
	static public void create(Path path) throws IOException {
		List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
		
		String aux = null;
		for (int i = 0; i < lines.size(); i++) {
			aux = lines.get(i);
			if (aux.contains("<div id=\"toctitle\">Conteúdo</div>")) {
				lines.add(i+1, inputField);
			} else if (aux.contains("<title>")) {
				lines.add(i+1, jqueryCDN);
			} else if (aux.contains("<script>hljs.initHighlighting()</script>")) {
				lines.add(i+1, searchFunction);
			}
		}
		
		Files.write(path, lines, StandardCharsets.UTF_8);
	}
	
	public static void main(String[] args) {
		Path path = Paths.get("/Users/relevant3/dev/versatil/versatil-condominio-server/target/Versatil-Condominio-Server-1.0.0/doc/index.html");
		try {
			SearchField.create(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
