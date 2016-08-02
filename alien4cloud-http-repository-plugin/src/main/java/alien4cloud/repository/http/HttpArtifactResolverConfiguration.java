package alien4cloud.repository.http;

import alien4cloud.ui.form.annotation.FormProperties;
import alien4cloud.ui.form.annotation.FormPropertyConstraint;
import alien4cloud.ui.form.annotation.FormPropertyDefinition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@FormProperties({ "url", "user", "password", "authenticationMode" })
public class HttpArtifactResolverConfiguration {

    @FormPropertyDefinition(type = "string", isRequired = true, constraints = @FormPropertyConstraint(pattern = "^(http://|https://)+.+$"))
    private String url;

    @FormPropertyDefinition(type = "string", isRequired = true, constraints = @FormPropertyConstraint(minLength = 1))
    private String user;

    @FormPropertyDefinition(type = "string", isRequired = true, isPassword = true, constraints = @FormPropertyConstraint(minLength = 1))
    private String password;

    @FormPropertyDefinition(type = "string", isRequired = true, defaultValue = "BASIC", constraints = @FormPropertyConstraint(validValues = { "BASIC" }))
    private String authenticationMode;
}
