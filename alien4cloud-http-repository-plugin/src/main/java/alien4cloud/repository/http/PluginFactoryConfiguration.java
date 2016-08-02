package alien4cloud.repository.http;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class PluginFactoryConfiguration {

    @Bean(name = "http-artifact-resolver")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public HttpArtifactResolver httpArtifactResolver() {
        return new HttpArtifactResolver();
    }

}
