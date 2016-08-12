package alien4cloud.repository.git;

import javax.validation.constraints.NotNull;

import alien4cloud.ui.form.annotation.FormPassword;
import alien4cloud.ui.form.annotation.FormProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@FormProperties({ "url", "user", "password" })
public class GitArtifactResolverConfiguration {

    @NotNull
    private String url;

    @NotNull
    private String user;

    @NotNull
    @FormPassword
    private String password;
}
