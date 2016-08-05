package alien4cloud.repository.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;

import alien4cloud.component.repository.IArtifactResolver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpArtifactResolver implements IArtifactResolver {

    private CloseableHttpClient httpclient = HttpClients.custom().setRedirectStrategy(new DefaultRedirectStrategy()).build();

    @Override
    public String getResolverType() {
        return "http";
    }

    @Override
    public boolean canHandleArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        if (StringUtils.isNotBlank(repositoryType) && !repositoryType.equals(getResolverType())) {
            return false;
        }
        // Must have credentials as user:password
        return credentials.indexOf(':') > 0 && isHttpURL(repositoryURL);
    }

    private boolean isHttpURL(String reference) {
        return reference.startsWith("http://") || reference.startsWith("https://");
    }

    @Override
    public InputStream resolveArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        if (!canHandleArtifact(artifactReference, repositoryURL, repositoryType, credentials)) {
            return null;
        }
        int indexOfSeparator = credentials.indexOf(':');
        String getURL = null;
        try {
            if (isHttpURL(artifactReference)) {
                getURL = artifactReference;
            } else {
                getURL = repositoryURL + "/" + URLEncoder.encode(artifactReference, "UTF-8");
            }
            // Prepare basic login
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(credentials.substring(0, indexOfSeparator),
                    credentials.substring(indexOfSeparator + 1, credentials.length())));
            // Add AuthCache to the execution context
            final HttpClientContext context = HttpClientContext.create();
            context.setCredentialsProvider(credentialsProvider);
            // Launch HTTP Get
            HttpGet httpget = new HttpGet(getURL);
            CloseableHttpResponse response = httpclient.execute(httpget, context);
            if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
                log.info("Received status {} while trying to download artifact at {}", response.getStatusLine(), getURL);
                response.close();
                return null;
            }
        } catch (ClientProtocolException e) {
            log.info("Error downloading artifact at " + getURL, e);
            return null;
        } catch (IOException e) {
            log.warn("Error downloading artifact at " + getURL, e);
            return null;
        }
        return null;
    }
}
