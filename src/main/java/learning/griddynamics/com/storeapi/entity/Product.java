package learning.griddynamics.com.storeapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer available;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    // Optimistic locking — protects against overselling on concurrent checkouts
    @Version
    private Long version;
}