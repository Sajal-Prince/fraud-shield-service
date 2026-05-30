package com.projectpay.fraud_shield_service.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class UserAccountsStatus {
    @Id
    private Long userId;
    private AccountStatus accountStatus;
}
