package com.example.ProjectTeam121.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendActivationEmail(String toEmail, String subject, String activationUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            Context context = new Context();
            context.setVariable("activationLink", activationUrl);

            String html =
                    "<!DOCTYPE html>" +
                            "<html lang=\"vi\">" +
                            "<head>" +
                            "    <meta charset=\"UTF-8\" />" +
                            "    <title>Kích hoạt tài khoản</title>" +
                            "    <style>" +
                            "        body { font-family: 'Segoe UI', sans-serif; background-color: #f6f7f9; padding: 0; margin: 0; }" +
                            "        .container { max-width: 600px; background: #ffffff; margin: 40px auto; padding: 30px; border-radius: 16px; border: 1px solid #eee; box-shadow: 0 8px 18px rgba(0, 0, 0, 0.06); }" +
                            "        h2 { color: #c8102e; text-align: center; }" +
                            "        .logo { text-align: center; font-size: 32px; font-weight: 900; color: #fff; width: 80px; height: 80px; background: linear-gradient(135deg, #b11226, #e61b36); border-radius: 20px; line-height: 80px; margin: 0 auto 20px; }" +
                            "        .footer { margin-top: 30px; font-size: 13px; text-align: center; color: #777; }" +
                            "    </style>" +
                            "</head>" +
                            "<body>" +
                            "<div class=\"container\">" +
                            "    <div class=\"logo\">PTIT</div>" +
                            "    <h2>PTIT IoT Platform</h2>" +
                            "    <p>Xin chào,</p>" +
                            "    <p>Cảm ơn bạn đã đăng ký tài khoản. Vui lòng nhấn nút bên dưới để kích hoạt tài khoản của bạn:</p>" +

                            "    <a href=\"" + activationUrl + "\" target=\"_blank\" style=\"display:inline-block;padding:12px 24px;background:#E53935;color:#fff;text-decoration:none;border-radius:8px;font-weight:bold;\">" +
                            "       KÍCH HOẠT TÀI KHOẢN" +
                            "    </a>" +

                            "    <p>Liên kết sẽ hết hạn sau <b>24 giờ</b>.</p>" +
                            "    <div class=\"footer\">" +
                            "        Email được gửi tự động – vui lòng không trả lời email này.<br />" +
                            "        © 2025 PTIT IoT Platform." +
                            "    </div>" +
                            "</div>" +
                            "</body>" +
                            "</html>";

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setFrom("noreply@projectteam121.com");

            mailSender.send(message);
            log.info("Activation email sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send activation email", e);
        }
    }

    @Async
    public void sendResetPasswordEmail(String toEmail, String subject, String resetUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            Context context = new Context();
            context.setVariable("resetLink", resetUrl);

            String html = templateEngine.process("email/reset-password-email.html", context);

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setFrom("noreply@projectteam121.com");

            mailSender.send(message);
            log.info("Reset password email sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send reset password email", e);
        }
    }

    @Async
    public void sendReactivationEmail(String toEmail, String subject, String reactivateUrl) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlMsg = "<h3>Chào mừng trở lại!</h3>"
                    + "<p>Bạn đã yêu cầu mở lại tài khoản (hoặc kích hoạt lại tài khoản đã vô hiệu hóa).</p>"
                    + "<p>Vui lòng nhấn vào đường dẫn bên dưới để kích hoạt lại ngay:</p>"
                    + "<a href=\"" + reactivateUrl + "\">MỞ KHÓA TÀI KHOẢN</a>"
                    + "<p>Link này có hiệu lực trong 24 giờ.</p>";

            helper.setText(htmlMsg, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setFrom("noreply@projectteam121.com");

            mailSender.send(mimeMessage);
            log.info("Reactivation email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send email", e);
            throw new IllegalStateException("Failed to send email");
        }
    }

    @Async
    public void sendTemplateEmail(String to, String subject, String template, Map<String, Object> model) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            Context context = new Context();
            context.setVariables(model);

            String html = templateEngine.process(template, context);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setFrom("noreply@projectteam121.com");

            mailSender.send(message);
            log.info("Email sent to {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send email", e);
        }
    }


}