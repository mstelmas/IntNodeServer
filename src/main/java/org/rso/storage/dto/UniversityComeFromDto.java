package org.rso.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UniversityComeFromDto {
    private UniversityDto universityDto;
    private List<ComeFromDto> comeFromDtos;
}
