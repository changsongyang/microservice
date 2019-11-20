package com.xpay.starter.comp.config;

import com.xpay.starter.comp.component.EmailSender;
import com.xpay.starter.comp.properties.MailProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@ConditionalOnProperty(prefix = "email", name = "enable", havingValue = "true")
@ConditionalOnClass(JavaMailSenderImpl.class)
@EnableConfigurationProperties(MailProperties.class)
@Configuration
public class EmailAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public EmailSender emailSender(MailProperties properties){
        Map<String, JavaMailSender> map = new HashMap<>();
        for(MailProperties.Mailer mailer : properties.getMailers()){
            JavaMailSender sender = buildJavaMailSender(mailer);
            map.put(mailer.getUsername(), sender);
        }
        return new EmailSender(map);
    }

    private JavaMailSender buildJavaMailSender(MailProperties.Mailer mailer) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(mailer.getHost());
        if (mailer.getPort() != null) {
            sender.setPort(mailer.getPort());
        }
        sender.setUsername(mailer.getUsername());
        sender.setPassword(mailer.getPassword());
        sender.setProtocol(mailer.getProtocol());
        if (mailer.getDefaultEncoding() != null) {
            sender.setDefaultEncoding(mailer.getDefaultEncoding().name());
        }
        if (!mailer.getProperties().isEmpty()) {
            Properties properties = new Properties();
            properties.putAll(mailer.getProperties());
            sender.setJavaMailProperties(properties);
        }
        return sender;
    }
}
