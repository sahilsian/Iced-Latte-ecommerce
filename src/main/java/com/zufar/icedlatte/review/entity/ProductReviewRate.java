package com.zufar.icedlatte.review.entity;

import com.zufar.icedlatte.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product_reviews_rates")
public class ProductReviewRate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", referencedColumnName = "id")
    private ProductReview review;

    @Column(name = "isLike", nullable = false)
    private Boolean isLike;

    @Override
    public String toString() {
        return "Product Review Rate Entry {" +
                "id=" + id +
                '}';
    }
}
