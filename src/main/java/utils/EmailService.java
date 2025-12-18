package utils;

import models.Employee;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Email Service for sending payslips to employees
 * Uses JavaMail API to send professional HTML emails
 */
public class EmailService {

    // Email configuration (use company email or Gmail)
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static String SENDER_EMAIL = "hr@company.com";
    private static String SENDER_PASSWORD = "your-app-password";
    private static String COMPANY_NAME = "ABC Corporation";

    /**
     * Configure email settings (call this on app startup or in settings)
     */
    public static void configure(String senderEmail, String senderPassword, String companyName) {
        SENDER_EMAIL = senderEmail;
        SENDER_PASSWORD = senderPassword;
        COMPANY_NAME = companyName;
    }

    /**
     * Send payslip via email
     */
    public static boolean sendPayslip(Employee employee, String payslipContent,
                                      String month, int year) {

        // Validate employee email
        if (employee.getContactInfo() == null ||
                !employee.getContactInfo().contains("@")) {
            System.out.println(" Invalid email for " + employee.getName());
            return false;
        }

        try {
            // Setup email properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);

            // Create session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                }
            });

            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, COMPANY_NAME + " HR Department"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(employee.getContactInfo()));

            message.setSubject("Payslip - " + month + " " + year + " | " + COMPANY_NAME);

            // Create HTML content
            String htmlContent = createHtmlPayslip(employee, payslipContent, month, year);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            // Send email
            Transport.send(message);

            System.out.println(" Payslip emailed to: " + employee.getName() +
                    " (" + employee.getContactInfo() + ")");
            return true;

        } catch (Exception e) {
            System.err.println(" Failed to send email to " + employee.getName() +
                    ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Send payslips to multiple employees
     */
    public static EmailSummary sendBatchPayslips(java.util.List<EmployeePayslip> payslips,
                                                 String month, int year) {
        int sent = 0;
        int failed = 0;
        StringBuilder failedList = new StringBuilder();

        for (EmployeePayslip ep : payslips) {
            boolean success = sendPayslip(ep.employee, ep.payslipContent, month, year);
            if (success) {
                sent++;
            } else {
                failed++;
                failedList.append("  • ").append(ep.employee.getName())
                        .append(" (").append(ep.employee.getContactInfo()).append(")\n");
            }

            // Small delay to avoid spam filters
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return new EmailSummary(sent, failed, failedList.toString());
    }

    /**
     * Create professional HTML email template
     */
    private static String createHtmlPayslip(Employee employee, String payslipContent,
                                            String month, int year) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm a"));

        // Convert plain text payslip to HTML with formatting
        String htmlPayslip = payslipContent
                .replace("\n", "<br>")
                .replace("═", "━")
                .replace("─", "━");

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: 'Segoe UI', Arial, sans-serif;
                        background-color: #f5f5f5;
                        margin: 0;
                        padding: 20px;
                    }
                    .container {
                        max-width: 700px;
                        margin: 0 auto;
                        background: white;
                        border-radius: 10px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        overflow: hidden;
                    }
                    .header {
                        background: linear-gradient(135deg, #2196F3 0%%, #1976D2 100%%);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                    }
                    .header p {
                        margin: 10px 0 0 0;
                        opacity: 0.9;
                    }
                    .content {
                        padding: 30px;
                    }
                    .greeting {
                        font-size: 18px;
                        color: #333;
                        margin-bottom: 20px;
                    }
                    .payslip-box {
                        background: #f8f9fa;
                        border-left: 4px solid #2196F3;
                        padding: 20px;
                        font-family: 'Courier New', monospace;
                        font-size: 13px;
                        line-height: 1.6;
                        white-space: pre-wrap;
                        border-radius: 5px;
                        margin: 20px 0;
                    }
                    .important-note {
                        background: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .footer {
                        background: #f8f9fa;
                        padding: 20px 30px;
                        text-align: center;
                        color: #666;
                        font-size: 12px;
                    }
                    .button {
                        display: inline-block;
                        background: #2196F3;
                        color: white;
                        padding: 12px 30px;
                        text-decoration: none;
                        border-radius: 5px;
                        margin: 20px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1> %s</h1>
                        <p>Human Resources Department</p>
                    </div>
                    
                    <div class="content">
                        <div class="greeting">
                            Dear <strong>%s</strong>,
                        </div>
                        
                        <p>Your payslip for <strong>%s %d</strong> is now available. 
                        Please review the details below:</p>
                        
                        <div class="payslip-box">%s</div>
                        
                        <div class="important-note">
                            <strong>Important:</strong> This is a confidential document. 
                            Please keep it secure and do not share with others.
                        </div>
                        
                        <p>If you have any questions or notice any discrepancies, please contact 
                        the HR department immediately.</p>
                        
                        <p style="margin-top: 30px;">
                            <strong>Best regards,</strong><br>
                            HR Department<br>
                            %s
                        </p>
                    </div>
                    
                    <div class="footer">
                        <p>This is an automated email. Please do not reply to this message.</p>
                        <p>Generated on %s</p>
                        <p style="margin-top: 10px; color: #999;">
                            © %d %s. All rights reserved.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """,
                COMPANY_NAME,
                employee.getName(),
                month, year,
                htmlPayslip,
                COMPANY_NAME,
                timestamp,
                year, COMPANY_NAME
        );
    }

    /**
     * Test email configuration
     */
    public static boolean testEmailConfiguration() {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.timeout", "5000");
            props.put("mail.smtp.connectiontimeout", "5000");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                }
            });

            Transport transport = session.getTransport("smtp");
            transport.connect();
            transport.close();

            System.out.println(" Email configuration is valid!");
            return true;

        } catch (Exception e) {
            System.err.println("Email configuration failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper class for batch operations
     */
    public static class EmployeePayslip {
        public final Employee employee;
        public final String payslipContent;

        public EmployeePayslip(Employee employee, String payslipContent) {
            this.employee = employee;
            this.payslipContent = payslipContent;
        }
    }

    /**
     * Summary of batch email operation
     */
    public static class EmailSummary {
        public final int sent;
        public final int failed;
        public final String failedList;

        public EmailSummary(int sent, int failed, String failedList) {
            this.sent = sent;
            this.failed = failed;
            this.failedList = failedList;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════\n");
            sb.append("   EMAIL BATCH SUMMARY\n");
            sb.append("═══════════════════════════════════════\n\n");
            sb.append(String.format("Successfully sent: %d\n", sent));
            sb.append(String.format("Failed: %d\n", failed));

            if (failed > 0) {
                sb.append("\nFailed recipients:\n");
                sb.append(failedList);
            }

            sb.append("\n═══════════════════════════════════════");
            return sb.toString();
        }
    }
}