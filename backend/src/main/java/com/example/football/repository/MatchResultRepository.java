package com.example.football.repository;

import com.example.football.domain.MatchResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
    @Query("""
            select m
            from MatchResult m
            join fetch m.homeTeam
            join fetch m.awayTeam
            order by m.matchDate desc, m.id desc
            """)
    List<MatchResult> findAllByOrderByMatchDateDescIdDesc();
}
