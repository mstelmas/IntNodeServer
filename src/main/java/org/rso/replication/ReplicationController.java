package org.rso.replication;

import lombok.NonNull;
import lombok.extern.java.Log;
import org.rso.dto.DtoConverters;
import org.rso.dto.UniversityDto;
import org.rso.entities.University;
import org.rso.repositories.UniversityRepo;
import org.rso.repositories.UniversityRepository;
import org.rso.utils.AppProperty;
import org.rso.utils.Location;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log
@RestController
@RequestMapping("utils")
public class ReplicationController {

    @Resource
    private UniversityRepository universityRepository;

    @Resource
    private UniversityRepo universityRepo;

    private final AppProperty appProperty = AppProperty.getInstance();

    @RequestMapping(value = "/replication/{locationId}", method = RequestMethod.GET)
    public List<UniversityDto> getUniversitiesForLocation(@PathVariable(value="locationId") final Location locationId) {
        final List<University> universitiesByLocation = Optional.ofNullable(universityRepo.findByLocation(locationId))
                .orElseThrow(() -> new RuntimeException(
                        String.format("No universities for location: %s found at node with ID: %d", locationId.toString(), appProperty.getSelfNode().getNodeId()))
                );

        return universitiesByLocation.stream()
                .map(DtoConverters.universityEntityToDto)
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/replication", method = RequestMethod.POST)
    public ResponseEntity<Void> saveUniversitiesForLocation(@RequestBody @NonNull final List<UniversityDto> universityDtoList){

        universityDtoList.stream()
                .map(DtoConverters.universityDtoToEntity)
                .forEach(universityRepository::insert);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/replication/{locationId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> removeUniversitiesForLocation(@PathVariable(value="locationId") final Location locationId) {
        final List<University> universitiesByLocation = Optional.ofNullable(universityRepo.findByLocation(locationId))
                .orElseThrow(() -> new RuntimeException(
                        String.format("No universities for location: %s found at node with ID: %d", locationId.toString(), appProperty.getSelfNode().getNodeId()))
                );

        universityRepo.delete(universitiesByLocation);

        return ResponseEntity.noContent().build();
    }
}
