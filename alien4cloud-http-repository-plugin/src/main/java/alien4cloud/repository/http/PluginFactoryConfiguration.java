package alien4cloud.repository.http;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;

@Configuration
@ImportResource("classpath:alien-properties-config.xml")
public class PluginFactoryConfiguration {

    @Bean(name = "configurable-http-artifact-resolver")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ConfigurableHttpArtifactResolver configurableHttpArtifactResolver() {
        return new ConfigurableHttpArtifactResolver();
    }

    @Bean(name = "configurable-http-artifact-resolver-factory")
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ConfigurableHttpArtifactResolverFactory configurableHttpArtifactResolverFactory() {
        return new ConfigurableHttpArtifactResolverFactory();
    }

    @Bean(name = "http-artifact-resolver")
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public HttpArtifactResolver httpArtifactResolver() {
        return new HttpArtifactResolver();
    }
}
