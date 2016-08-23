package alien4cloud.repository.http;

import static alien4cloud.repository.http.HttpUtil.isHttpURL;

import java.nio.file.Path;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import alien4cloud.component.repository.IConfigurableArtifactResolver;
import alien4cloud.component.repository.exception.InvalidResolverConfigurationException;
import alien4cloud.repository.model.ValidationResult;
import alien4cloud.repository.model.ValidationStatus;
import alien4cloud.repository.util.ResolverUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigurableHttpArtifactResolver implements IConfigurableArtifactResolver<HttpArtifactResolverConfiguration> {

    @Resource
    private HttpArtifactResolver httpArtifactResolver;

    private HttpArtifactResolverConfiguration configuration;

    public HttpArtifactResolverConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(HttpArtifactResolverConfiguration configuration) {
        if (!isHttpURL(configuration.getUrl())) {
            throw new InvalidResolverConfigurationException("Resolver's configuration is incorrect, URL must be defined and begins with 'http' or 'https'");
        }
        this.configuration = configuration;
    }

    @Override
    public ValidationResult canHandleArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        if (StringUtils.isNotBlank(repositoryURL) && !repositoryURL.equals(ResolverUtil.getMandatoryConfiguration(this).getUrl())) {
            return new ValidationResult(ValidationStatus.INVALID_REPOSITORY_URL, "Artifact's repository's URL does not match configuration");
        } else {
            return httpArtifactResolver.canHandleArtifact(artifactReference, repositoryURL, repositoryType,
                    ResolverUtil.getConfiguredCredentials(this, credentials));
        }
    }

    private ValidationResult validateArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        if (StringUtils.isNotBlank(repositoryURL) && !repositoryURL.equals(ResolverUtil.getMandatoryConfiguration(this).getUrl())) {
            return new ValidationResult(ValidationStatus.INVALID_REPOSITORY_URL, "Artifact's repository's URL does not match configuration");
        } else {
            return httpArtifactResolver.validateArtifact(artifactReference, repositoryURL, repositoryType, credentials);
        }
    }

    @Override
    public Path resolveArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        if (!validateArtifact(artifactReference, repositoryURL, repositoryType, credentials).equals(ValidationResult.SUCCESS)) {
            return null;
        }
        return httpArtifactResolver.doResolveArtifact(artifactReference, repositoryURL, ResolverUtil.getConfiguredCredentials(this, credentials));
    }
}
