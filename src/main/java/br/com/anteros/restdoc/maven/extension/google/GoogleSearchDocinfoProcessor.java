package br.com.anteros.restdoc.maven.extension.google;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.DocinfoProcessor;

import br.com.anteros.restdoc.maven.extension.ReadResources;


public class GoogleSearchDocinfoProcessor extends DocinfoProcessor{

    public GoogleSearchDocinfoProcessor(){
        super();
    }

    public GoogleSearchDocinfoProcessor(Map<String, Object> config) {
        super(config);
    }

    public static final String TAG = "google-search";

    @Override
    public String process(Document document) {

        if("html5".equals(document.getAttributes().get("backend"))==false)
            return "\n";

        String code = (String)(document.getAttributes().get(TAG));
        if( code == null)
            return "\n";

        String doctype = (String)document.getAttributes().get("doctype");
        if( doctype == null)
            doctype = "article";

        switch (doctype){
            case "article":
                return article(document);
            case "book":
                return book(document);
            default:
                return "\n";
        }
    }

    private String article(Document document){
        String resource = ReadResources.readResource("/googlesearch/article.html");
        resource = resource.replace("[[TITLE]]",document.doctitle());
        resource = resource.replace("[[AUTHOR]]",document.getAttr("author","").toString());

        String caption = document.getTitle();
        if( caption == null )
            caption = "";
        resource = resource.replace("[[DESCRIPTION]]",caption);

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        String now = df.format(new Date());

        resource = resource.replace("[[DATE_PUBLISHED]]",(String)document.getAttr("revdate", now));
        resource = resource.replace("[[DATE_MODIFIED]]",now);

        return resource;
    }


    private String book(Document document){
        String resource = ReadResources.readResource("/googlesearch/book.html");
        resource = resource.replace("[[TITLE]]",document.doctitle());
        resource = resource.replace("[[AUTHOR]]",document.getAttr("author","").toString());
        return resource;
    }


}
