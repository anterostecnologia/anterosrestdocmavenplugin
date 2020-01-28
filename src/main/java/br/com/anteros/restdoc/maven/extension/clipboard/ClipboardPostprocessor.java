package br.com.anteros.restdoc.maven.extension.clipboard;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Postprocessor;

import br.com.anteros.restdoc.maven.extension.ReadResources;

/**
 * Created by jorge on 18/06/17.
 */
public class ClipboardPostprocessor extends Postprocessor{

    public static final String TAG = "nocopyblocks";

    public ClipboardPostprocessor(){

    }

    @Override
    public String process(Document document, String output) {

        String code = (String)(document.getAttributes().get(TAG));
        if( code != null)
            return output;

        output = ReadResources.includeJQuery(output);
        output = ReadResources.addExternalJs(output,"//cdnjs.cloudflare.com/ajax/libs/clipboard.js/1.7.1/clipboard.min.js");
        output = ReadResources.addJs(output,"/clipboard.js");
        output = ReadResources.addCss(output,"/clipboard.css");

        return output;
    }
}
