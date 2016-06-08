package org.rso.storage.dto;

import lombok.*;
import lombok.experimental.Wither;
import org.rso.storage.types.Location;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
@EqualsAndHashCode
public class LocationValueDto {
    private Location location;
    private long value;
}
