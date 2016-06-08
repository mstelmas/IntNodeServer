package org.rso.storage.controllers;

import lombok.extern.java.Log;
import org.rso.storage.services.MongoAppService;
import org.rso.utils.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log
@RestController
@RequestMapping(value = "/int")
public class AppCtrl {

    @Autowired
    private MongoAppService mongoAppService;


    @RequestMapping(value = "/graduatesByLocation/{location}")
    public ResponseEntity getGraduatesByLocation(@PathVariable Location location){
        log.info("location "+ location);
        return new ResponseEntity(mongoAppService.getGraduatesByLocation(location), HttpStatus.OK);
    }

    @RequestMapping(value = "/getGraduatesByLocationInAllUniwersity/{location}")
    public ResponseEntity getGraduatesByLocationInAllUniwersity(@PathVariable Location location){
        return new ResponseEntity(mongoAppService.getGraduatesByLocationInAllUniwersity(location),HttpStatus.OK);
    }

    @RequestMapping(value = "/getGraduatesByLocationInAllFieldOfStudy/{location}")
    public ResponseEntity getGraduatesByLocationInAllFieldOfStudy(@PathVariable Location location){
        return new ResponseEntity(mongoAppService.getGraduatesByLocationInAllFieldOfStudy(location),HttpStatus.OK);
    }

    @RequestMapping(value = "/getStatisticOrginGraduateByLocation/countries/{location}")
    public ResponseEntity getStatisticOrginGraduateByLocation(@PathVariable Location location){
        return new ResponseEntity(mongoAppService.getStatisticOrginGraduateByLocation(location),HttpStatus.OK);
    }

    @RequestMapping(value = "/getStatisticOrginGraduateByLocation/universities/{location}")
    public ResponseEntity getStatisticOrginGaduateByUniversities(@PathVariable Location location){
        return new ResponseEntity(mongoAppService.getStatisticOrginGaduateByUniversities(location),HttpStatus.OK);
    }

    @RequestMapping(value = "/getStatisticOrginGraduateByLocation/fieldOfStudy/{location}")
    public ResponseEntity getStatisticOrginGraduateByFieldOfStudies(@PathVariable Location location){
        return new ResponseEntity(mongoAppService.getStatisticOrginGraduateByFieldOfStudies(location),HttpStatus.OK);
    }

    @RequestMapping(value = "/getStatisticWorkingStudents/countries/{location}")
    public ResponseEntity getStatisticWorkingStudentsByCountries(@PathVariable Location location){
        return new ResponseEntity(mongoAppService.getStatisticWorkingStudentsByCountries(location),HttpStatus.OK);
    }

    @RequestMapping(value = "/getStatisticWorkingStudents/universities/{location}")
    public ResponseEntity getStatisticWorkingStudentsByUniverities(@PathVariable Location location){
        return new ResponseEntity(mongoAppService.getStatisticWorkingStudentsByUniverities(location),HttpStatus.OK);
    }

    @RequestMapping(value = "/getStatisticWorkingStudents/fieldOfStudy/{location}")
    public ResponseEntity getStatisticWorkingStudentsByFieldOfStudy(@PathVariable Location location){
        return new ResponseEntity(mongoAppService.getStatisticWorkingStudentsByFieldOfStudy(location),HttpStatus.OK);
    }

    @RequestMapping(value = "/getGraduatesMoreThanOneFieldOfStudyByCountries/{location}")
    public ResponseEntity getGraduatesMoreThanOneFieldOfStudyByCountries(@PathVariable Location location){
        return ResponseEntity.ok(mongoAppService.getGraduatesMoreThanOneFieldOfStudyByCountries(location));
    }

    @RequestMapping(value = "/getGraduatesMoreThanOneFieldOfStudyByUniversities/{location}")
    public ResponseEntity getGraduatesMoreThanOneFieldOfStudyByUniversities(@PathVariable Location location){
        return ResponseEntity.ok(mongoAppService.getGraduatesMoreThanOneFieldOfStudyByUniversities(location));
    }
}
