package com.bookstores.service;

import com.bookstores.DTO.AdminViewModels;
import com.bookstores.DTO.AdminBookUpsertRequest;
import com.bookstores.DTO.AdminCategoryUpsertRequest;
import com.bookstores.DTO.AdminOrderUpsertRequest;
import com.bookstores.DTO.AdminUserUpsertRequest;
import com.bookstores.DTO.BookApproveRequest;
import com.bookstores.DTO.BookDTO;
import com.bookstores.DTO.CategoryListDTO;
import com.bookstores.DTO.OrderDTO;
import com.bookstores.DTO.UserDTO;
import java.util.List;

public interface AdminService {

    List<UserDTO> listUsers();

    UserDTO createUser(AdminUserUpsertRequest req);

    UserDTO updateUser(Integer id, AdminUserUpsertRequest req);

    UserDTO lockUser(Integer id);

    void deleteUser(Integer id);

    List<CategoryListDTO> listCategories();

    CategoryListDTO createCategory(AdminCategoryUpsertRequest req);

    CategoryListDTO updateCategory(Integer id, AdminCategoryUpsertRequest req);

    void deleteCategory(Integer id);

    List<BookDTO> listBooks();

    BookDTO createBook(AdminBookUpsertRequest req);

    BookDTO updateBook(Integer id, AdminBookUpsertRequest req);

    void deleteBook(Integer id);

    List<BookDTO> pendingBooks();

    BookDTO approveBook(Integer id, BookApproveRequest req);

    OrderDTO createOrder(AdminOrderUpsertRequest req);

    OrderDTO updateOrder(Integer id, AdminOrderUpsertRequest req);

    void deleteOrder(Integer id);

    List<AdminViewModels.OrderRow> listOrdersForAdmin();

    List<AdminViewModels.ReviewRow> listReviewsForAdmin();

    AdminViewModels.DashboardOverview dashboardOverview();
}
