package org.rso.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.rso.storage.entities.FieldOfStudy;
import org.rso.storage.types.ComeFrom;
import org.rso.storage.types.Location;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class GraduateDto {
    private String name;
    private String surname;
    private ComeFrom comeFrom;
    private Location locationFrom;
    private List<FieldOfStudy> fieldOfStudies = new ArrayList<>();
    private UniversityDto universityDto;
}
