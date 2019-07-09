package com.gw.api.gateway.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gw.api.base.config.mail.MailProperties;
import com.gw.api.base.config.mail.Mailer;
import com.gw.api.base.params.RequestParam;
import com.gw.api.base.service.ValidFailService;
import com.gw.api.base.utils.StringUtil;
import com.gw.api.gateway.ratelimit.SimpleRateLimiter;
import com.gw.facade.message.entity.MailParam;
import com.gw.facade.message.enums.EmailSenderTypeEnum;
import com.gw.facade.message.enums.EmailTypeEnum;
import com.gw.facade.message.service.SendEmailFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @description 网关校验失败之后的处理器：如：IP校验、签名校验 等
 * @author: chenyf
 * @Date: 2019-02-20
 */
public class ValidFailServiceImpl implements ValidFailService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MailProperties mailProperties;
    @Autowired
    private SimpleRateLimiter simpleRateLimiter;
    @Reference(check = false)
    private SendEmailFacade sendEmailFacade;

    /**
     * 验签失败之后的处理，如果是发送通知，需注意限制发送通知的频率，如：同一个商户每分钟不超过5次
     * @param requestIp     用户请求的IP
     * @param routeId       定义路由的id
     * @param requestParam  用户的请求数据，当用户没有传入数据时为null
     * @param cause         验签失败的异常，可能为null
     */
    public void afterSignValidFail(String routeId, String requestIp, RequestParam requestParam, Throwable cause){
        Mailer mailer = getMailer(routeId, requestParam.getMethod());
        if(mailer == null){
            logger.info("routeId={} mchNo={} method={} 没有配置预警邮件，将不发送", routeId, requestParam.getMch_no(), requestParam.getMethod());
            return;
        }

        //先判断是否已经已超过最大次数限制，如果是，则直接返回
        if(! this.isAllow(routeId, requestParam, mailer.getSendLimitPerMin())){
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("签名校验失败：")
                .append("mchNo=").append(requestParam.getMch_no())
                .append(", routeId=").append(routeId)
                .append(", method=").append(requestParam.getMethod())
                .append(", requestIp=").append(requestIp);

        this.sendNotifyEmail(mailer, sb.toString());
    }

    /**
     * IP校验失败之后的处理，如果是发送通知，需注意限制发送通知的频率，如：同一个商户每分钟不超过5次
     * @param routeId       定义路由的id
     * @param requestIp     用户请求的IP
     * @param expectIp      实际要求的IP，可能为null
     * @param requestParam  用户的请求数据，当用户没有传入数据时为null
     */
    public void afterIpValidFail(String routeId, String requestIp, String expectIp, RequestParam requestParam){
        Mailer mailer = getMailer(routeId, requestParam.getMethod());
        if(mailer == null){
            logger.info("routeId={} mchNo={} method={} 没有配置预警邮件，将不发送", routeId, requestParam.getMch_no(), requestParam.getMethod());
            return;
        }

        //先判断是否已经已超过最大次数限制，如果是，则直接返回
        if(! this.isAllow(routeId, requestParam, mailer.getSendLimitPerMin())){
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("IP校验失败：")
                .append("mchNo=").append(requestParam.getMch_no())
                .append(", routeId=").append(routeId)
                .append(", method=").append(requestParam.getMethod())
                .append(", requestIp=").append(requestIp)
                .append(", expectIp=").append(expectIp);

        this.sendNotifyEmail(mailer, sb.toString());
    }

    private boolean isAllow(String routeId, RequestParam requestParam, int limit){
        String key = "gatewayValidFail." + routeId + "." + requestParam.getMethod() + "." + requestParam.getMch_no();
        return simpleRateLimiter.isAllow(key, limit, 60 * 1000);
    }

    private void sendNotifyEmail(Mailer mailer, String content){
        String[] cc = null;
        if(StringUtil.isNotEmpty(mailer.getCc())){
            cc = mailer.getCc().split(",");
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("mailContent", content);

        MailParam mailParam = new MailParam();
        mailParam.setSubject(mailer.getSubject());
        mailParam.setToMail(mailer.getReceiver());
        mailParam.setCc(cc);
        mailParam.setHtmlMail(false);
        mailParam.setParamModel(paramMap);
        mailParam.setEmailSenderTypeEnum(EmailSenderTypeEnum.DEFAULT_EMAIL_SENDER);
        mailParam.setEmailTypeEnum(EmailTypeEnum.GATEWAY_VALIDATION_WARNING);

        //后台任务处理
        CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                //还未超过最大次数限制，则发送邮件预警
                sendEmailFacade.sendEmail(mailParam);
            }
        });
    }

    private Mailer getMailer(String routeId, String method){
        if(routeId == null){
            return null;
        }else if(mailProperties == null || mailProperties.getMailers() == null || mailProperties.getMailers().isEmpty()){
            return null;
        }

        for(Mailer mailer : mailProperties.getMailers()){
            if(routeId.equals(mailer.getUid())){
                if(StringUtil.isEmpty(mailer.getMethod())){
                    return mailer;
                }else if(mailer.getMethod().equals(method)){
                    return mailer;
                }
            }
        }
        return null;
    }
}
