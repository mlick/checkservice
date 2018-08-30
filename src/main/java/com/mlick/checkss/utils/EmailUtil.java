package com.mlick.checkss.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;


public class EmailUtil {
    private static final Logger log = LoggerFactory.getLogger(EmailUtil.class);
    /**
     * 邮件服务的地址/ip
     */
    private static String mailServerHost;

    /**
     * 配置是否需要验证
     */
    private static String mailSmtpAuth;

    /**
     * 解码规则
     */
    private static String mailSmtpCharset;

    /**
     * 邮件服务器用来验证的账户
     */
    private static String systemUserName;

    /**
     * 邮件服务器用来验证的密码
     */
    private static String systemUserPassword;

    /**
     * 主发送人
     */
    private static String[] tos;
    /**
     * 抄送人
     */
    private static String[] ccs;


    /**
     * 初始化参数
     */
    static {
        init();
    }

    private static void init() {
        Properties prop = new Properties();
        try {
            InputStream in = new FileInputStream(System.getProperty("user.dir") + "/eMail.properties");
            prop.load(in);
            in.close();
            mailServerHost = prop.getProperty("mail.smtp.host");
            mailSmtpAuth = prop.getProperty("mail.smtp.auth");
            mailSmtpCharset = prop.getProperty("mail.smtp.charset");
            systemUserName = prop.getProperty("mail.system.userName");
            systemUserPassword = prop.getProperty("mail.system.password");
            tos = prop.getProperty("mail.tos").split(";");
            ccs = prop.getProperty("mail.ccs").split(";");
            prop.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getTodayTime() {
        DateFormat df = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
        return df.format(new Date());
    }

    public static boolean sendMail(String expStr) {
        String sb = "&nbsp;&nbsp;相关信息: <font color=\"#0066D5\">" + expStr + "</font></p>" +
                "</p>此邮件为自动发送，请勿回复！</p>";
        return sendMail(tos,//主送
                ccs,//抄送
                null,
                getTodayTime() + "服务器请求异常，请尽快联系相关人进行检查",
                sb,
                null,
                null);
    }

    /**
     * 发送邮件,可带附件
     *
     * @param to：主送
     * @param cc：抄送
     * @param bcc：密送
     * @param subject:主题
     * @param content:内容
     * @param attachFile:附件地址
     * @return true or false
     * @author xal 2013-8-6
     */
    public static boolean sendMail(String to[], String cc[], String bcc[],
                                   String subject, String content, String attachFile[], String fileName) {

        if (mailServerHost == null || systemUserName == null || systemUserPassword == null) {
            init();
        }

        Properties props = new Properties();
        try {
            //是否验证
            props.put("mail.smtp.auth", mailSmtpAuth);
            Session session = Session.getDefaultInstance(props);
            MimeMessage message = new MimeMessage(session);

            //发件人
            message.setFrom(new InternetAddress(systemUserName));

            // 收件人
            if (to != null && to.length > 0) {
                message.setRecipients(Message.RecipientType.TO, getAddress(to));
            } else {
                return false;
            }
            // 设置主题
            message.setSubject(subject, mailSmtpCharset);


            // 抄送
            if (cc != null && cc.length > 0) {
                message.setRecipients(Message.RecipientType.CC, getAddress(cc));
            }

            // 密送
            if (bcc != null && bcc.length > 0) {
                message.setRecipients(Message.RecipientType.BCC, getAddress(bcc));
            }
            // 发送日期
            message.setSentDate(new Date());

            // 判断是否存在附件,决定设置邮件正文的方式
            if (fileName != null || (attachFile != null && attachFile.length > 0)) {
                // MiniMultipart类是一个容器类，包含MimeBodyPart类型的对象
                Multipart mainPart = new MimeMultipart("mixed");

                if (fileName != null) {// 正文的图片部分
                    MimeBodyPart contentBody = new MimeBodyPart();
                    // 用于组合文本和图片，"related"型的MimeMultipart对象
                    MimeMultipart contentMulti = new MimeMultipart("related");
                    // 正文的图片部分
                    MimeBodyPart jpgBody = new MimeBodyPart();
                    FileDataSource fds = new FileDataSource(fileName);
                    jpgBody.setDataHandler(new DataHandler(fds));
                    jpgBody.setContentID("timg");
                    contentMulti.addBodyPart(jpgBody);
                    contentBody.setContent(contentMulti);
                    mainPart.addBodyPart(contentBody);
                }

                // 创建一个包含HTML内容的MimeBodyPart
                MimeBodyPart html = new MimeBodyPart();
                // 设置邮件正文  正文的文本部分
                html.setContent(content, "text/html; charset=utf-8");
                mainPart.addBodyPart(html);

                // 设置附件信息
                if (attachFile != null && attachFile.length > 0)
                    for (int i = 0; i < attachFile.length; i++) {
                        if (attachFile[i] != null && !attachFile[i].equals("")) {
                            html = new MimeBodyPart();
                            // 获得附件
                            DataSource source = new FileDataSource(attachFile[i]);
                            // 设置附件的数据处理器
                            html.setDataHandler(new DataHandler(source));
                            // 设置附件文件名
                            html.setFileName(MimeUtility.encodeText(source
                                    .getName()));
                            // 加入附件
                            mainPart.addBodyPart(html);
                        }
                    }

                message.setContent(mainPart);
                message.saveChanges();
            } else {
                // 设置邮件正文,不带附件可通过此方式设置内容
                message.setText(content, mailSmtpCharset);
                //这功能不要被替换掉了是设置格式问题
                // *****给消息对象设置内容 @liu
                BodyPart mdp = new MimeBodyPart();// 新建一个存放信件内容的BodyPart对象
                mdp.setContent(content, "text/html;charset=gb2312");// 给BodyPart对象设置内容和格式/编码方式
                Multipart mm = new MimeMultipart();// 新建一个MimeMultipart对象用来存放BodyPart对
                // 象(事实上可以存放多个)
                mm.addBodyPart(mdp);// 将BodyPart加入到MimeMultipart对象中(可以加入多个BodyPart)
                message.setContent(mm);// 把mm作为消息对象的内容

                message.saveChanges();
                //***** 给消息对象设置内容
            }

            Transport transport = session.getTransport("smtp");
            // 连接邮件服务器
            transport.connect(mailServerHost, systemUserName, systemUserPassword);

            // 执行发送
            transport.sendMessage(message, message.getAllRecipients());

            return true;
        } catch (Exception e) {
            log.debug("邮件发送失败", e);
            e.printStackTrace();
            return false;
        }
    }


    // 解析地址
    private static Address[] getAddress(String mail[]) {
        Address address[] = new Address[mail.length];
        for (int i = 0; i < mail.length; i++) {
            try {
                address[i] = new InternetAddress(mail[i]);
            } catch (AddressException e) {
                e.printStackTrace();
            }
        }
        return address;
    }


//    public static void main(String[] args) {
//        StringBuffer sb = new StringBuffer();
//        sb.append("尊敬的用户：</p>");
//        sb.append("&nbsp;&nbsp;您好!</p>");
//        sb.append("&nbsp;&nbsp;本邮件为: <font color=\"#0066D5\">").append("测试邮件,可以忽略").append("</font></p>");
//        sb.append("</p>此邮件为自动发送，请勿回复！</p>");
//
//        System.out.println(sb.toString());
//
//        EmailUtil.sendMail(sb.toString());
//    }

}
