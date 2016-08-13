package alien4cloud.repository.http;

public class HttpUtil {

    public static boolean isHttpURL(String reference) {
        return reference != null && (reference.startsWith("http://") || reference.startsWith("https://"));
    }

}
