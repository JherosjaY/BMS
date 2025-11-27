package com.example.blottermanagementsystem.config;

import java.util.Properties;

/**
 * ✅ EMAIL CONFIGURATION
 * SMTP settings for Gmail auto-email service
 */
public class EmailConfig {
    
    // Gmail SMTP Configuration
    public static final String SMTP_HOST = "smtp.gmail.com";
    public static final int SMTP_PORT = 587;
    public static final String EMAIL_USERNAME = "official.bms.2025@gmail.com";
    public static final String EMAIL_PASSWORD = "bvgm vyes knki yvgi"; // App password
    public static final String FROM_EMAIL = "official.bms.2025@gmail.com";
    
    /**
     * Get SMTP Properties for JavaMail
     */
    public static Properties getSMTPProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        return props;
    }
}
