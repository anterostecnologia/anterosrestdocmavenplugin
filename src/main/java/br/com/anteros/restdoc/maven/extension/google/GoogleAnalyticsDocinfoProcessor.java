package br.com.anteros.restdoc.maven.extension.google;
import java.util.Map;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.DocinfoProcessor;

import br.com.anteros.restdoc.maven.extension.ReadResources;

/**
 * Created by jorge on 10/06/17.
 */
public class GoogleAnalyticsDocinfoProcessor extends DocinfoProcessor {

    public GoogleAnalyticsDocinfoProcessor(){
        super();
    }

    public GoogleAnalyticsDocinfoProcessor(Map<String, Object> config) {
        super(config);
    }

    public static final String TAG = "google-analytics-code";


    @Override
    public String process(Document document) {

        if("html5".equals(document.getAttributes().get("backend"))==false)
            return "\n";

        String code = (String)(document.getAttributes().get(TAG));
        if( code == null)
            return "\n";

        String javascript = ReadResources.readResource("/googleAnalytics.js");
        String footer = String.format("%n%s%n", javascript.replace("UA-XXXXXX-XX", code));
        return footer;
    }



}
