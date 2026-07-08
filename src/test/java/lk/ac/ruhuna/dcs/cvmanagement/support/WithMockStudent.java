package lk.ac.ruhuna.dcs.cvmanagement.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Meta-annotation that configures a mock student user for security-aware tests.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithMockUser(username = "sc2020001@dcs.ruh.ac.lk", roles = "STUDENT")
public @interface WithMockStudent {
}
