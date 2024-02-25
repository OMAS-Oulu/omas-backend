package com.omas.webapp.service;

import com.omas.webapp.entity.data.Message;
import com.omas.webapp.repository.CompetitionRepository;
import com.omas.webapp.repository.TeamMemberRepository;
import com.omas.webapp.repository.TeamRepository;
import com.omas.webapp.table.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompetitionService {

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamRepository teamRepository;

    public Competition addCompetition(Competition competition) {

        if (competitionRepository.findById(competition.getName()).isPresent()) {
            return null;
        }
        return competitionRepository.save(competition);

    }

    // Authorization for this method is handled in CompetitionController
    public ResponseEntity<?> createTeam(TeamId teamId) {

        Optional<Team> team = teamRepository.findById(teamId);

        if (team.isPresent()) {
            return new ResponseEntity<>(new Message("Team already exists"), HttpStatus.BAD_REQUEST);
        }

        Team savedTeam = teamRepository.save(new Team(teamId));

        return new ResponseEntity<>(savedTeam, HttpStatus.CREATED);
    }

    public ResponseEntity<?> addTeamMember(TeamMemberId teamMemberId) {

        Optional<Team> team = teamRepository.findById(teamMemberId.getTeamId());

        if (team.isEmpty()) {
            return new ResponseEntity<>(new Message("Team does not exist"), HttpStatus.BAD_REQUEST);
        }

        UserInfoDetails userDetails = UserInfoDetails.getDetails();

        // TODO: Add check to see if user is an admin as they would probably be allowed to do this
        // Create a TeamMemberId for the authenticated user to find them in the TeamMemberRepository
        // and see if they are part of any team
        TeamMemberId userId = new TeamMemberId(userDetails.getId(), teamMemberId.getClubId(), teamMemberId.getCompetitionId());

        Optional<TeamMember> user = teamMemberRepository.findById(userId);

        if (user.isEmpty()) {
            return new ResponseEntity<>(new Message("You are not allowed to add members to this team"), HttpStatus.BAD_REQUEST);
        }

        // Check if the given user is already part of the given team
        Optional<TeamMember> member = teamMemberRepository.findById(teamMemberId);

        if (member.isPresent()) {
            return new ResponseEntity<>(new Message("User is already on the team"), HttpStatus.BAD_REQUEST);
        }

        teamMemberRepository.save(new TeamMember(teamMemberId));

        return new ResponseEntity<>(new Message("User was added to the team"), HttpStatus.ACCEPTED);
    }

    public List<Competition> getAllCompetitions() {
        return competitionRepository.findAll();
    }

    public Optional<Competition> getCompetition(String name) {

        return competitionRepository.findById(name);

    }

    public Page<Competition> findWithPaginatedSearch(int page, int size, String search) {
        return competitionRepository.findByNameContaining(search, PageRequest.of(page, size));
    }

    public Page<Competition> firstPaginated(int page, int size) {
        return competitionRepository.findAll(PageRequest.of(page, size));
    }

}
