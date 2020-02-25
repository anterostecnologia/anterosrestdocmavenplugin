package br.com.anteros.restdoc.maven.extension;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;


public class ReadResources {

    public static String includeJQuery(String output){

        if( output.indexOf("<script type='text/javascript'>/*! jQuery") !=-1)
            return output;

        String jquery = readResource("/jquery-3.2.1.min.js");
        String replacement = new StringBuffer()
                .append("</body>")
                .append("<script type='text/javascript'>").append(jquery).append("</script>")
                .toString();

        return output.replace("</body>", replacement);
    }

    public static String addCss(String output, String resource){
        String css = readResource(resource);
        String replacement = new StringBuffer()
                .append("<style>").append(css).append("</style>")
                .append("</head>")
                .toString();
        return  output.replace("</head>", replacement);
    }

    public static String addJs(String output, String resource){
        String javascript = readResource(resource);
        String replacement = new StringBuffer()
                .append("<script type='text/javascript'>").append(javascript).append("</script>")
                .append("</html>")
                .toString();
        return output.replace("</html>", replacement);
    }

    public static String addExternalJs(String output, String url){
        String replacement = new StringBuffer()
                .append("</body>")
                .append("<script type='text/javascript' src='").append(url).append("'></script>")
                .toString();
        return output.replace("</body>", replacement);
    }


    public static String readResource(String name) {
        InputStream is = ReadResources.class.getResourceAsStream(name);
        if( is == null )
            return "";
        Reader reader = new InputStreamReader( is );
        try {
            StringWriter writer = new StringWriter();
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer)) >= 0) {
                writer.write(buffer, 0, read);
            }
            return writer.toString();
        }
        catch (Exception ex) {
            //throw new IllegalStateException("Failed to read '" + name + "'", ex);
            return "";
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                // Continue
            }
        }
    }

    public static void extractResourceToFile(String resource, File file) {
        try {
            file.getParentFile().mkdirs();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(readResourceAsBytes(resource));
            fileOutputStream.close();
        }catch(IOException ioe){

        }
    }

    public static byte[] readResourceAsBytes(String name) {
        InputStream is = ReadResources.class.getResourceAsStream(name);
        if( is == null )
            return new byte[0];
        Reader reader = new InputStreamReader( is );
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) >= 0) {
                bos.write(buffer,0,read);
            }
            return bos.toByteArray();
        }
        catch (Exception ex) {
            return new byte[0];
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                // Continue
            }
        }
    }

}
