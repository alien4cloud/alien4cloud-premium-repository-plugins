package alien4cloud.repository.http;

import javax.validation.constraints.NotNull;

import alien4cloud.ui.form.annotation.FormPassword;
import alien4cloud.ui.form.annotation.FormProperties;
import alien4cloud.ui.form.annotation.FormValidValues;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@FormProperties({ "url", "user", "password", "authenticationMode" })
public class HttpArtifactResolverConfiguration {

    @NotNull
    private String url;

    private String user;

    @FormPassword
    private String password;

    @NotNull
    @FormValidValues("BASIC")
    private String authenticationMode;
}
