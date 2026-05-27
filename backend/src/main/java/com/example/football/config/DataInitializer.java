package com.example.football.config;

import com.example.football.domain.MatchResult;
import com.example.football.domain.Standing;
import com.example.football.domain.Team;
import com.example.football.repository.MatchResultRepository;
import com.example.football.repository.StandingRepository;
import com.example.football.repository.TeamRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedData(SeedService seedService) {
        return args -> seedService.seed();
    }

    @Configuration
    static class SeedService {
        private final TeamRepository teamRepository;
        private final MatchResultRepository matchResultRepository;
        private final StandingRepository standingRepository;

        SeedService(
                TeamRepository teamRepository,
                MatchResultRepository matchResultRepository,
                StandingRepository standingRepository
        ) {
            this.teamRepository = teamRepository;
            this.matchResultRepository = matchResultRepository;
            this.standingRepository = standingRepository;
        }

        @Transactional
        public void seed() {
            if (teamRepository.count() > 0) {
                return;
            }

            Team sapporo = new Team("Sapporo North Stars", "SNS");
            Team tokyo = new Team("Tokyo United", "TKY");
            Team osaka = new Team("Osaka Azul", "OSA");
            Team fukuoka = new Team("Fukuoka Waves", "FKW");

            teamRepository.saveAll(List.of(sapporo, tokyo, osaka, fukuoka));
            standingRepository.saveAll(List.of(
                    new Standing(sapporo),
                    new Standing(tokyo),
                    new Standing(osaka),
                    new Standing(fukuoka)
            ));

            matchResultRepository.saveAll(List.of(
                    new MatchResult(LocalDate.now().minusDays(8), sapporo, tokyo, 2, 1, "Snow Dome"),
                    new MatchResult(LocalDate.now().minusDays(7), osaka, fukuoka, 1, 1, "Azul Park"),
                    new MatchResult(LocalDate.now().minusDays(3), tokyo, osaka, 3, 2, "Capital Stadium")
            ));

            resetAndApplyStandings();
        }

        private void resetAndApplyStandings() {
            List<Standing> standings = standingRepository.findAll();
            standings.forEach(Standing::reset);

            for (MatchResult match : matchResultRepository.findAll()) {
                Standing home = standings.stream()
                        .filter(standing -> standing.getTeam().getId().equals(match.getHomeTeam().getId()))
                        .findFirst()
                        .orElseThrow();
                Standing away = standings.stream()
                        .filter(standing -> standing.getTeam().getId().equals(match.getAwayTeam().getId()))
                        .findFirst()
                        .orElseThrow();

                home.applyMatch(match.getHomeScore(), match.getAwayScore());
                away.applyMatch(match.getAwayScore(), match.getHomeScore());
            }
        }
    }
}
