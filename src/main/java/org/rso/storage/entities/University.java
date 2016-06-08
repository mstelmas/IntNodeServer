package org.rso.storage.entities;

import lombok.*;
import org.rso.utils.Location;
import org.rso.utils.UniversityType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class University {
    @Id
    private String id;
    private String name;
    private String yearOfFundation;
    private Location location;
    private UniversityType universityType;
    private List<Graduate> graduates = new ArrayList<>();

    public University(String name, String yearOfFundation, Location location, UniversityType universityType) {
        this.name = name;
        this.yearOfFundation = yearOfFundation;
        this.location = location;
        this.universityType = universityType;
    }
}
