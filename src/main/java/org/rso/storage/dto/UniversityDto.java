package org.rso.storage.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.rso.utils.Location;
import org.rso.utils.UniversityType;

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
