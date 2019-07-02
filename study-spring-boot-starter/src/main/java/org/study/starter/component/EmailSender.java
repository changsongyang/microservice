package org.study.starter.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * 邮件发送器
 */
public class EmailSender {
    private Logger logger = LoggerFactory.getLogger(EmailSender.class);

    private String mailFrom;
    private JavaMailSender mailSender;

    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }



    public void sendSimpleMail(String receiver, String subject, String content){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(this.mailFrom);
        message.setTo(receiver);
        message.setSubject(subject);
        message.setText(content);
        try {
            mailSender.send(message);
        } catch (Throwable e) {
            logger.error("receiver={} subject={} content={} 发送邮件时发生异常", receiver, subject, content, e);
        }
    }

    public void sendSimpleMail(String[] receivers, String subject, String content){
        int len = receivers==null?0:receivers.length;
        for(int i=0; i<len; i++){
            this.sendSimpleMail(receivers[i], subject, content);
        }
    }
}
