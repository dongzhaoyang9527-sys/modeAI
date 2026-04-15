package com.modeai.core.domain.repository;

import com.modeai.core.domain.entity.TokenUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TokenUsageRepository extends JpaRepository<TokenUsage, Long> {
    List<TokenUsage> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT SUM(t.totalTokens) FROM TokenUsage t WHERE t.userId = :userId AND t.createdAt BETWEEN :start AND :end")
    Long sumTokensByUserIdAndDateRange(@Param("userId") Long userId,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

    @Query("SELECT SUM(t.totalTokens) FROM TokenUsage t WHERE t.createdAt BETWEEN :start AND :end")
    Long sumTokensByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
