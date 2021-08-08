package gov.dhs.cisa.ctm.flare.client.api.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import gov.dhs.cisa.ctm.flare.client.api.config.Constants;

import java.util.Optional;

/**
 * Implementation of AuditorAware based on Spring Security.
 */
@SuppressWarnings({"NullableProblems", "unused"})
@Component
class SpringSecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of(SecurityUtils.getCurrentUserLogin().orElse(Constants.SYSTEM_ACCOUNT));
    }
}
