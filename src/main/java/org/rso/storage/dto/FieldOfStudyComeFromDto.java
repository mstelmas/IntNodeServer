package org.rso.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.rso.storage.entities.FieldOfStudy;

import java.util.List;


@Data
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class FieldOfStudyComeFromDto {
    private FieldOfStudy fieldOfStudy;
    private List<ComeFromDto> comeFromDtos;
}
