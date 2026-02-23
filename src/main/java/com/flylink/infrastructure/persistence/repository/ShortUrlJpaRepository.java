package com.flylink.infrastructure.persistence.repository;

import com.flylink.infrastructure.persistence.entity.ShortUrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for ShortUrlEntity.
 */
@Repository
public interface ShortUrlJpaRepository extends JpaRepository<ShortUrlEntity, Long> {

    Optional<ShortUrlEntity> findByCode(String code);

    Optional<ShortUrlEntity> findByCodeAndIsActiveTrue(String code);

    boolean existsByCode(String code);

    List<ShortUrlEntity> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE ShortUrlEntity u SET " +
            "u.clickCount = u.clickCount + 1, " +
            "u.lastClickAt = CURRENT_TIMESTAMP, " +
            "u.isActive = CASE WHEN (u.maxClicks IS NOT NULL AND u.clickCount + 1 >= u.maxClicks) THEN false ELSE u.isActive END "
            +
            "WHERE u.code = :code AND u.isActive = true")
    int incrementClickCountSafely(String code);
}
