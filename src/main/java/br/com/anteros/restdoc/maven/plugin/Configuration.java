package br.com.anteros.restdoc.maven.plugin;

import static java.lang.Boolean.parseBoolean;
import java.util.Map;

public class Configuration {

    public enum ConfigOption {
        OUTPUT_FORMAT("o", "Output Format", "legacy"),

        //Legacy Options
        TITLE("t", "title", "REST Endpoint Descriptions"),
        STYLESHEET("stylesheet", "stylesheet", "./stylesheet.css"),

        //Swagger options
        API_VERSION("version", "version", null),
        DISPLAY_ONLY("callable", "callable", "true"),
        BASEPATH("path", "base path", "/");

        private final String option;
        private final String description;
        private final String defaultValue;

        private ConfigOption(String option, String description, String defaultValue) {
            this.option = "-" + option;
            this.description = description;
            this.defaultValue = defaultValue;
        }

        public String getOption() {
            return option;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        /**
         * @return the description
         */
        public String getDescription() {
            return description;
        }
    }

    private Map<String, String> options;

    public Configuration(Map<String, String> options) {
        this.options = options;
    }

    public String getOutputFormat() {
        return getOption(ConfigOption.OUTPUT_FORMAT);
    }

    public String getDocumentTitle() {
        return getOption(ConfigOption.TITLE);
    }

    public String getStyleSheet() {
        return getOption(ConfigOption.STYLESHEET);
    }

    public String getApiVersion() {
        return getOption(ConfigOption.API_VERSION);
    }

    public String getPath() {
        return getOption(ConfigOption.BASEPATH);
    }

    public boolean isCallable() {
        return parseBoolean(getOption(ConfigOption.DISPLAY_ONLY));
    }

    public boolean isdefaultStyleSheet() {
        return getOption(ConfigOption.STYLESHEET.getOption(), null) == null;
    }

    private String getOption(ConfigOption configOption) {
        return getOption(configOption.getOption(), configOption.getDefaultValue());
    }

    private String getOption(String name, String defaultValue) {
        if (options.containsKey(name)) {
            return options.get(name);
        }
        return defaultValue;
    }

    public static int getOptionLength(String option) {

        for (ConfigOption configOption : ConfigOption.values())
            if (option.equals(configOption.getOption()))
                return 2;

        return 0;
    }
}
