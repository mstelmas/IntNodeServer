package org.rso.storage.controllers;

import lombok.extern.java.Log;
import org.rso.storage.entities.University;
import org.rso.storage.repositories.UniversityRepo;
import org.rso.utils.Location;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Log
@RestController
@RequestMapping(value = "/int")
public class LocationController {

    @Resource
    private UniversityRepo universityRepo;

    @RequestMapping(value = "locations", method = RequestMethod.GET)
    public List<Location> getLocations() {
        return universityRepo.findAll().stream()
                .map(University::getLocation)
                .distinct()
                .collect(Collectors.toList());
    }
}
