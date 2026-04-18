package com.bookstores.DTO;

import java.util.List;

/**
 * Các kiểu dữ liệu trả về cho khu vực admin — gom một file để giảm số lớp DTO rời.
 * JSON giữ nguyên tên trường như trước (Jackson serialize record theo tên thành phần).
 */
public final class AdminViewModels {

    private AdminViewModels() {}

    public record SalesPoint(String name, double total) {}

    public record OrderRow(
            String id,
            String customer,
            String type,
            String date,
            String total,
            String status,
            String note) {}

    public record ReviewRow(
            String id,
            String user,
            String avatar,
            String book,
            int rating,
            String comment,
            String date,
            String status) {}

    public record TopBook(int id, String title, String author, long sales, String price, String image) {}

    public record DashboardOverview(
            List<SalesPoint> salesByDay, List<OrderRow> recentOrders, List<TopBook> topBooks) {}
}
