package alien4cloud.repository.maven;

import static alien4cloud.repository.maven.MavenUtil.isValidMavenRepository;

import java.nio.file.Path;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import alien4cloud.component.repository.IConfigurableArtifactResolver;
import alien4cloud.component.repository.exception.InvalidResolverConfigurationException;
import alien4cloud.repository.model.ValidationResult;
import alien4cloud.repository.model.ValidationStatus;
import alien4cloud.repository.util.ResolverUtil;

public class ConfigurableMavenArtifactResolver implements IConfigurableArtifactResolver<MavenArtifactResolverConfiguration> {

    private MavenArtifactResolverConfiguration configuration;

    @Resource
    private MavenArtifactResolver mavenArtifactResolver;

    @Override
    public MavenArtifactResolverConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public void setConfiguration(MavenArtifactResolverConfiguration configuration) {
        if (!isValidMavenRepository(configuration.getUrl())) {
            throw new InvalidResolverConfigurationException("Resolver's configuration is incorrect, URL must be defined and be valid absolute URI");
        }
        this.configuration = configuration;
    }

    @Override
    public ValidationResult canHandleArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        if (StringUtils.isNotBlank(repositoryURL) && !repositoryURL.equals(ResolverUtil.getMandatoryConfiguration(this).getUrl())) {
            return new ValidationResult(ValidationStatus.INVALID_REPOSITORY_URL, "Artifact's repository's URL does not match configuration");
        } else {
            return mavenArtifactResolver.canHandleArtifact(artifactReference, ResolverUtil.getMandatoryConfiguration(this).getUrl(), repositoryType,
                    ResolverUtil.getConfiguredCredentials(this, credentials));
        }
    }

    private ValidationResult validateArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        if (StringUtils.isNotBlank(repositoryURL) && !repositoryURL.equals(ResolverUtil.getMandatoryConfiguration(this).getUrl())) {
            return new ValidationResult(ValidationStatus.INVALID_REPOSITORY_URL, "Artifact's repository's URL does not match configuration");
        } else {
            return mavenArtifactResolver.validateArtifact(artifactReference, ResolverUtil.getMandatoryConfiguration(this).getUrl(), repositoryType,
                    credentials);
        }
    }

    @Override
    public Path resolveArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        if (!validateArtifact(artifactReference, repositoryURL, repositoryType, credentials).equals(ValidationResult.SUCCESS)) {
            return null;
        }
        return mavenArtifactResolver.doResolveArtifact(artifactReference, ResolverUtil.getMandatoryConfiguration(this).getUrl(),
                ResolverUtil.getConfiguredCredentials(this, credentials));
    }
}
