package br.com.anteros.restdoc.maven.extension;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.spi.ExtensionRegistry;

import br.com.anteros.restdoc.maven.extension.clipboard.ClipboardPostprocessor;
import br.com.anteros.restdoc.maven.extension.collapsable.CollapsablePostProcessor;
import br.com.anteros.restdoc.maven.extension.google.GoogleAnalyticsDocinfoProcessor;
import br.com.anteros.restdoc.maven.extension.google.GoogleSearchDocinfoProcessor;
import br.com.anteros.restdoc.maven.extension.https.HttpsPostProcessor;
import br.com.anteros.restdoc.maven.extension.tooltip.TooltipPostprocessor;


public class AnterosAsciidoctorExtensionRegistry implements ExtensionRegistry {

    @Override
    public void register(Asciidoctor asciidoctor) {

        {
            Map<String, Object> options = new HashMap<String, Object>();
            options.put("location", ":footer");
            GoogleAnalyticsDocinfoProcessor googleAnalyticsDocinfo = new GoogleAnalyticsDocinfoProcessor(options);
            asciidoctor.javaExtensionRegistry().docinfoProcessor(googleAnalyticsDocinfo);
        }

        asciidoctor.javaExtensionRegistry().postprocessor(CollapsablePostProcessor.class);

        asciidoctor.javaExtensionRegistry().postprocessor(ClipboardPostprocessor.class);

        asciidoctor.javaExtensionRegistry().docinfoProcessor(GoogleSearchDocinfoProcessor.class);

        asciidoctor.javaExtensionRegistry().postprocessor(TooltipPostprocessor.class);

        asciidoctor.javaExtensionRegistry().postprocessor(HttpsPostProcessor.class);

    }

}
