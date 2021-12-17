package com.bcmc.xor.flare.client.api.config;

import org.bson.Document;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.ParseException;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Locale;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(new BsonDocumentFormatter());
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setUseIsoFormat(true);
        registrar.registerFormatters(registry);
    }

    public class BsonDocumentFormatter implements Formatter<Document> {

        @Override
        public Document parse(String s, Locale locale) throws ParseException {
            return Document.parse(s);
        }

        @Override
        public String print(Document document, Locale locale) {
            return document.toJson();
        }
    }
}
