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
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;

import alien4cloud.component.repository.IArtifactResolver;
import alien4cloud.repository.exception.ResolverNotConfiguredException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpArtifactResolver implements IArtifactResolver<HttpArtifactResolverConfiguration> {

    private CloseableHttpClient httpclient;

    private HttpArtifactResolverConfiguration configuration;

    @Override
    public String getResolverType() {
        return "http";
    }

    private HttpArtifactResolverConfiguration getMandatoryConfiguration() {
        if (configuration == null) {
            throw new ResolverNotConfiguredException("HTTP resolver is not configured, please call setConfiguration first");
        }
        return configuration;
    }

    @Override
    public void setConfiguration(HttpArtifactResolverConfiguration configuration) {
        this.configuration = configuration;
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(configuration.getUser(), configuration.getPassword()));
        httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).setRedirectStrategy(new DefaultRedirectStrategy()).build();
    }

    @Override
    public Class<HttpArtifactResolverConfiguration> getResolverConfigurationType() {
        return HttpArtifactResolverConfiguration.class;
    }

    @Override
    public boolean canHandleArtifact(String artifactReference, String repositoryURL, String repositoryType) {
        if (StringUtils.isNotBlank(repositoryType) && !repositoryType.equals(getResolverType())) {
            return false;
        }
        String configuredURL = getMandatoryConfiguration().getUrl();
        if (StringUtils.isNotBlank(repositoryURL) && !repositoryURL.equals(configuredURL)) {
            return false;
        } else {
            // By default if the user used short notation artifact_name: https://abcd.com/my_script.sh check if the reference contains repository's url
            return artifactReference.startsWith(configuredURL);
        }
    }

    private boolean isAbsoluteURL(String reference) {
        return reference.startsWith("http://") || reference.startsWith("https://");
    }

    @Override
    public InputStream resolveArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        String getURL = null;
        try {
            if (isAbsoluteURL(artifactReference)) {
                getURL = artifactReference;
            } else {
                getURL = repositoryURL + "/" + URLEncoder.encode(artifactReference, "UTF-8");
            }
            HttpGet httpget = new HttpGet(getURL);
            CloseableHttpResponse response = httpclient.execute(httpget);
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
