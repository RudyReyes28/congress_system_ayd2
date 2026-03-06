package com.alessandro.congress_management.controllers.registrationandpayments.wallettransaction;

import com.alessandro.congress_management.dto.registrationandpayments.wallettransaction.WalletBalanceResponse;
import com.alessandro.congress_management.dto.registrationandpayments.wallettransaction.WalletRechargeRequest;
import com.alessandro.congress_management.dto.registrationandpayments.wallettransaction.WalletTransactionCreate;
import com.alessandro.congress_management.dto.registrationandpayments.wallettransaction.WalletTransactionResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.security.SecurityUtils;
import com.alessandro.congress_management.services.registrationandpayments.wallettransaction.WalletTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.EntityResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Wallet Transactions", description = "Endpoints for managing wallet transactions, including balance inquiries and transaction history.")
public class WalletTransactionController {

    private final WalletTransactionService walletTransactionService;


    public WalletTransactionController(WalletTransactionService walletTransactionService) {
        this.walletTransactionService = walletTransactionService;
    }

    @PostMapping("/wallet/recharge")
    @Operation(summary = "Create a new wallet transaction for a user", description = "Allows users to create a new wallet transaction, such as a recharge or payment. The transaction will update the user's wallet balance accordingly.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Wallet transaction created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid transaction data"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<WalletTransactionResponse> createWalletRecharge( @RequestBody @Valid WalletRechargeRequest request) throws BusinessRuleException, NotFoundException {
        Long userId = SecurityUtils.getCurrentUserId();
        WalletTransactionResponse response = walletTransactionService.transactionRecharge(userId, request);
        return ResponseEntity.status(201).body(response);

    }

    @GetMapping("/wallet/balance")
    @Operation(summary = "Get user's wallet balance", description = "Retrieves the current wallet balance for a specific user, along with their basic information.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wallet balance retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<WalletBalanceResponse> getWalletBalance() throws NotFoundException {
        Long userId = SecurityUtils.getCurrentUserId();
        WalletBalanceResponse response = walletTransactionService.getWalletBalance(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/transactions")
    @Operation(summary = "Get user's wallet transaction history", description = "Retrieves a list of all wallet transactions for a specific user, including details such as amount, description, date, and transaction type.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wallet transaction history retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<WalletTransactionResponse>> getWalletTransactions() throws NotFoundException {
        Long userId = SecurityUtils.getCurrentUserId();
        List<WalletTransactionResponse> response = walletTransactionService.getWalletTransactions(userId);
        return ResponseEntity.ok(response);
    }
}
