package com.bookmyshow.catalog.entity;

import com.bookmyshow.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "venues", indexes = {
        @Index(name = "idx_venues_city", columnList = "city_id"),
        @Index(name = "idx_venues_name", columnList = "name"),
        @Index(name = "idx_venues_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venue extends BaseEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "city_id", nullable = false, columnDefinition = "UUID")
    private UUID cityId;
    
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "address", nullable = false, length = 500)
    private String address;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;
    
    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;
    
    @Column(name = "amenities", columnDefinition = "TEXT")
    private String amenities;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}