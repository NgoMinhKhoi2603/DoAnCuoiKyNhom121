package com.example.ProjectTeam121.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendActivationEmail(String toEmail, String subject, String activationUrl) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlMsg = "<h3>Xin chào!</h3>"
                    + "<p>Cảm ơn bạn đã đăng ký tài khoản. Vui lòng nhấn vào link bên dưới để kích hoạt tài khoản:</p>"
                    + "<a href=\"" + activationUrl + "\">KÍCH HOẠT TÀI KHOẢN</a>"
                    + "<p>Link này sẽ hết hạn sau 24 giờ.</p>";

            helper.setText(htmlMsg, true); // true = html
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setFrom("noreply@projectteam121.com"); // Email người gửi giả định

            mailSender.send(mimeMessage);
            log.info("Activation email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send email", e);
            throw new IllegalStateException("Failed to send email");
        }
    }
}