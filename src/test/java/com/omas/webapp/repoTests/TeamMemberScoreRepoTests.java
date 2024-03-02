package com.omas.webapp.repoTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import com.omas.webapp.TestUtils;
import com.omas.webapp.repository.TeamMemberScoreRepository;
import com.omas.webapp.table.TeamId;
import com.omas.webapp.table.TeamMemberId;
import com.omas.webapp.table.TeamMemberScore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(true)
public class TeamMemberScoreRepoTests {

    @Autowired
    private TeamMemberScoreRepository repo;

    @Test
        public void findByClubIdAndCompetitionId() {


                TeamMemberScore score1 = new TeamMemberScore(new TeamMemberId(1l, "Clubi1", "kilpa1"),TestUtils.give60shots());
                TeamMemberScore score2 = new TeamMemberScore(new TeamMemberId(2l, "Clubi1", "kilpa1"),TestUtils.give60shots());
                TeamMemberScore score3 = new TeamMemberScore(new TeamMemberId(3l, "Clubi1", "kilpa1"),TestUtils.give60shots());
                TeamMemberScore score4 = new TeamMemberScore(new TeamMemberId(4l, "Clubi1", "kilpa1"),TestUtils.give60shots());
                TeamMemberScore score5 = new TeamMemberScore(new TeamMemberId(5l, "Clubi1", "kilpa1"),TestUtils.give60shots());

                TeamMemberScore otherScore1 = new TeamMemberScore(new TeamMemberId(11l, "Clubi11", "kilpa1"),TestUtils.give60shots());
                TeamMemberScore otherScore2 = new TeamMemberScore(new TeamMemberId(111l, "club2", "kilpa1"),TestUtils.give60shots());


                repo.save(score1);
                repo.save(score2);
                repo.save(score3);
                repo.save(score4);
                repo.save(score5);

                repo.save(otherScore1);
                repo.save(otherScore2);


                List<TeamMemberScore> scores = repo.findByTeamId(new TeamId("Clubi1", "kilpa1"));

                assertEquals(5, scores.size(), "Scores list should have 5 elements");
        }

        @Test
        public void findByUserIdAndCompetitionId() {

                TeamMemberScore score1 = new TeamMemberScore(new TeamMemberId(1l, "Clubi1", "kilpa1"),TestUtils.give60shots());

                repo.save(score1);

                TeamMemberScore result = repo.findByUserIdAndCompetitionId(1l, "kilpa1");
                assertTrue(result!=null);
                assertTrue(result.getScorePerShot().equals(score1.getScorePerShot()));
                }

}
