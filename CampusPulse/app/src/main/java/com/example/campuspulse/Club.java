package com.example.campuspulse;
import java.util.List;


public class Club {
    private String clubName;
    private List<String> clubOwners;
    private String joinCode;
    private String clubDescription;

    public Club() {
        // Default constructor required for calls to DataSnapshot.getValue(Club.class)
    }

    public Club(String clubName, List<String> clubOwners, String joinCode, String clubDescription) {
        this.clubName = clubName;
        this.clubOwners = clubOwners;
        this.joinCode = joinCode;
        this.clubDescription = clubDescription;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public List<String> getClubOwners() {
        return clubOwners;
    }

    public void setClubOwners(List<String> clubOwners) {
        this.clubOwners = clubOwners;
    }

    public String getJoinCode() {
        return joinCode;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }

    public String getClubDescription() {
        return clubDescription;
    }

    public void setClubDescription(String clubDescription) {
        this.clubDescription = clubDescription;
    }
}
