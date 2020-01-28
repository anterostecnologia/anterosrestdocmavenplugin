package br.com.anteros.restdoc.maven.extension.https;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Postprocessor;

import br.com.anteros.restdoc.maven.extension.ReadResources;


public class HttpsPostProcessor extends Postprocessor {

    public static final String TAG = "ensure-https";

    public HttpsPostProcessor(){

    }

    @Override
    public String process(Document document, String output) {

        String code = (String)(document.getAttributes().get(TAG));
        if( code == null)
            return output;

        output = ReadResources.includeJQuery(output);
        output = ReadResources.addJs(output,"/https.js");

        return output;
    }

}
