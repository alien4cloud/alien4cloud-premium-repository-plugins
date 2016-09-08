package alien4cloud.repository.maven;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;

import alien4cloud.component.repository.IConfigurableArtifactResolver;
import alien4cloud.component.repository.IConfigurableArtifactResolverFactory;

public class ConfigurableMavenArtifactResolverFactory implements IConfigurableArtifactResolverFactory<MavenArtifactResolverConfiguration> {

    @Resource
    private ApplicationContext factoryContext;

    @Resource
    private MavenArtifactResolver mavenArtifactResolver;

    @Override
    public IConfigurableArtifactResolver<MavenArtifactResolverConfiguration> newInstance() {
        return factoryContext.getBean(ConfigurableMavenArtifactResolver.class);
    }

    @Override
    public Class<MavenArtifactResolverConfiguration> getResolverConfigurationType() {
        return MavenArtifactResolverConfiguration.class;
    }

    @Override
    public String getResolverType() {
        return mavenArtifactResolver.getResolverType();
    }
}
