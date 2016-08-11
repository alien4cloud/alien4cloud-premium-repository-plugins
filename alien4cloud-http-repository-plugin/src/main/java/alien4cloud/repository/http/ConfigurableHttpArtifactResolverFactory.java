package alien4cloud.repository.http;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;

import alien4cloud.component.repository.IConfigurableArtifactResolver;
import alien4cloud.component.repository.IConfigurableArtifactResolverFactory;

public class ConfigurableHttpArtifactResolverFactory implements IConfigurableArtifactResolverFactory<HttpArtifactResolverConfiguration> {

    @Resource
    private ApplicationContext factoryContext;

    @Resource
    private HttpArtifactResolver httpArtifactResolver;

    @Override
    public IConfigurableArtifactResolver<HttpArtifactResolverConfiguration> newInstance() {
        return factoryContext.getBean(ConfigurableHttpArtifactResolver.class);
    }

    @Override
    public Class<HttpArtifactResolverConfiguration> getResolverConfigurationType() {
        return HttpArtifactResolverConfiguration.class;
    }

    @Override
    public String getResolverType() {
        return httpArtifactResolver.getResolverType();
    }
}
