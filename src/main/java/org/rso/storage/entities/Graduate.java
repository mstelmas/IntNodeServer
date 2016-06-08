package org.rso.storage.entities;

import lombok.*;
import org.rso.storage.types.ComeFrom;
import org.rso.storage.types.Location;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class Graduate {
    private String name;
    private String surname;
    private ComeFrom comeFrom;
    private Location locationFrom;
    private boolean workedAtStudy;
    private List<FieldOfStudy> fieldOfStudyList = new ArrayList<>();
}
