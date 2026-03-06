package com.alessandro.congress_management.repositories.wallettransaction;

import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.registrations_and_payments.WalletTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransactionEntity, Long> {
    List<WalletTransactionEntity> findByUser(UserEntity user);
}
