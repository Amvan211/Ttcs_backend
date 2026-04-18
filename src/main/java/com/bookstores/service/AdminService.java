package com.bookstores.service;

import com.bookstores.DTO.AdminViewModels;
import com.bookstores.DTO.BookApproveRequest;
import com.bookstores.DTO.BookDTO;
import com.bookstores.DTO.UserDTO;
import java.util.List;

public interface AdminService {

    List<UserDTO> listUsers();

    UserDTO lockUser(Integer id);

    List<BookDTO> pendingBooks();

    BookDTO approveBook(Integer id, BookApproveRequest req);

    List<AdminViewModels.OrderRow> listOrdersForAdmin();

    List<AdminViewModels.ReviewRow> listReviewsForAdmin();

    AdminViewModels.DashboardOverview dashboardOverview();
}
