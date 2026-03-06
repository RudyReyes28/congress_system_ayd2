package com.alessandro.congress_management.services.registrationandpayments.registration;

import com.alessandro.congress_management.dto.registrationandpayments.registration.MyCongressRegistrationDTO;
import com.alessandro.congress_management.dto.registrationandpayments.registration.RegisteredUserDTO;
import com.alessandro.congress_management.dto.registrationandpayments.registration.RegistrationRequest;
import com.alessandro.congress_management.dto.registrationandpayments.registration.RegistrationResponse;
import com.alessandro.congress_management.dto.registrationandpayments.wallettransaction.WalletTransactionCreate;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;
import com.alessandro.congress_management.models.authentication_and_users.UserEntity;
import com.alessandro.congress_management.models.congress_management.CongressEntity;
import com.alessandro.congress_management.models.registrations_and_payments.RegistrationEntity;
import com.alessandro.congress_management.repositories.registration.RegistrationRepository;
import com.alessandro.congress_management.services.congress.CongressService;
import com.alessandro.congress_management.services.registrationandpayments.wallettransaction.WalletTransactionService;
import com.alessandro.congress_management.services.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegistrationServiceImpl implements RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final CongressService congressService;
    private final UserService userService;
    private final WalletTransactionService walletTransactionService;
    private final String WALLET_TRANSACTION_PAYMENT = "PAYMENT";

    public RegistrationServiceImpl(RegistrationRepository registrationRepository, CongressService congressService, UserService userService, WalletTransactionService walletTransactionService) {
        this.registrationRepository = registrationRepository;
        this.congressService = congressService;
        this.userService = userService;
        this.walletTransactionService = walletTransactionService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegistrationResponse registerForCongress(Long congressId, Long userId) throws NotFoundException, BusinessRuleException {
        //Obtenemos el congreso
        CongressEntity congress = congressService.findCongressEntityById(congressId);

        //verificamos que el congreso este activo
        if (!congress.getIsActive()) {
            throw new BusinessRuleException("The congress is not active.");
        }

        //Obtenemos el usuario
        UserEntity user = userService.getUserById(userId);
        //verificamos que el usuario no se haya registrado previamente
        if (registrationRepository.existsByUser_IdUserAndCongress_IdCongress(
                user.getIdUser(), congressId)) {
            throw new BusinessRuleException("User is already registered for this congress.");
        }
        //verificamos que el usuario tenga saldo suficiente en su billetera
        if (user.getWalletBalance().compareTo(congress.getPrice()) < 0) {
            throw new BusinessRuleException("Insufficient balance in wallet.");
        }



        //Creamos la inscripcion
        RegistrationEntity inscription = new RegistrationEntity();
        inscription.setCongress(congress);
        inscription.setUser(user);
        inscription.setAmountPaid(congress.getPrice());
        inscription.setRegistrationDate(LocalDateTime.now());
        RegistrationEntity inscriptionSave = registrationRepository.save(inscription);

        //Registramos la transacción de pago en la billetera del usuario
        walletTransactionService.createTransaction(new WalletTransactionCreate(
                user,
                WALLET_TRANSACTION_PAYMENT,
                "Pago por la inscripcion del congreso: " + congress.getCongressName(),
                inscriptionSave,
                LocalDateTime.now(),
                congress.getPrice()

        ));

        return RegistrationResponse.fromEntity(inscriptionSave);
    }

    @Override
    public List<MyCongressRegistrationDTO> getMyRegistrations(Long userId) {
        List<RegistrationEntity> registrations = registrationRepository.findByUser_IdUser(userId);
        return registrations.stream()
                .map(MyCongressRegistrationDTO::fromEntity)
                .toList();
    }

    @Override
    public List<RegisteredUserDTO> getRegistrationsByCongress(Long congressId) {
        List<RegistrationEntity> registrations = registrationRepository.findByCongress_IdCongress(congressId);
        return registrations.stream()
                .map(RegisteredUserDTO::fromEntity)
                .toList();
    }
}
