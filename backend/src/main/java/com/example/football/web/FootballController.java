package com.example.football.web;

import com.example.football.repository.TeamRepository;
import com.example.football.service.MatchService;
import com.example.football.web.dto.CreateMatchRequest;
import com.example.football.web.dto.MatchResponse;
import com.example.football.web.dto.StandingResponse;
import com.example.football.web.dto.TeamResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class FootballController {
    private final TeamRepository teamRepository;
    private final MatchService matchService;

    public FootballController(TeamRepository teamRepository, MatchService matchService) {
        this.teamRepository = teamRepository;
        this.matchService = matchService;
    }

    @GetMapping("/teams")
    public List<TeamResponse> teams() {
        return teamRepository.findAll().stream()
                .map(TeamResponse::from)
                .toList();
    }

    @GetMapping("/matches")
    public List<MatchResponse> matches() {
        return matchService.getMatches().stream()
                .map(MatchResponse::from)
                .toList();
    }

    @PostMapping("/matches")
    @ResponseStatus(HttpStatus.CREATED)
    public MatchResponse createMatch(@Valid @RequestBody CreateMatchRequest request) {
        return MatchResponse.from(matchService.recordMatch(request));
    }

    @DeleteMapping("/matches/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMatch(@PathVariable Long id) {
        matchService.deleteMatch(id);
    }

    @GetMapping("/standings")
    public List<StandingResponse> standings() {
        AtomicInteger rank = new AtomicInteger(1);
        return matchService.getStandings().stream()
                .map(standing -> StandingResponse.from(rank.getAndIncrement(), standing))
                .toList();
    }
}
