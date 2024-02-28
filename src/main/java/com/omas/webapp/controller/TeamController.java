package com.omas.webapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.omas.webapp.entity.AddTeamRequest;
import com.omas.webapp.entity.TeamScoreRequest;
import com.omas.webapp.service.TeamMemberScoreService;
import com.omas.webapp.service.TeamService;
import com.omas.webapp.service.UserInfoDetails;
import com.omas.webapp.table.Team;
import com.omas.webapp.table.TeamId;
import com.omas.webapp.table.TeamMemberScore;

@RestController
@RequestMapping("/api/competition/team")
public class TeamController {

    @Autowired
    TeamService teamService;

    @Autowired
    TeamMemberScoreService scoreService;

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/new")
    public ResponseEntity<?> addTeam(@RequestBody AddTeamRequest request) {

        UserInfoDetails userDetails = (UserInfoDetails) SecurityContextHolder.getContext().getAuthentication()
        .getPrincipal();
        String club = userDetails.getPartOfClub();

        if(club==null || club.isEmpty()){
            return new ResponseEntity<>(Map.of("error", "User is not part of any club thus cannot create a team"), HttpStatus.BAD_REQUEST);
        }

        teamService.addTeam(request.getCompetitionName(), club);
        
        Team addedTeam = teamService.addTeam(request.getCompetitionName(), club);

        return new ResponseEntity<>(addedTeam, HttpStatus.OK);

    }


    @GetMapping("/score")
    public ResponseEntity<?> getScores(@RequestBody TeamScoreRequest request) {


        if(!teamService.isTeamPartOfThisComp(request.getClubName(), request.getCompetitionName())){
            return new ResponseEntity<>(Map.of("error", "No team found / this club is not participating in this competition"), HttpStatus.OK);
        }
        List<TeamMemberScore> scores = scoreService.getTeamScores(new TeamId(request.getClubName(), request.getCompetitionName()));

        // Notify client if there are no scores for this team id
        if (scores == null || scores.isEmpty()) {
            return new ResponseEntity<>(Map.of("messge","This team has not yet any submitted scores"), HttpStatus.OK);
        }

        return new ResponseEntity<>(scores, HttpStatus.FOUND);
    }

        @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

}