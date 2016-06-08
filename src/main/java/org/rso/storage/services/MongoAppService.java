package org.rso.storage.services;

import org.rso.storage.dto.*;
import org.rso.storage.exceptions.LocationDoesNotExistException;
import org.rso.storage.repositories.UniversityRepository;
import org.rso.utils.ComeFrom;
import org.rso.utils.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class MongoAppService {

    @Autowired
    private UniversityRepository universityRepository;

    public LocationValueDto getGraduatesByLocation(Location location) {
        if(!isLocationExist(location)){
            throw new LocationDoesNotExistException(String.format("location %s does not exist!!! ",location.toString()));
        }
        return universityRepository.getGraduatesByLocation(location);
    }

    public List<UniversityDto> getGraduatesByLocationInAllUniwersity(Location location) {
        if(!isLocationExist(location)){
            throw new LocationDoesNotExistException(String.format("location %s does not exist!!! ",location.toString()));
        }
        return universityRepository.getGraduatesByLocationInAllUniversities(location);
    }

    public List<FieldOfStudyDto> getGraduatesByLocationInAllFieldOfStudy(Location location) {
        if(!isLocationExist(location)){
            throw new LocationDoesNotExistException(String.format("location %s does not exist!!! ",location.toString()));
        }
        Map<String, Long> fromDob = universityRepository.getGraduatesByLocationInAllFieldOfStudy(location);
        List<FieldOfStudyDto> fileOfStudyDtos = new ArrayList<>();
        for(String k: fromDob.keySet()){
            long val = fromDob.get(k);
            fileOfStudyDtos.add(new FieldOfStudyDto(k,val));
        }
        return fileOfStudyDtos;
    }

    public List<ComeFromDto> getStatisticOrginGraduateByLocation(Location location) {
        if(!isLocationExist(location)){
            throw new LocationDoesNotExistException(String.format("location %s does not exist!!! ",location.toString()));
        }
        Map<String ,Long> res = universityRepository.getStatisticOrginGraduateByLocation(location);
        List<ComeFromDto> resList = new ArrayList<>();
        for(String k: res.keySet()){
            long val = res.get(k);
            resList.add(new ComeFromDto(ComeFrom.valueOf(k),val));
        }
        return resList;
    }

    public List<UniversityComeFromDto> getStatisticOrginGaduateByUniversities(Location location) {
        if(!isLocationExist(location)){
            throw new LocationDoesNotExistException(String.format("location %s does not exist!!! ",location.toString()));
        }
        return universityRepository.getStatisticOrginGaduateByUniversities(location);
//        return result;
    }

    private boolean isLocationExist(Location location) {
        return Arrays.asList(Location.values()).contains(location);
    }


    public List<FieldOfStudyComeFromDto> getStatisticOrginGraduateByFieldOfStudies(Location location) {
        if(!isLocationExist(location)){
            throw new LocationDoesNotExistException(String.format("location %s does not exist!!! ",location.toString()));
        }
        return universityRepository.getStatisticOrginGraduateByFieldOfStudies(location);
    }

    public LocationValueDto getStatisticWorkingStudentsByCountries(Location location) {
        if(!isLocationExist(location)){
            throw new LocationDoesNotExistException(String.format("location %s does not exist!!! ",location.toString()));
        }
        return universityRepository.getStatisticWorkingStudentsByCountries(location);
    }

    public List<UniversityDto> getStatisticWorkingStudentsByUniverities(Location location) {
        if(!isLocationExist(location)){
            throw new LocationDoesNotExistException(String.format("location %s does not exist!!! ",location.toString()));
        }
        return universityRepository.getStatisticWorkingStudentsByUniverities(location);
    }


    public List<FieldOfStudyDto> getStatisticWorkingStudentsByFieldOfStudy(Location location) {
        if(!isLocationExist(location)){
            throw new LocationDoesNotExistException(String.format("location %s does not exist!!! ",location.toString()));
        }
        return universityRepository.getStatisticWorkingStudentsByFieldOfStudy(location);
    }

    public LocationValueDto getGraduatesMoreThanOneFieldOfStudyByCountries(Location location) {
        if(!isLocationExist(location)){
            throw new LocationDoesNotExistException(String.format("location %s does not exist!!! ",location.toString()));
        }
        return universityRepository.getGraduatesMoreThanOneFieldOfStudyByCountries(location);
    }

    public List<UniversityDto> getGraduatesMoreThanOneFieldOfStudyByUniversities(Location location) {
        if(!isLocationExist(location)){
            throw new LocationDoesNotExistException(String.format("location %s does not exist!!! ",location.toString()));
        }
        return universityRepository.getGraduatesMoreThanOneFieldOfStudyByUniversities(location);
    }
}
