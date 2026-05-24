package com.example.football.web.dto;

import com.example.football.domain.MatchResult;
import java.time.LocalDate;

public record MatchResponse(
        Long id,
        LocalDate matchDate,
        TeamResponse homeTeam,
        TeamResponse awayTeam,
        int homeScore,
        int awayScore,
        String venue
) {
    public static MatchResponse from(MatchResult match) {
        return new MatchResponse(
                match.getId(),
                match.getMatchDate(),
                TeamResponse.from(match.getHomeTeam()),
                TeamResponse.from(match.getAwayTeam()),
                match.getHomeScore(),
                match.getAwayScore(),
                match.getVenue()
        );
    }
}
