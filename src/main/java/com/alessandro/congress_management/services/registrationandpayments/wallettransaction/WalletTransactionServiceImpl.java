package com.alessandro.congress_management.services.registrationandpayments.wallettransaction;

import com.alessandro.congress_management.dto.registrationandpayments.wallettransaction.WalletBalanceResponse;
import com.alessandro.congress_management.dto.registrationandpayments.wallettransaction.WalletRechargeRequest;
import com.alessandro.congress_management.dto.registrationandpayments.wallettransaction.WalletTransactionCreate;
import com.alessandro.congress_management.dto.registrationandpayments.wallettransaction.WalletTransactionResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.registrations_and_payments.WalletTransactionEntity;
import com.alessandro.congress_management.repositories.authenticate.UserRepository;
import com.alessandro.congress_management.repositories.wallettransaction.WalletTransactionRepository;
import com.alessandro.congress_management.services.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WalletTransactionServiceImpl implements WalletTransactionService{
    private final WalletTransactionRepository walletTransactionRepository;
    private final String WALLET_TRANSACTION_RECHARGE = "RECHARGE";
    private final String WALLET_TRANSACTION_PAYMENT = "PAYMENT";
    private final UserService userService;
    private final UserRepository userRepository;

    public WalletTransactionServiceImpl(WalletTransactionRepository walletTransactionRepository, UserService userService, UserRepository userRepository) {
        this.walletTransactionRepository = walletTransactionRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletTransactionEntity createTransaction(WalletTransactionCreate transaction) throws NotFoundException, BusinessRuleException {
        WalletTransactionEntity entity = new WalletTransactionEntity();
        entity.setUser(transaction.getUser());
        entity.setAmount(transaction.getAmount());
        entity.setDescription(transaction.getDescription());
        entity.setTransactionDate(transaction.getTransactionDate());
        entity.setTransactionType(transaction.getTransactionType());
        entity.setRelatedRegistration(transaction.getRelatedRegistration());


        // Actualizar el balance del usuario segun el tipo de transacción
        if (WALLET_TRANSACTION_RECHARGE.equals(transaction.getTransactionType())) {
            UserEntity user = transaction.getUser();
            user.setWalletBalance(user.getWalletBalance().add(transaction.getAmount()));
            userRepository.save(user);
        }else if (WALLET_TRANSACTION_PAYMENT.equals(transaction.getTransactionType())) {
            UserEntity user = transaction.getUser();
            if (user.getWalletBalance().compareTo(transaction.getAmount()) < 0) {
                throw new BusinessRuleException("Insufficient balance for payment");
            }
            user.setWalletBalance(user.getWalletBalance().subtract(transaction.getAmount()));
            userRepository.save(user);
        } else {
            throw new BusinessRuleException("Invalid transaction type");
        }
        WalletTransactionEntity savedEntity = walletTransactionRepository.save(entity);

        return savedEntity;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletTransactionResponse transactionRecharge(Long idUser, WalletRechargeRequest request) throws NotFoundException, BusinessRuleException {
        UserEntity user = userService.getUserById(idUser);
        WalletTransactionCreate transactionCreate = new WalletTransactionCreate(
                user,
                WALLET_TRANSACTION_RECHARGE,
                request.getDescription(),
                null,
                request.getTransactionDate(),
                request.getAmount()
        );
        WalletTransactionEntity transactionEntity = createTransaction(transactionCreate);
        return WalletTransactionResponse.fromEntity(transactionEntity);
    }

    @Override
    public WalletBalanceResponse getWalletBalance(Long idUser) throws NotFoundException {
        UserEntity user = userService.getUserById(idUser);
        return WalletBalanceResponse.fromEntity(user);
    }

    @Override
    public List<WalletTransactionResponse> getWalletTransactions(Long idUser) throws NotFoundException {
        UserEntity user = userService.getUserById(idUser);
        List<WalletTransactionEntity> transactions = walletTransactionRepository.findByUser(user);
        return transactions.stream().map(WalletTransactionResponse::fromEntity).toList();
    }
}
