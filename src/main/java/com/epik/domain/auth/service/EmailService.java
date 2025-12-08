package com.epik.domain.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.mail.username}")
    private String fromEmail;

    private final JavaMailSender mailSender;

    /**
     * 비밀번호 재설정 이메일을 발송한다.
     *
     * @param email 수신자 이메일
     * @param token 비밀번호 재설정 토큰
     * @throws RuntimeException 이메일 발송 실패 시
     */
    public void sendPasswordResetEmail(String email, String token) {

        log.info("[Email][PasswordReset] 이메일 발송 요청 - to={}, token={}", email, token);

        try {
            // TODO: 링크 설정파일로 빼기
            String resetDeepLink = "epik://reset-password?token=" + token;
            log.debug("[Email][PasswordReset] DeepLink 생성 - link={}", resetDeepLink);

            String htmlContent = createPasswordResetHtml(resetDeepLink);
            log.debug("[Email][PasswordReset] HTML 템플릿 생성 완료 (길이: {} bytes)",
                    htmlContent.length());

            String subject = "[EPIK] 비밀번호 재설정 안내";

            sendHtmlEmail(email, subject, htmlContent);

            log.info("[Email][PasswordReset] 이메일 발송 성공 - to={}", email);

        } catch (MessagingException e) {
            log.error("[Email][PasswordReset] 이메일 발송 실패 - to={}, token={}, 원인={}",
                    email, token, e.getMessage(), e);

            // 에러를 위로 던져 PasswordService에서 캐치하도록 유지
            throw new RuntimeException("이메일 발송에 실패했습니다", e);
        }
    }

    /**
     * HTML 형식의 이메일을 발송한다.
     *
     * @param to 수신자 이메일
     * @param subject 이메일 제목
     * @param htmlContent HTML 본문 내용
     * @throws MessagingException 이메일 생성 또는 발송 실패 시
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        log.debug("[Email] MimeMessage 생성 시작");

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        log.debug("[Email] MimeMessage 설정 완료 - from={}, to={}, subject={}",
                fromEmail, to, subject);

        mailSender.send(message);

        log.debug("[Email] mailSender.send() 호출 완료");
    }

    /**
     * 비밀번호 재설정 이메일 HTML 템플릿을 생성한다.
     *
     * @param resetLink 비밀번호 재설정 링크
     * @return HTML 형식의 이메일 본문
     */
    private String createPasswordResetHtml(String resetLink) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
        </head>
        <body style="margin: 0; padding: 0; background-color: #f5f5f5; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">
            <div style="max-width: 600px; margin: 40px auto; background-color: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); overflow: hidden;">
                
                <!-- 헤더 -->
                <div style="padding: 40px 40px 0 40px; text-align: center;">
                    <h1 style="color: #D72424; font-size: 24px; font-weight: 600; margin: 0;">EPIK</h1>
                </div>
                
                <!-- 본문 -->
                <div style="padding: 40px 40px 60px 40px; text-align: center;">
                    <h2 style="color: #2d3436; font-size: 22px; font-weight: 600; margin: 0 0 30px 0;">
                        비밀번호를 잊으셨나요?
                    </h2>
                    
                    <p style="color: #636e72; font-size: 15px; line-height: 1.6; margin: 20px 0;">
                        안녕하세요, EPIK입니다.<br>
                        아래 버튼을 눌러 새 비밀번호를 설정해주시기 바랍니다.
                    </p>
                    
                    <!-- 버튼 -->
                    <div style="margin: 40px 0;">
                        <a href="%s" 
                           style="display: inline-block; 
                                  background: #D72424;
                                  color: white; 
                                  padding: 16px 48px; 
                                  text-decoration: none; 
                                  border-radius: 8px;
                                  font-size: 16px;
                                  font-weight: 600;
                                  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);">
                            비밀번호 재설정
                        </a>
                    </div>
                    
                    <p style="color: #b2bec3; font-size: 13px; line-height: 1.6; margin: 30px 0 0 0;">
                        인증코드는 이메일 발송 시점으로부터 30분 동안 유효합니다.
                    </p>
                </div>
                
                <!-- 푸터 -->
                <div style="background-color: #f8f9fa; padding: 30px 40px; border-top: 1px solid #e9ecef;">
                    <p style="color: #868e96; font-size: 12px; line-height: 1.6; margin: 0 0 10px 0;">
                        본 메일은 서비스 이용 및 약관 고지사항 안내를 위한 메일로, 수신 동의 여부와 관계없이 발송되었습니다.
                    </p>
                    <p style="color: #868e96; font-size: 12px; line-height: 1.6; margin: 0 0 10px 0;">
                        발송 대상 선정과 발송 시점의 차이로 인해 일부 후에도 메일을 수신하실 수 있는 점 양해 부탁드립니다.
                    </p>
                    <p style="color: #868e96; font-size: 12px; line-height: 1.6; margin: 0;">
                        본 메일은 발신 전용이므로 회신이 불가합니다.
                    </p>
                </div>
                
            </div>
        </body>
        </html>
        """.formatted(resetLink);
    }
}
