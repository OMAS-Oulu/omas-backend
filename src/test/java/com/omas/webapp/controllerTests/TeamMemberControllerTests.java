package com.omas.webapp.controllerTests;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.omas.webapp.Constants;
import com.omas.webapp.Json;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.omas.webapp.TestUtils;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class TeamMemberControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private static final String baseUrl = "/api/competition/team/member";

    private static final String addNewUrl = baseUrl + "/add";
    private static final String ScoreUrl = baseUrl + "/score";

    private String userToken;

    private final String clubName = "Seuraajat1";
    private final String teamName = "team1";
    final String competitionNameId = "2040_kesaampujaiset";


    @BeforeEach
    private void test() throws Exception {
        userToken = TestUtils.getToken(mockMvc, "johndoe");
        TestUtils.addClub(mockMvc, clubName, userToken);
        TestUtils.joinClub(mockMvc, clubName, userToken);
        TestUtils.addRifleCompetition(mockMvc, competitionNameId, userToken);
        TestUtils.addTeam(mockMvc, competitionNameId, teamName + "_2", userToken);
        TestUtils.addTeam(mockMvc, competitionNameId, teamName, userToken);
    }

    @Test
    public void addTeamMember() throws Exception {


        String json = new JSONObject()
            .put("competitionName", competitionNameId)
            .put("teamName", teamName)
            .toString();


        mockMvc.perform(MockMvcRequestBuilders.post(addNewUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userToken)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.competitionId").value(competitionNameId));

    }

    @Test
    public void addTeamMemberToTwoTeamsInTheSameCompetition() throws Exception {

        String firstTeamJson = new JSONObject()
            .put("competitionName", competitionNameId)
            .put("teamName", teamName)
            .toString();

        String secondTeamJson = new JSONObject()
            .put("competitionName", competitionNameId)
            .put("teamName", teamName + "_2")
            .toString();


        mockMvc.perform(MockMvcRequestBuilders.post(addNewUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userToken)
                .content(firstTeamJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.competitionId").value(competitionNameId));

        mockMvc.perform(MockMvcRequestBuilders.post(addNewUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userToken)
                .content(secondTeamJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void getTeamMemberScore() throws Exception {

        String user = "johndoe";
        String password = "password123";

        TestUtils.registerUser(mockMvc, user, password);
        String loginResponse = TestUtils.loginUser(mockMvc, user, password);
        JSONObject userObject = new JSONObject(loginResponse).getJSONObject("user");
        long userId = userObject.getInt("userId");

        String token = new JSONObject(loginResponse).getString("token");


        String json = new JSONObject()
            .put("competitionName", competitionNameId)
            .put("teamName", teamName)
            .toString();

        mockMvc.perform(MockMvcRequestBuilders.post(addNewUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.competitionId").value(competitionNameId));

        List<Double> shots = TestUtils.give60shots();
        ObjectMapper mapper = new ObjectMapper();
        String postScoreJson = mapper.writeValueAsString(Map.of(
            "competitionName", competitionNameId,
            "teamName", teamName,
            "scoreList", shots));

        // Post user score
        mockMvc.perform(MockMvcRequestBuilders.post(ScoreUrl + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(postScoreJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sum").isNotEmpty());

        // getUserScore


        mockMvc.perform(MockMvcRequestBuilders.get(ScoreUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content("{"
                    + "\"competitionName\":\"" + competitionNameId + "\","

                    + "\"userId\":\"" + userId + "\"" + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sum").isNotEmpty())
            .andExpect(jsonPath("$.teamName").exists())
            .andExpect(jsonPath("$.userId").exists());

    }

    @Test
    public void PostTeamMemberScore() throws Exception {

        // add user to team
        String addUserJson = new JSONObject()
            .put("competitionName", competitionNameId)
            .put("teamName", teamName)
            .toString();
        mockMvc.perform(MockMvcRequestBuilders.post(addNewUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userToken)
                .content(addUserJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.competitionId").value(competitionNameId));

        List<Double> shots = TestUtils.give60shots();
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(Map.of(
            "competitionName", competitionNameId,
            "teamName", teamName,
            "scoreList", shots));

        // Post user score
        mockMvc.perform(MockMvcRequestBuilders.post(ScoreUrl + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userToken)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sum").isNotEmpty());
    }

    @Test
    public void setTeamMemberScore() throws Exception {

        TestUtils.joinTeam(mockMvc, competitionNameId, teamName, userToken);

        // First add scores normally
        TestUtils.addScores(mockMvc, competitionNameId, teamName, userToken);

        // Then set the scores and see if they changed
        List<Double> newShots = List.of(10.21, 9.126, 2.312);

        String json = Json.tree(
            "competitionName", competitionNameId,
            "teamName", teamName,
            "requestType", Constants.ADD_METHOD_SET,
            "scoreList", newShots
        ).toString();

        String correctShotString = Json.stringify(newShots);

        // Post the score
        String response = mockMvc.perform(MockMvcRequestBuilders.post(ScoreUrl + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userToken)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("scorePerShot").value(correctShotString))
            .andReturn()
            .getResponse()
            .getContentAsString();

        System.out.println(response);
    }

    @Test
    public void appendTeamMemberScore() throws Exception {

        TestUtils.joinTeam(mockMvc, competitionNameId, teamName, userToken);

        List<Double> shotsToSet = List.of(10.21, 9.126, 2.312);

        // First set some scores
        String setJson = Json.tree(
            "competitionName", competitionNameId,
            "teamName", teamName,
            "requestType", Constants.ADD_METHOD_SET,
            "scoreList", shotsToSet
        ).toString();

        mockMvc.perform(MockMvcRequestBuilders.post(ScoreUrl + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userToken)
                .content(setJson))
            .andExpect(status().isOk());

        List<Double> shotsToAppend = List.of(9.99, 1.2, 6.362);

        // Then append some scores
        String appendJson = Json.tree(
            "competitionName", competitionNameId,
            "teamName", teamName,
            "requestType", Constants.ADD_METHOD_UPDATE,
            "scoreList", shotsToAppend
        ).toString();

        List<Double> correctList = new ArrayList<>(shotsToSet);
        correctList.addAll(shotsToAppend);

        // Round the numbers like the score adding code does
        // In this case the competition is a rifle competition
        correctList = correctList.stream().map(score -> Math.floor(score * 10.0) / 10.0).collect(Collectors.toList());

        mockMvc.perform(MockMvcRequestBuilders.post(ScoreUrl + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userToken)
                .content(appendJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("scorePerShot").value(Json.stringify(correctList)));
    }

    @Test
    public void setTeamMemberScoreAsSum() throws Exception {

        TestUtils.joinTeam(mockMvc, competitionNameId, teamName, userToken);

        // Submit some sum
        String submitSumJson = new JSONObject()
            .put("competitionName", competitionNameId)
            .put("teamName", teamName)
            .put("bullsEyeCount", 3)
            .put("score", 240D)
            .toString();

        mockMvc.perform(MockMvcRequestBuilders.post(ScoreUrl + "/add/sum")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userToken)
                .content(submitSumJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sum").isNotEmpty())
            .andExpect(jsonPath("$.sum").value(240D))
            .andExpect(jsonPath("$.bullsEyeCount").value(3));

        // Now set it to something else
        String setSumJson = new JSONObject()
            .put("competitionName", competitionNameId)
            .put("teamName", teamName)
            .put("bullsEyeCount", 5)
            .put("score", 300D)
            .put("requestType", Constants.ADD_METHOD_SET)
            .toString();

        mockMvc.perform(MockMvcRequestBuilders.post(ScoreUrl + "/add/sum")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userToken)
                .content(setSumJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sum").isNotEmpty())
            .andExpect(jsonPath("$.sum").value(300D))
            .andExpect(jsonPath("$.bullsEyeCount").value(5));
    }

    @Test
    public void updateTeamMemberScoreSum() throws Exception {

        TestUtils.joinTeam(mockMvc, competitionNameId, teamName, userToken);

        // Submit some sum
        String submitSumJson = new JSONObject()
            .put("competitionName", competitionNameId)
            .put("teamName", teamName)
            .put("bullsEyeCount", 3)
            .put("score", 240D)
            .put("requestType", Constants.ADD_METHOD_UPDATE)
            .toString();

        mockMvc.perform(MockMvcRequestBuilders.post(ScoreUrl + "/add/sum")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userToken)
                .content(submitSumJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sum").isNotEmpty())
            .andExpect(jsonPath("$.sum").value(240D));

        // Submit the same thing again
        mockMvc.perform(MockMvcRequestBuilders.post(ScoreUrl + "/add/sum")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userToken)
                .content(submitSumJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sum").isNotEmpty())
            .andExpect(jsonPath("$.bullsEyeCount").value(6))
            .andExpect(jsonPath("$.sum").value(480D));

    }

    @Test
    public void PostTeamMemberScoreAsSum() throws Exception {

        // add user to team
        String addUserJson = new JSONObject()
            .put("competitionName", competitionNameId)
            .put("teamName", teamName)
            .toString();

        mockMvc.perform(MockMvcRequestBuilders.post(addNewUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userToken)
                .content(addUserJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.competitionId").value(competitionNameId));

        String submitSumJson = new JSONObject()
            .put("competitionName", competitionNameId)
            .put("teamName", teamName)
            .put("bullsEyeCount", 3)
            .put("score", 240d)
            .toString();

        // Post user score
        mockMvc.perform(MockMvcRequestBuilders.post(ScoreUrl + "/add/sum")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userToken)
                .content(submitSumJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sum").isNotEmpty());
    }

    @Test
    public void PostTeamMemberScoreAsClubAdmin() throws Exception {

        // add user to team
        String addUserJson = new JSONObject()
            .put("competitionName", competitionNameId)
            .put("teamName", teamName)

            .toString();

        mockMvc.perform(MockMvcRequestBuilders.post(addNewUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userToken)
                .content(addUserJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.competitionId").value(competitionNameId));

        TestUtils.registerUser(mockMvc, "Johndeye", "Johndeye");
        String resp = TestUtils.loginUser(mockMvc, "Johndeye", "Johndeye");
        JSONObject userObject = new JSONObject(resp).getJSONObject("user");
        long userId = userObject.getInt("userId");
        String teamMemberToken = new JSONObject(resp).getString("token");

        String submitSumJson = new JSONObject()
            .put("competitionName", competitionNameId)
            .put("teamName", teamName)
            .put("bullsEyeCount", 3)
            .put("score", 240d)
            .put("userId", userId)
            .put("clubName", clubName)
            .toString();

        TestUtils.joinClub(mockMvc, clubName, teamMemberToken);
        TestUtils.joinTeam(mockMvc, competitionNameId, teamName, teamMemberToken);
        // Post user score
        mockMvc.perform(MockMvcRequestBuilders.post(ScoreUrl + "/add/sum/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userToken)
                .content(submitSumJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sum").isNotEmpty());
    }

    @Test
    public void PostTooHighScore() throws Exception {

        // add user to team
        String addUserJson = new JSONObject()
            .put("competitionName", competitionNameId)
            .put("teamName", teamName)
            .toString();
        mockMvc.perform(MockMvcRequestBuilders.post(addNewUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userToken)
                .content(addUserJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.competitionId").value(competitionNameId));

        List<Double> shots = new ArrayList<>();
        shots.add(11d);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(Map.of(
            "competitionName", competitionNameId,
            "scoreList", shots));

        // Post user score
        mockMvc.perform(MockMvcRequestBuilders.post(ScoreUrl + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userToken)
                .content(json))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void PostScoresToNonExistentCompetition() throws Exception {


        List<Double> shots = TestUtils.give60shots();
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(Map.of(
            "competitionName", "no",
            "scoreList", shots));

        mockMvc.perform(MockMvcRequestBuilders.post(ScoreUrl + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userToken)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void IsMember() throws Exception {

        final String url = "/api/competition/team/member/isMember";

        String competition = "kilpa";
        String club = "seura";
        String team = "tiimi";

        TestUtils.registerUser(mockMvc, "Henrik", "salasana");
        String token = new JSONObject(TestUtils.loginUser(mockMvc, "Henrik", "salasana")).getString("token");

        //setup user in a team
        TestUtils.addClub(mockMvc, club, token);
        TestUtils.joinClub(mockMvc, club, token);
        TestUtils.addRifleCompetition(mockMvc, competition, token);
        TestUtils.addTeam(mockMvc, competition, team, token);
        TestUtils.joinTeam(mockMvc, competition, team, token);


        String json = new JSONObject()
            .put("teamName", team)
            .put("competitionName", competition)
            .toString();

        //this user should be in the team
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));


    }

    @Test
    public void IsMemberWithUserWhoseOnlyInTheClub() throws Exception {

        final String url = "/api/competition/team/member/isMember";

        String competition = "kilpa";
        String club = "seura";
        String team = "tiimi";

        TestUtils.registerUser(mockMvc, "Henrik", "salasana");
        String token = new JSONObject(TestUtils.loginUser(mockMvc, "Henrik", "salasana")).getString("token");

        // setup user in a team
        TestUtils.addClub(mockMvc, club, token);
        TestUtils.joinClub(mockMvc, club, token);
        TestUtils.addRifleCompetition(mockMvc, competition, token);
        TestUtils.addTeam(mockMvc, competition, team, token);


        String otherToken = TestUtils.getToken(mockMvc, "pekatin");

        TestUtils.joinClub(mockMvc, club, otherToken);
        TestUtils.addRifleCompetition(mockMvc, competition, otherToken);
        TestUtils.addTeam(mockMvc, competition, team, otherToken);
        TestUtils.joinTeam(mockMvc, competition, team, otherToken);


        String json = new JSONObject()
            .put("teamName", team)
            .put("competitionName", competition)
            .toString();

        // this user should be in the team
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(content().string("false"));

    }

}



