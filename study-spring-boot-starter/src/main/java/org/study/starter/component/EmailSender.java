package org.study.starter.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.study.common.statics.exceptions.BizException;

import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 邮件发送器
 */
public class EmailSender {
    private Logger logger = LoggerFactory.getLogger(EmailSender.class);
    private final static ConcurrentHashMap<String, JavaMailSender> MAIL_SENDER = new ConcurrentHashMap();

    public EmailSender(Map<String, JavaMailSender> senderMap){
        MAIL_SENDER.putAll(senderMap);
    }

    public boolean sendTextMail(String from, String to, String[] cc, String subject, String content){
        JavaMailSender sender = MAIL_SENDER.get(from);
        if(sender == null){
            throw new BizException(from + " 没有此邮件发送者的配置");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        if(cc != null && cc.length > 0){
            message.setCc(cc);
        }

        try {
            sender.send(message);
            return true;
        } catch (Throwable e) {
            logger.error("from={} to={} subject={} content={} 发送Text邮件时发生异常", from, to, subject, content, e);
            return false;
        }
    }

    public boolean sendHtmlMail(String from, String to, String[] cc, String subject, String content) {
        JavaMailSender sender = MAIL_SENDER.get(from);
        if (sender == null) {
            throw new BizException(from + " 没有此邮件发送者的配置");
        }

        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
            messageHelper.setFrom(from);
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(content, true);
            if (cc != null && cc.length > 0) {
                messageHelper.setCc(cc);
            }
            sender.send(message);
            return true;
        } catch (Exception e) {
            logger.error("from={} to={} subject={} content={} 发送HTML邮件时发生异常", from, to, subject, content, e);
            return false;
        }
    }
}
