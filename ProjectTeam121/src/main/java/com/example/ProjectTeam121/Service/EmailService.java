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

    @Async
    public void sendResetPasswordEmail(String toEmail, String subject, String resetUrl) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlMsg = "<h3>Yêu cầu đặt lại mật khẩu</h3>"
                    + "<p>Bạn đã yêu cầu đặt lại mật khẩu. Vui lòng nhấn vào link bên dưới để tiếp tục:</p>"
                    + "<a href=\"" + resetUrl + "\">ĐẶT LẠI MẬT KHẨU</a>"
                    + "<p>Link này sẽ hết hạn sau 24 giờ. Nếu bạn không yêu cầu, vui lòng bỏ qua email này.</p>";

            helper.setText(htmlMsg, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setFrom("noreply@projectteam121.com");

            mailSender.send(mimeMessage);
            log.info("Reset password email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send email", e);
            throw new IllegalStateException("Failed to send email");
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
}