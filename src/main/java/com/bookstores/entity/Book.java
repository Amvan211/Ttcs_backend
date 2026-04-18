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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "tblbook")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 100)
    private String author;

    @Column(nullable = false)
    private Double price;

    @Column(name = "stockQuantity")
    private Integer stockQuantity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "coverImageUrl", length = 500)
    private String coverImageUrl;


    @Column(name = "approvalStatus", length = 20)
    private String approvalStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryID")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partnerID")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Partner partner;

    @OneToMany(mappedBy = "book")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "book")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<UserBehavior> userBehaviors;

    @OneToMany(mappedBy = "book")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Review> reviews;

    @PrePersist
    public void prePersistApproval() {
        if (approvalStatus == null || approvalStatus.isBlank()) {
            approvalStatus = "APPROVED";
        }
    }
}
