package com.bookstores.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "tblorder")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "orderDate")
    private LocalDateTime orderDate;

    @Column(name = "totalAmount", nullable = false)
    private Double totalAmount;

    @Column(length = 50)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(length = 255)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucherID")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @OneToMany(mappedBy = "order")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<OrderItem> orderItems;
}
