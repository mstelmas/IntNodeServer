package org.rso.storage.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.rso.storage.types.Location;
import org.rso.storage.types.UniversityType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UniversityDto {
    private String name;
    private String yearOfFundation;
    private Location location;
    private UniversityType universityType;
    private int value;
}
