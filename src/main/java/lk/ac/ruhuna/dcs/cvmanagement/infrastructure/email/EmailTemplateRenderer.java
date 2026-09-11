package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.email;

import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * Renders both HTML and plain-text bodies for transactional OTP messages.
 *
 * <p>The only interpolated values are the OTP code (always numeric, system-generated) and the
 * expiry minute count (an integer), so HTML injection is not a concern here — both are validated
 * server-side before this class ever sees them. A plain-text alternative is still generated
 * alongside the HTML body so the message is sent as multipart/alternative, which improves
 * deliverability and gives email clients that block HTML a readable fallback.
 */
@Component
public class EmailTemplateRenderer {

    private static final String SIGN_UP = "SIGN_UP";
    private static final String PASSWORD_RESET = "PASSWORD_RESET";

    public String subjectFor(String purpose) {
        return switch (purpose) {
            case SIGN_UP -> "Verify your CV Management account";
            case PASSWORD_RESET -> "Reset your CV Management password";
            default -> "Your CV Management verification code";
        };
    }

    private static long minutesUntil(Instant expiresAt, Instant now) {
        return Math.max(1, Duration.between(now, expiresAt).toMinutes());
    }

    private static String actionFor(String purpose) {
        return switch (purpose) {
            case SIGN_UP -> "complete your account registration";
            case PASSWORD_RESET -> "reset your password";
            default -> "verify your identity";
        };
    }

    /**
     * Plain-text alternative part. Kept in sync with {@link #otpBodyHtml} so both parts of the
     * multipart message say the same thing.
     */
    public String otpBody(String purpose, String otp, Instant expiresAt, Instant now) {
        long minutes = minutesUntil(expiresAt, now);
        String action = actionFor(purpose);

        return """
                Department of Computer Science
                University of Ruhuna \u2014 CV Management

                Use the following verification code to %s:

                    %s

                This code expires in %d minute(s). Do not share it with anyone.

                If you did not request this, you can safely ignore this email \u2014 no
                changes have been made to your account.

                This is an automated message. Please do not reply.
                """
            .formatted(action, otp, minutes);
    }

    /**
     * HTML body. Table-based layout with fully inlined CSS so it renders consistently across
     * Gmail, Outlook (Word rendering engine) and university webmail clients.
     */
    public String otpBodyHtml(String purpose, String otp, Instant expiresAt, Instant now) {
        long minutes = minutesUntil(expiresAt, now);
        String action = actionFor(purpose);

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <meta name="color-scheme" content="light">
                <meta name="supported-color-schemes" content="light">
                <title>%s</title>
                </head>
                <body style="margin:0; padding:0; width:100%%; background-color:#F2F0FA; -webkit-text-size-adjust:100%%;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" bgcolor="#F2F0FA" style="background-color:#F2F0FA;">
                <tr><td align="center" style="padding:32px 12px;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" style="max-width:520px; width:100%%;">

                <tr>
                <td align="left" style="padding:0 8px 18px 8px; font-family:Roboto,'Segoe UI',Arial,sans-serif;">
                <div style="font-size:14px; font-weight:700; color:#2E2856;">CV Management System</div>
                <div style="font-size:12px; color:#7A7594; padding-top:2px;">Department of Computer Science, University of Ruhuna</div>
                </td>
                </tr>

                <tr>
                <td bgcolor="#FFFFFF" style="background-color:#FFFFFF; border:1px solid #E4E0F2; border-radius:14px; padding:32px 28px; font-family:Roboto,'Segoe UI',Arial,sans-serif;">

                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                <tr><td style="font-size:20px; font-weight:700; color:#2E2856;">%s</td></tr>
                <tr><td style="padding-top:10px; font-size:14px; line-height:22px; color:#4A4568;">
                Use the code below to %s.
                </td></tr>

                <tr><td style="padding-top:24px;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0" bgcolor="#F0EDFB" style="background-color:#F0EDFB; border:1px solid #DBD4F5; border-radius:10px;">
                <tr><td align="center" style="padding:20px 14px;">
                <div style="font-size:12px; color:#6B6590;">Your verification code</div>
                <div style="padding-top:8px; font-size:30px; font-weight:700; color:#2E2856; letter-spacing:7px; font-family:Consolas,'Courier New',monospace;">%s</div>
                </td></tr>
                </table>
                </td></tr>

                <tr><td style="padding-top:18px; font-size:13px; line-height:20px; color:#4A4568;">
                This code expires in %d minute(s). Do not share it with anyone, including staff.
                </td></tr>

                <tr><td style="padding-top:20px;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                <tr><td height="1" bgcolor="#EDEAF7" style="background-color:#EDEAF7; height:1px; font-size:0;">&nbsp;</td></tr>
                </table>
                </td></tr>

                <tr><td style="padding-top:16px; font-size:12px; line-height:19px; color:#7A7594;">
                If you did not request this, you can safely ignore this email \u2014 no changes have been made to your account.
                </td></tr>

                </table>
                </td>
                </tr>

                <tr>
                <td align="left" style="padding:16px 8px 0 8px; font-family:Roboto,'Segoe UI',Arial,sans-serif; font-size:11px; color:#8B85A6;">
                This is an automated message. Please do not reply.
                </td>
                </tr>

                </table>
                </td></tr>
                </table>
                </body>
                </html>
                """
            .formatted(subjectFor(purpose), subjectFor(purpose), action, otp, minutes);
    }
}
