package alien4cloud.repository.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

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
import org.springframework.beans.factory.annotation.Value;

import alien4cloud.component.repository.IArtifactResolver;
import alien4cloud.repository.util.ResolverUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpArtifactResolver implements IArtifactResolver {

    private Path tempDir;

    @Value("${directories.alien}/${directories.upload_temp}")
    public void setTempDir(String tempDir) throws IOException {
        this.tempDir = ResolverUtil.createPluginTemporaryDownloadDir(tempDir, "artifacts");
    }

    private CloseableHttpClient httpclient = HttpClients.custom().setRedirectStrategy(new DefaultRedirectStrategy()).build();

    @Override
    public String getResolverType() {
        return "http";
    }

    private boolean isHttpURL(String reference) {
        return reference != null && (reference.startsWith("http://") || reference.startsWith("https://"));
    }

    @Override
    public boolean canHandleArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        if (!ResolverUtil.isResolverTypeCompatible(this, repositoryType)) {
            return false;
        }
        if (!ResolverUtil.isCredentialsBasicUserPassword(credentials)) {
            // Must have credentials as user:password
            return false;
        }
        // If repository is defined then it must be a http url
        if (StringUtils.isNotBlank(repositoryURL)) {
            // If reference is a http url then it must have base path the repository's url
            return isHttpURL(repositoryURL) && (!isHttpURL(artifactReference) || artifactReference.startsWith(repositoryURL));
        } else {
            // Repository is blank then artifact reference must be a http url
            return isHttpURL(artifactReference);
        }
    }

    Path doResolveArtifact(String artifactReference, String repositoryURL, String credentials) {
        String getURL = null;
        try {
            if (isHttpURL(artifactReference)) {
                getURL = artifactReference;
            } else {
                getURL = repositoryURL;
                if (StringUtils.isNotBlank(artifactReference)) {
                    getURL += "/" + artifactReference;
                }
            }
            HttpClientContext context = null;
            if (StringUtils.isNotBlank(credentials)) {
                int indexOfSeparator = credentials.indexOf(':');
                // Prepare basic login
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(credentials.substring(0, indexOfSeparator),
                        credentials.substring(indexOfSeparator + 1, credentials.length())));
                // Add AuthCache to the execution context
                context = HttpClientContext.create();
                context.setCredentialsProvider(credentialsProvider);
            }
            // Launch HTTP Get
            HttpGet httpget = new HttpGet(getURL);
            CloseableHttpResponse response = httpclient.execute(httpget, context);
            if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
                log.info("Received status {} while trying to download artifact at {}", response.getStatusLine(), getURL);
                response.close();
                return null;
            } else {
                try (InputStream artifactStream = response.getEntity().getContent()) {
                    return ResolverUtil.copyArtifactStreamToTempFile(artifactReference, artifactStream, tempDir);
                }
            }
        } catch (ClientProtocolException e) {
            log.info("Error downloading artifact at " + getURL, e);
            return null;
        } catch (IOException e) {
            log.warn("Error downloading artifact at " + getURL, e);
            return null;
        }
    }

    @Override
    public Path resolveArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        if (!canHandleArtifact(artifactReference, repositoryURL, repositoryType, credentials)) {
            return null;
        } else {
            return doResolveArtifact(artifactReference, repositoryURL, credentials);
        }
    }
}
