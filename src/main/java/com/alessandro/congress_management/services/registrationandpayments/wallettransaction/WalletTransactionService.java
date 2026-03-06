package com.alessandro.congress_management.services.registrationandpayments.wallettransaction;

import com.alessandro.congress_management.dto.registrationandpayments.wallettransaction.WalletBalanceResponse;
import com.alessandro.congress_management.dto.registrationandpayments.wallettransaction.WalletRechargeRequest;
import com.alessandro.congress_management.dto.registrationandpayments.wallettransaction.WalletTransactionCreate;
import com.alessandro.congress_management.dto.registrationandpayments.wallettransaction.WalletTransactionResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.registrations_and_payments.WalletTransactionEntity;

import java.util.List;

public interface WalletTransactionService {

    WalletTransactionEntity createTransaction(WalletTransactionCreate transaction) throws NotFoundException, BusinessRuleException;

    WalletTransactionResponse transactionRecharge(Long idUser, WalletRechargeRequest request) throws NotFoundException, BusinessRuleException;

    WalletBalanceResponse getWalletBalance(Long idUser) throws NotFoundException;

    List<WalletTransactionResponse> getWalletTransactions(Long idUser) throws NotFoundException;

}
