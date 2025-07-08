package com.openclassrooms.tourguide.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AttractionUserDTO {
    private String attractionName;
    private double latitude;
    private double longitude;
    private double distance;
    private int rewardPoints;
}
