package br.com.anteros.restdoc.maven.extension.tooltip;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Postprocessor;

import br.com.anteros.restdoc.maven.extension.ReadResources;

/**
 * Created by jorge on 15/07/17.
 */
public class TooltipPostprocessor extends Postprocessor {

    public static final String TAG = "notooltip";

    public TooltipPostprocessor(){

    }

    @Override
    public String process(Document document, String output) {

        if("html5".equals(document.getAttributes().get("backend"))==false)
            return output;

        String code = (String)(document.getAttributes().get(TAG));
        if( code != null)
            return output;


        output = ReadResources.includeJQuery(output);
        output = ReadResources.addJs(output,"/tooltip.js");
        output = ReadResources.addCss(output,"/tooltip.css");

        return output;
    }
}
