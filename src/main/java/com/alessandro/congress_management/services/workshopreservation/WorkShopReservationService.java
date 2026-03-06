package com.alessandro.congress_management.services.workshopreservation;

import com.alessandro.congress_management.dto.workshopreservation.CountWorkshopReservationsResponse;
import com.alessandro.congress_management.dto.workshopreservation.WorkShopReservationDetailsResponse;
import com.alessandro.congress_management.dto.workshopreservation.WorkShopReservationResponse;
import com.alessandro.congress_management.exceptions.BusinessRuleException;
import com.alessandro.congress_management.exceptions.NotFoundException;

import java.util.List;

public interface WorkShopReservationService {

    WorkShopReservationResponse reserveWorkshop(Long idActivityWorkshop, Long idUser) throws NotFoundException, BusinessRuleException;

    void cancelWorkshopReservation(Long idRervation, Long idUser) throws BusinessRuleException, NotFoundException;

    List<WorkShopReservationDetailsResponse> getMyWorkshopReservations(Long idUser);

    CountWorkshopReservationsResponse countWorkshopReservations(Long idActivityWorkshop) throws NotFoundException;

    List<WorkShopReservationResponse> getAllWorkshopReservationsByActivity(Long idActivityWorkshop, Long idUser) throws NotFoundException, BusinessRuleException;

}
