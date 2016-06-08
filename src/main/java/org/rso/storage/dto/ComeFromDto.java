package org.rso.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.rso.utils.ComeFrom;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ComeFromDto {
    private ComeFrom comeFrom;
    private long val;
}
