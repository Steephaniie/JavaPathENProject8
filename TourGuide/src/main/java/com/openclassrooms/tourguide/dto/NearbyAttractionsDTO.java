package com.openclassrooms.tourguide.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class NearbyAttractionsDTO {
    private double latitudeUser;
    private double longitudeUser;
   private List<AttractionUserDTO> attractionUsersDTO;

}
