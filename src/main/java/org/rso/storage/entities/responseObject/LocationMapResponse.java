package org.rso.storage.entities.responseObject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.rso.utils.Location;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class LocationMapResponse extends ResponseBody {
    private Map<Location,Integer> result = new HashMap<>();

    public void addToMap(Location location, int val){
        result.put(location,val);
    }
}
