package com.example.football.repository;

import com.example.football.domain.Standing;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StandingRepository extends JpaRepository<Standing, Long> {
    @Query("""
            select s
            from Standing s
            join fetch s.team
            order by s.points desc, s.goalDifference desc, s.goalsFor desc, s.team.name asc
            """)
    List<Standing> findAllByOrderByPointsDescGoalDifferenceDescGoalsForDescTeamNameAsc();
}
