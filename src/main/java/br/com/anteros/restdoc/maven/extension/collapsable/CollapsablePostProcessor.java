package br.com.anteros.restdoc.maven.extension.collapsable;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Postprocessor;

import br.com.anteros.restdoc.maven.extension.ReadResources;


public class CollapsablePostProcessor extends Postprocessor {

    public static final String TAG = "toc-collapsable";

    public CollapsablePostProcessor(){
    }

    @Override
    public String process(Document document, String output) {

        String code = (String)(document.getAttributes().get(TAG));
        if( code == null)
            return output;

        output = ReadResources.addCss(output,"/collapsable.css");
        output = ReadResources.includeJQuery(output);
        output = ReadResources.addExternalJs(output,"//cdnjs.cloudflare.com/ajax/libs/clipboard.js/1.7.1/clipboard.min.js");
        output = ReadResources.addJs(output,"/collapsable.js");

        return output;
    }

}
