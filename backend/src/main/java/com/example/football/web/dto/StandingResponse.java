package com.example.football.web.dto;

import com.example.football.domain.Standing;

public record StandingResponse(
        int rank,
        TeamResponse team,
        int played,
        int wins,
        int draws,
        int losses,
        int goalsFor,
        int goalsAgainst,
        int goalDifference,
        int points
) {
    public static StandingResponse from(int rank, Standing standing) {
        return new StandingResponse(
                rank,
                TeamResponse.from(standing.getTeam()),
                standing.getPlayed(),
                standing.getWins(),
                standing.getDraws(),
                standing.getLosses(),
                standing.getGoalsFor(),
                standing.getGoalsAgainst(),
                standing.getGoalDifference(),
                standing.getPoints()
        );
    }
}
