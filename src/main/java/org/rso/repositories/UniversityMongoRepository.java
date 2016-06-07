package org.rso.repositories;

import org.rso.dto.*;
import org.rso.entities.FieldOfStudy;
import org.rso.entities.Graduate;
import org.rso.entities.University;
import org.rso.utils.ComeFrom;
import org.rso.utils.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class UniversityMongoRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UniversityRepo universityRepo;

    public void insertGraduate(GraduateDto graduateDto) {
        University university = universityRepo.findByName(graduateDto.getUniversityDto().getName());
        if (university != null) {
            Graduate graduate = DtoConverters.graduateDtoToGraduate.apply(graduateDto);
            university.getGraduates().add(graduate);
            universityRepo.save(university);
        } else {
            University newUniversity = DtoConverters.universityDtoToEntity.apply(graduateDto.getUniversityDto());
            Graduate graduate = DtoConverters.graduateDtoToGraduate.apply(graduateDto);
            newUniversity.getGraduates().add(graduate);
            universityRepo.save(newUniversity);
        }
    }

    public University insert(University university) {
        return universityRepo.insert(university);
    }

    public University findByName(String name) {
        return universityRepo.findByName(name);
    }

    public List<University> findAll() {
        return universityRepo.findAll();
    }

    public void clear() {
        this.universityRepo.deleteAll();
    }

    public LocationValueDto getGraduatesByLocation(Location location) {
        long res = universityRepo.findByLocation(location).stream()
                .mapToInt(p -> p.getGraduates().size()).sum();
//        return null;
        return new LocationValueDto(location, res);
    }

    public List<UniversityDto> getGraduatesByLocationInAllUniwersity(Location location) {
        List<UniversityDto> result = new ArrayList<>();
        for (University university : universityRepo.findByLocation(location)) {
            UniversityDto universityDto = DtoConverters.universityEntityToDto.apply(university);
            universityDto.setValue(university.getGraduates().size());
            result.add(universityDto);
        }
        return result;
    }

    public Map<String, Long> getGraduatesByLocationInAllFieldOfStudy(Location location) {
        List<FieldOfStudyDto> result = new ArrayList<>();
        Map<String, Long> map = new HashMap<>();
        for (University university : universityRepo.findByLocation(location)) {
            university.getGraduates().stream().forEach(graduate -> addToMap(map, graduate.getFieldOfStudy().getName())
            );
        }
        return map;
    }

    private void addToMap(Map map, String fieldOfStudyName) {
        map.computeIfPresent(fieldOfStudyName, (k, v) -> (long) v + 1);
        map.putIfAbsent(fieldOfStudyName, 1L);
    }

    public Map<String, Long> getStatisticOrginGraduateByLocation(Location location) {

        Map<String, Long> map = new HashMap<>();
        for (University university : universityRepo.findByLocation(location)) {
            university.getGraduates().forEach(graduate -> addToMap(map, graduate.getComeFrom().toString()));
        }
        return map;
    }

    public List<UniversityComeFromDto> getStatisticOrginGaduateByUniversities(Location location) {
        List<UniversityComeFromDto> res = new ArrayList<>();

        for (University university : universityRepo.findByLocation(location)) {
            Map<ComeFrom, Long> map = new HashMap<>();
            university.getGraduates().forEach(p -> {
                        map.computeIfPresent(p.getComeFrom(), (k, v) -> v += 1);
                        map.putIfAbsent(p.getComeFrom(),1L);
                    }
            );
            List<ComeFromDto> comeFromDtos = new ArrayList<>();
            for(ComeFrom comeFrom: map.keySet()){
                comeFromDtos.add(new ComeFromDto(comeFrom,map.get(comeFrom)));
            }
            res.add(new UniversityComeFromDto(DtoConverters.universityEntityToDto.apply(university),comeFromDtos));
        }
        return res;
    }

    public List<FieldOfStudyComeFromDto> getStatisticOrginGraduateByFieldOfStudies(Location location) {
        return Arrays.asList(new FieldOfStudyComeFromDto(new FieldOfStudy("TODO"), Arrays.asList()));
    }

    public LocationValueDto getStatisticWorkingStudentsByCountries(Location location) {
        long val = 0L;
        for(University university: universityRepo.findByLocation(location)){
            val+=university.getGraduates().stream().filter(Graduate::isWorkedAtStudy).count();
        }
        return new LocationValueDto(location,val);
    }

    public List<UniversityDto> getStatisticWorkingStudentsByUniverities(Location location) {
        List<UniversityDto> result = new ArrayList<>();
        for(University university: universityRepo.findByLocation(location)){
            long val = university.getGraduates().stream().filter(Graduate::isWorkedAtStudy).count();
            UniversityDto universityDto = DtoConverters.universityEntityToDto.apply(university);
            universityDto.setValue((int) val);
            result.add(universityDto);
        }
        return result;
    }

    public List<FieldOfStudyDto> getStatisticWorkingStudentsByFieldOfStudy(Location location) {
        List<FieldOfStudyDto> result = new ArrayList<>();
        Map<FieldOfStudy,Long> map = new HashMap<>();
        for(University university: universityRepo.findByLocation(location)){
            university.getGraduates().stream().forEach(gr->{
                FieldOfStudy fieldOfStudy = gr.getFieldOfStudy();
                map.computeIfPresent(fieldOfStudy,(k,v)->v+=1);
                map.putIfAbsent(fieldOfStudy,1L);
            });
        }
        for(FieldOfStudy fieldOfStudy: map.keySet()){
            result.add(new FieldOfStudyDto(fieldOfStudy.getName(),map.get(fieldOfStudy)));
        }
        return result;
    }
}