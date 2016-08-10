package alien4cloud.repository.http;

import java.io.InputStream;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import alien4cloud.component.repository.IConfigurableArtifactResolver;
import alien4cloud.repository.exception.ResolverNotConfiguredException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigurableHttpArtifactResolver implements IConfigurableArtifactResolver<HttpArtifactResolverConfiguration> {

    @Resource
    private HttpArtifactResolver httpArtifactResolver;

    private HttpArtifactResolverConfiguration configuration;

    private HttpArtifactResolverConfiguration getMandatoryConfiguration() {
        if (configuration == null) {
            throw new ResolverNotConfiguredException("HTTP resolver is not configured, please call setConfiguration first");
        }
        return configuration;
    }

    @Override
    public void setConfiguration(HttpArtifactResolverConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String getResolverType() {
        return httpArtifactResolver.getResolverType();
    }

    @Override
    public boolean canHandleArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        if (!httpArtifactResolver.canHandleArtifact(artifactReference, repositoryURL, repositoryType, credentials)) {
            return false;
        }
        String configuredURL = getMandatoryConfiguration().getUrl();
        if (StringUtils.isNotBlank(repositoryURL) && !repositoryURL.equals(configuredURL)) {
            // The artifact's repository url is configured and it's not equals to the configured URL of the resolver
            return false;
        }
        // By default if the user used short notation artifact_name: https://abcd.com/my_script.sh check if the reference contains repository's url
        return artifactReference.startsWith(configuredURL);
    }

    @Override
    public InputStream resolveArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        if (!canHandleArtifact(artifactReference, repositoryURL, repositoryType, credentials)) {
            return null;
        }
        HttpArtifactResolverConfiguration preConfiguration = getMandatoryConfiguration();
        if (StringUtils.isBlank(credentials) && preConfiguration.getUser() != null) {
            credentials = preConfiguration.getUser() + ":" + preConfiguration.getPassword();
        }
        return httpArtifactResolver.resolveArtifact(artifactReference, repositoryURL, repositoryType, credentials);
    }
}
