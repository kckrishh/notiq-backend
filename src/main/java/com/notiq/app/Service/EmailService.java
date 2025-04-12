package com.notiq.app.Service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
  private final JavaMailSender mailSender;

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void sendForgetPassEmail(String toEmail, String token) {
    String link = "http://localhost:4200/auth/forgot-password?token=" + token;

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);

      helper.setTo(toEmail);
      helper.setSubject("Reset your Notiq password");

      String html = """
          <html>
            <head>
              <style>
                @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600&family=Poppins:wght@600;700&display=swap');
              </style>
            </head>
           <body style="margin: 0; padding: 2rem; background-color: #F5F7FA; font-family: 'Inter', sans-serif; color: #333333;">
              <div style="max-width: 500px; margin: auto; margin-top: 0; background-color: #FFFFFF; padding: 2rem; border-radius: 1rem; box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);">

                <!-- Logo -->
                <div style="text-align: center; margin-top: -4rem; margin-bottom: 0.2rem; background: transparent; position: relative">
                  <img src='cid:logoImage' style="height: 360px; position: absolute;" alt="Notiq logo" />
                </div>

                <!-- Heading -->
                <h2 style="font-family: 'Poppins', sans-serif; font-size: 1.8rem; font-weight: 700; color: #2C2C2C; text-align: center; margin-top: -6.5rem;">
                  Reset your password
                </h2>


                <!-- Body -->
                <p style="margin-top: 1rem; font-size: 1rem;">
                  Hey 👋, we received a request to reset your <strong>Notiq</strong> account password.
                </p>
                <p style="margin-top: 0.5rem; font-size: 1rem;">
                  Click the button below to choose a new password:
                </p>

                <!-- Button -->
                <div style="text-align: center; margin: 2rem 0;">
                  <a href="%s"
                     style="background-color: #14433B; color: #FFFFFF; text-decoration: none;
                            padding: 12px 24px; border-radius: 8px; font-weight: 600; font-family: 'Poppins', sans-serif;
                            display: inline-block;">
                    Reset Password
                  </a>
                </div>

                <p style="font-size: 0.85rem; color: #666666;">
                  If you didn’t request this, just ignore this email — your password won’t change.
                </p>

                <p style="margin-top: 2rem; font-size: 0.75rem; color: #999999; text-align: center;">
                  &copy; 2025 Notiq. All rights reserved.
                </p>
              </div>
            </body>
          </html>
          """
          .formatted(link);

      helper.setText(html, true);

      // Attach your logo (make sure it's inside `resources/static/logo.png`)
      ClassPathResource logo = new ClassPathResource("/static/logo.png");
      helper.addInline("logoImage", logo);

      mailSender.send(message);
    } catch (MessagingException e) {
      throw new RuntimeException("Failed to send verification email", e);
    }
  }

  public void sendVerificationEmail(String toEmail, String token) {
    String link = "http://localhost:8080/auth/confirm?token=" + token;
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);

      helper.setTo(toEmail);
      helper.setSubject("Verify your notiq account");
      String html = """
          <html>
            <head>
              <style>
                @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600&family=Poppins:wght@600;700&display=swap');
              </style>
            </head>
            <body style="margin: 0; padding: 2rem; background-color: #F5F7FA; font-family: 'Inter', sans-serif; color: #333333;">
              <div style="max-width: 500px; margin: auto; margin-top: 0; background-color: #FFFFFF; padding: 2rem; border-radius: 1rem; box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);">

                <!-- Logo -->
                <div style="text-align: center; margin-top: -4rem; margin-bottom: 0.2rem; background: transparent; position: relative">
                  <img src='cid:logoImage' style="height: 360px; position: absolute;" alt="Notiq logo" />
                </div>

                <!-- Heading -->
                <h2 style="font-family: 'Poppins', sans-serif; font-size: 1.8rem; font-weight: 700; color: #2C2C2C; text-align: center; margin-top: -3.5rem;">
                  Verify your email
                </h2>

                <!-- Body -->
                <p style="margin-top: 1rem; font-size: 1rem; color: #333333;">
                  Hey 👋, thanks for joining <strong>Notiq</strong> – your secure note-taking companion.
                </p>
                <p style="margin-top: 0.5rem; font-size: 1rem; color: #333333;">
                  Click the button below to verify your email address and activate your account:
                </p>

                <!-- CTA Button -->
                <div style="text-align: center; margin: 2rem 0;">
                  <a href="%s"
                     style="background-color: #14433B; color: #FFFFFF; text-decoration: none;
                            padding: 12px 24px; border-radius: 8px; font-weight: 600; font-family: 'Poppins', sans-serif;
                            display: inline-block;">
                    Verify Email
                  </a>
                </div>

                <p style="font-size: 0.85rem; color: #666666;">
                  Didn’t request this? No worries — just ignore this email and your account won’t be created.
                </p>
              </div>
            </body>
          </html>
          """
          .formatted(link);

      helper.setText(html, true);

      ClassPathResource logo = new ClassPathResource("/static/logo.png");
      helper.addInline("logoImage", logo);

      mailSender.send(message);

    } catch (MessagingException e) {
      throw new RuntimeException("Failed to send verification email", e);
    }
  }
}
