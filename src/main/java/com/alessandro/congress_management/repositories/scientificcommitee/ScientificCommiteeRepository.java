package com.alessandro.congress_management.repositories.scientificcommitee;


import com.alessandro.congress_management.models.congress_management.ScientificCommiteeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScientificCommiteeRepository extends JpaRepository<ScientificCommiteeEntity, Long> {

    boolean existsByCongress_IdCongressAndUser_IdUser(Long congressId, Long userId);

    List<ScientificCommiteeEntity> findAllByCongress_IdCongress(Long congressId);

    Optional<ScientificCommiteeEntity> findByCongress_IdCongressAndUser_IdUser(Long congressId, Long userId);

    @Query("""
    SELECT sc.user.idUser
    FROM ScientificCommitee sc
    WHERE sc.congress.idCongress = :congressId
    """)
    List<Long> findUserIdsByCongressId(@Param("congressId") Long congressId);
}
