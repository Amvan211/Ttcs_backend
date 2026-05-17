package com.bookstores.service;

import com.bookstores.entity.Order;
import com.bookstores.entity.OrderItem;
import com.bookstores.entity.Partner;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendOrderConfirmationEmail(Order order) {
        if (order.getEmail() == null || order.getEmail().isBlank()) {
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(order.getEmail());
            helper.setSubject("Xác nhận đơn hàng #" + order.getId() + " - The Archive");

            double totalWithShipping = order.getTotalAmount() + 30000; // Flat shipping fee 30k
            String momoTransferContent = "THEARCHIVE-ORD-" + order.getId();
            
            // Build MoMo QR Code URL
            String rawQrData = String.format("MOMO|amount=%.0f|content=%s|merchant=THE_ARCHIVE", 
                    totalWithShipping, momoTransferContent);
            String encodedQrData = URLEncoder.encode(rawQrData, StandardCharsets.UTF_8.toString());
            String qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=260x260&data=" + encodedQrData;

            // Generate HTML Content
            StringBuilder html = new StringBuilder();
            html.append("<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #eee; border-radius: 12px; background-color: #fcfcfc;\">");
            html.append("<h2 style=\"color: #6750A4; text-align: center; border-bottom: 2px solid #6750A4; padding-bottom: 10px;\">Cảm ơn bạn đã mua sắm tại The Archive!</h2>");
            html.append("<p>Chào khách hàng,</p>");
            html.append("<p>Đơn hàng <strong>#").append(order.getId()).append("</strong> của bạn đã được tiếp nhận thành công. Dưới đây là thông tin chi tiết:</p>");
            
            html.append("<table style=\"width: 100%; border-collapse: collapse; margin: 20px 0;\">");
            html.append("<thead><tr style=\"background-color: #f2f2f2;\"><th style=\"text-align: left; padding: 8px; border-bottom: 1px solid #ddd;\">Sách</th><th style=\"text-align: center; padding: 8px; border-bottom: 1px solid #ddd;\">Số lượng</th><th style=\"text-align: right; padding: 8px; border-bottom: 1px solid #ddd;\">Đơn giá</th></tr></thead>");
            html.append("<tbody>");
            
            for (OrderItem item : order.getOrderItems()) {
                html.append("<tr>");
                html.append("<td style=\"padding: 8px; border-bottom: 1px solid #ddd;\">").append(item.getBook().getTitle()).append("</td>");
                html.append("<td style=\"text-align: center; padding: 8px; border-bottom: 1px solid #ddd;\">").append(item.getQuantity()).append("</td>");
                html.append("<td style=\"text-align: right; padding: 8px; border-bottom: 1px solid #ddd;\">").append(String.format("%,.0fđ", item.getPrice())).append("</td>");
                html.append("</tr>");
            }
            
            html.append("</tbody></table>");

            html.append("<div style=\"text-align: right; margin-bottom: 20px; font-size: 16px;\">");
            html.append("<p>Tạm tính: <strong>").append(String.format("%,.0fđ", order.getTotalAmount())).append("</strong></p>");
            html.append("<p>Phí vận chuyển: <strong>30,000đ</strong></p>");
            html.append("<p style=\"font-size: 18px; color: #6750A4;\">Tổng cộng: <strong>").append(String.format("%,.0fđ", totalWithShipping)).append("</strong></p>");
            html.append("</div>");

            html.append("<div style=\"text-align: center; margin-top: 30px; padding: 20px; border: 1px dashed #6750A4; border-radius: 8px; background-color: #f7f5fa;\">");
            html.append("<h3 style=\"color: #6750A4; margin-top: 0;\">Hướng dẫn thanh toán MoMo</h3>");
            html.append("<p>Quét mã QR dưới đây để tiến hành chuyển khoản thanh toán cho đơn hàng:</p>");
            html.append("<img src=\"").append(qrCodeUrl).append("\" alt=\"Mã QR MoMo\" style=\"width: 200px; height: 200px; border: 1px solid #ddd; padding: 5px; background: white; border-radius: 8px;\" />");
            html.append("<p style=\"margin-top: 15px; font-size: 14px;\">Nội dung chuyển khoản: <strong style=\"color: #e91e63;\">").append(momoTransferContent).append("</strong></p>");
            html.append("<p style=\"font-size: 12px; color: #666;\">(Vui lòng giữ nguyên nội dung chuyển khoản để hệ thống tự động duyệt đơn hàng nhanh nhất)</p>");
            html.append("</div>");

            html.append("<p style=\"margin-top: 30px; font-size: 12px; color: #999; text-align: center;\">Đây là email tự động từ hệ thống The Archive. Vui lòng không trả lời email này.</p>");
            html.append("</div>");

            helper.setText(html.toString(), true);
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendOrderDeliveredEmail(Order order) {
        String targetEmail = order.getEmail();
        if (targetEmail == null || targetEmail.isBlank()) {
            if (order.getUser() != null) {
                targetEmail = order.getUser().getMail();
            }
        }
        if (targetEmail == null || targetEmail.isBlank()) {
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(targetEmail);
            helper.setSubject("Đơn hàng #" + order.getId() + " đã được giao thành công - The Archive");

            StringBuilder html = new StringBuilder();
            html.append("<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #eee; border-radius: 12px; background-color: #fcfcfc;\">");
            html.append("<h2 style=\"color: #2e7d32; text-align: center; border-bottom: 2px solid #2e7d32; padding-bottom: 10px;\">Giao hàng thành công!</h2>");
            html.append("<p>Chào bạn,</p>");
            html.append("<p>Đơn hàng <strong>#").append(order.getId()).append("</strong> của bạn tại <strong>The Archive</strong> đã được giao thành công.</p>");
            
            html.append("<div style=\"background-color: #e8f5e9; border-left: 4px solid #2e7d32; padding: 15px; margin: 20px 0; border-radius: 4px;\">");
            html.append("<p style=\"margin: 0; font-weight: bold; color: #2e7d32;\">Trạng thái đơn hàng: Đã giao thành công</p>");
            html.append("<p style=\"margin: 5px 0 0 0; font-size: 14px; color: #555;\">Cảm ơn bạn đã tin tưởng và đồng hành cùng chúng tôi. Hy vọng bạn sẽ có những trải nghiệm đọc sách tuyệt vời!</p>");
            html.append("</div>");

            html.append("<h3 style=\"color: #333;\">Thông tin chi tiết đơn hàng:</h3>");
            html.append("<table style=\"width: 100%; border-collapse: collapse; margin: 15px 0;\">");
            html.append("<thead><tr style=\"background-color: #f2f2f2;\"><th style=\"text-align: left; padding: 8px; border-bottom: 1px solid #ddd;\">Sách</th><th style=\"text-align: center; padding: 8px; border-bottom: 1px solid #ddd;\">Số lượng</th><th style=\"text-align: right; padding: 8px; border-bottom: 1px solid #ddd;\">Đơn giá</th></tr></thead>");
            html.append("<tbody>");
            
            if (order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    html.append("<tr>");
                    html.append("<td style=\"padding: 8px; border-bottom: 1px solid #ddd;\">").append(item.getBook().getTitle()).append("</td>");
                    html.append("<td style=\"text-align: center; padding: 8px; border-bottom: 1px solid #ddd;\">").append(item.getQuantity()).append("</td>");
                    html.append("<td style=\"text-align: right; padding: 8px; border-bottom: 1px solid #ddd;\">").append(String.format("%,.0fđ", item.getPrice())).append("</td>");
                    html.append("</tr>");
                }
            }
            
            html.append("</tbody></table>");

            html.append("<div style=\"text-align: right; margin-bottom: 20px; font-size: 16px;\">");
            html.append("<p>Tổng cộng: <strong style=\"font-size: 18px; color: #2e7d32;\">").append(String.format("%,.0fđ", order.getTotalAmount() + 30000)).append("</strong> (Đã bao gồm 30,000đ phí ship)</p>");
            html.append("</div>");

            html.append("<p style=\"margin-top: 30px; font-size: 12px; color: #999; text-align: center;\">Đây là email tự động từ hệ thống The Archive. Vui lòng không trả lời email này.</p>");
            html.append("</div>");

            helper.setText(html.toString(), true);
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendPartnerApprovalEmail(Partner partner) {
        if (partner.getUser() == null || partner.getUser().getMail() == null || partner.getUser().getMail().isBlank()) {
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(partner.getUser().getMail());
            helper.setSubject("Chúc mừng! Đăng ký trở thành Đối tác của bạn đã được phê duyệt - The Archive");

            StringBuilder html = new StringBuilder();
            html.append("<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #eee; border-radius: 12px; background-color: #fcfcfc;\">");
            html.append("<h2 style=\"color: #6750A4; text-align: center; border-bottom: 2px solid #6750A4; padding-bottom: 10px;\">Chào mừng Đối tác mới!</h2>");
            html.append("<p>Chào <strong>").append(partner.getUser().getFullName() != null && !partner.getUser().getFullName().isBlank() ? partner.getUser().getFullName() : partner.getUser().getUsername()).append("</strong>,</p>");
            html.append("<p>Yêu cầu đăng ký trở thành Đối tác của cửa hàng sách <strong>").append(partner.getStoreName()).append("</strong> đã được Ban quản trị <strong>The Archive</strong> chính thức phê duyệt.</p>");
            
            html.append("<div style=\"background-color: #f3e5f5; border-left: 4px solid #6750A4; padding: 15px; margin: 20px 0; border-radius: 4px;\">");
            html.append("<p style=\"margin: 0; font-weight: bold; color: #6750A4;\">Thông tin cửa hàng đối tác:</p>");
            html.append("<p style=\"margin: 5px 0 0 0; font-size: 14px; color: #333;\"><strong>Tên cửa hàng:</strong> ").append(partner.getStoreName()).append("</p>");
            html.append("<p style=\"margin: 5px 0 0 0; font-size: 14px; color: #333;\"><strong>Địa chỉ:</strong> ").append(partner.getAddress()).append("</p>");
            html.append("<p style=\"margin: 5px 0 0 0; font-size: 14px; color: #333;\"><strong>Mô tả:</strong> ").append(partner.getDescription() != null ? partner.getDescription() : "-").append("</p>");
            html.append("</div>");

            html.append("<p>Bây giờ bạn đã có quyền truy cập vào các tính năng dành riêng cho Đối tác (Partner) như:</p>");
            html.append("<ul style=\"color: #333; line-height: 1.6;\">");
            html.append("<li>Quản lý danh mục sách và kho hàng của riêng bạn</li>");
            html.append("<li>Đăng bán sách mới lên nền tảng The Archive</li>");
            html.append("<li>Xem thống kê doanh thu và quản lý đơn hàng khách mua từ cửa hàng của bạn</li>");
            html.append("</ul>");
            
            html.append("<p style=\"text-align: center; margin-top: 30px;\">");
            html.append("<a href=\"http://localhost:3000/\" style=\"background-color: #6750A4; color: white; padding: 12px 24px; text-decoration: none; border-radius: 30px; font-weight: bold; display: inline-block; box-shadow: 0 4px 6px rgba(103, 80, 164, 0.2);\">Truy cập The Archive ngay</a>");
            html.append("</p>");

            html.append("<p style=\"margin-top: 30px; font-size: 12px; color: #999; text-align: center;\">Đây là email tự động từ hệ thống The Archive. Vui lòng không trả lời email này.</p>");
            html.append("</div>");

            helper.setText(html.toString(), true);
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
