package com.alessandro.congress_management.repositories.invitation;

import com.alessandro.congress_management.models.authentication_and_users.UserInvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInvitationRepository extends JpaRepository<UserInvitationEntity, Long> {

    Optional<UserInvitationEntity> findByToken(String token);

    void deleteByUser_IdUserAndUsedAtIsNull(Long idUser);
}