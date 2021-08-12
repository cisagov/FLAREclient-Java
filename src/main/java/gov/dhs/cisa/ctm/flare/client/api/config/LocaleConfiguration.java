package gov.dhs.cisa.ctm.flare.client.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * Not putting time into fixing this as we don't use it
 */
//@Configuration
public class LocaleConfiguration implements WebMvcConfigurer {

//    @Bean(name = "localeResolver")
//    public LocaleResolver localeResolver() {
//        AngularCookieLocaleResolver cookieLocaleResolver = new AngularCookieLocaleResolver();
//        cookieLocaleResolver.setCookieName("NG_TRANSLATE_LANG_KEY");
//        return cookieLocaleResolver;
//    }
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
//        localeChangeInterceptor.setParamName("language");
//        registry.addInterceptor(localeChangeInterceptor);
//    }
}
