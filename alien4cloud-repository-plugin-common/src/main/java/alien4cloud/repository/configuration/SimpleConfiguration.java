package alien4cloud.repository.configuration;

import javax.validation.constraints.NotNull;

import alien4cloud.ui.form.annotation.FormPassword;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class SimpleConfiguration {

    @NotNull
    private String url;

    private String user;

    @FormPassword
    private String password;
}
