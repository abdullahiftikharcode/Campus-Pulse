package com.example.campuspulse;

public class DataClass {
    private String dataTitle;  // Club Name
    private String dataDesc;   // Club Description
    private int dataImage;     // Club Image (you can use a placeholder image for now)
    private String clubId;     // Club ID

    // Modified constructor to include clubId
    public DataClass(String dataTitle, String dataDesc, int dataImage, String clubId) {
        this.dataTitle = dataTitle;
        this.dataDesc = dataDesc;
        this.dataImage = dataImage;
        this.clubId = clubId;
    }

    // Getter for Club Name
    public String getDataTitle() {
        return dataTitle;
    }

    // Getter for Club Description
    public String getDataDesc() {
        return dataDesc;
    }

    // Getter for Club Image
    public int getDataImage() {
        return dataImage;
    }

    // Getter for Club ID
    public String getClubId() {
        return clubId;
    }
}
