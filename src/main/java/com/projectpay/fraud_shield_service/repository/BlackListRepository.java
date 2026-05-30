package com.projectpay.fraud_shield_service.repository;

import com.projectpay.fraud_shield_service.entities.UserAccountsStatus;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlackListRepository extends JpaRepository<@NonNull UserAccountsStatus,@NonNull Long> {
}
