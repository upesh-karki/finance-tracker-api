package com.upkdev.financialtracker.domain.account.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "financial_institution", schema = "ref")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FinancialInstitution {
    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "country")
    private String country;

    @Column(name = "is_active")
    private Boolean isActive;
}
