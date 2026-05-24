package com.example.football.service;

import com.example.football.domain.MatchResult;
import com.example.football.domain.Standing;
import com.example.football.domain.Team;
import com.example.football.repository.MatchResultRepository;
import com.example.football.repository.StandingRepository;
import com.example.football.repository.TeamRepository;
import com.example.football.web.dto.CreateMatchRequest;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchService {
    private final MatchResultRepository matchResultRepository;
    private final TeamRepository teamRepository;
    private final StandingRepository standingRepository;

    public MatchService(
            MatchResultRepository matchResultRepository,
            TeamRepository teamRepository,
            StandingRepository standingRepository
    ) {
        this.matchResultRepository = matchResultRepository;
        this.teamRepository = teamRepository;
        this.standingRepository = standingRepository;
    }

    @Transactional(readOnly = true)
    public List<MatchResult> getMatches() {
        return matchResultRepository.findAllByOrderByMatchDateDescIdDesc();
    }

    @Transactional(readOnly = true)
    public List<Standing> getStandings() {
        return standingRepository.findAllByOrderByPointsDescGoalDifferenceDescGoalsForDescTeamNameAsc();
    }

    @Transactional
    public MatchResult recordMatch(CreateMatchRequest request) {
        if (request.homeTeamId().equals(request.awayTeamId())) {
            throw new IllegalArgumentException("Home team and away team must be different.");
        }

        Team homeTeam = teamRepository.findById(request.homeTeamId())
                .orElseThrow(() -> new IllegalArgumentException("Home team not found."));
        Team awayTeam = teamRepository.findById(request.awayTeamId())
                .orElseThrow(() -> new IllegalArgumentException("Away team not found."));

        MatchResult saved = matchResultRepository.save(new MatchResult(
                request.matchDate(),
                homeTeam,
                awayTeam,
                request.homeScore(),
                request.awayScore(),
                request.venue()
        ));

        recalculateStandings();
        return saved;
    }

    @Transactional
    public void deleteMatch(Long matchId) {
        if (!matchResultRepository.existsById(matchId)) {
            throw new IllegalArgumentException("Match not found.");
        }

        matchResultRepository.deleteById(matchId);
        recalculateStandings();
    }

    private void recalculateStandings() {
        Map<Long, Standing> standingsByTeamId = standingRepository.findAll().stream()
                .collect(Collectors.toMap(standing -> standing.getTeam().getId(), Function.identity()));

        standingsByTeamId.values().forEach(Standing::reset);

        for (MatchResult match : matchResultRepository.findAll()) {
            standingsByTeamId.get(match.getHomeTeam().getId())
                    .applyMatch(match.getHomeScore(), match.getAwayScore());
            standingsByTeamId.get(match.getAwayTeam().getId())
                    .applyMatch(match.getAwayScore(), match.getHomeScore());
        }

        standingRepository.saveAll(standingsByTeamId.values());
    }
}
