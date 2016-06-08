package org.rso.storage.repositories;

import org.rso.DtoConverters;
import org.rso.storage.dto.*;
import org.rso.storage.entities.FieldOfStudy;
import org.rso.storage.entities.Graduate;
import org.rso.storage.entities.University;
import org.rso.storage.types.ComeFrom;
import org.rso.storage.types.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

@Repository
public class UniversityRepository {

    @Autowired
    private UniversityRepo universityRepo;

    // a tak w ogole to od kiedy zapisujemy do repo obiekt DTO...? */
    public void insertGraduate(GraduateDto graduateDto) {

        final University university = Optional.ofNullable(universityRepo.findByName(graduateDto.getUniversityDto().getName()))
                .orElse(DtoConverters.universityDtoToEntity.apply(graduateDto.getUniversityDto()));

        final Graduate graduate = DtoConverters.graduateDtoToEntity.apply(graduateDto);
        university.getGraduates().add(graduate);
        universityRepo.save(university);
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

    public LocationValueDto getGraduatesByLocation(final Location location) {

        final long res = universityRepo.findByLocation(location).stream()
                .map(University::getGraduates)
                .mapToInt(List::size)
                .sum();

        return new LocationValueDto()
                .withLocation(location)
                .withValue(res);
    }

    public List<UniversityDto> getGraduatesByLocationInAllUniversities(final Location location) {
        return universityRepo.findByLocation(location).stream()
                .map(DtoConverters.universityEntityToDto)
                .collect(toList());
    }

    public Map<String, Long> getGraduatesByLocationInAllFieldOfStudy(Location location) {
        List<FieldOfStudyDto> result = new ArrayList<>();
        Map<String, Long> map = new HashMap<>();
        for (University university : universityRepo.findByLocation(location)) {
            university.getGraduates().stream().forEach(graduate -> {
                        graduate.getFieldOfStudyList().stream().forEach(fieldOfStudy -> {
                            addToMap(map, fieldOfStudy.getName());
                        });
                    }
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
                        map.putIfAbsent(p.getComeFrom(), 1L);
                    }
            );
            List<ComeFromDto> comeFromDtos = new ArrayList<>();
            for (ComeFrom comeFrom : map.keySet()) {
                comeFromDtos.add(new ComeFromDto(comeFrom, map.get(comeFrom)));
            }
            res.add(new UniversityComeFromDto(DtoConverters.universityEntityToDto.apply(university), comeFromDtos));
        }
        return res;
    }

    public List<FieldOfStudyComeFromDto> getStatisticOrginGraduateByFieldOfStudies(Location location) {
        return Arrays.asList(new FieldOfStudyComeFromDto(new FieldOfStudy("TODO"), Arrays.asList()));
    }

    public LocationValueDto getStatisticWorkingStudentsByCountries(Location location) {
        long val = 0L;
        for (University university : universityRepo.findByLocation(location)) {
            val += university.getGraduates().stream().filter(Graduate::isWorkedAtStudy).count();
        }
        return new LocationValueDto(location, val);
    }

    public List<UniversityDto> getStatisticWorkingStudentsByUniverities(Location location) {
        List<UniversityDto> result = new ArrayList<>();
        for (University university : universityRepo.findByLocation(location)) {
            long val = university.getGraduates().stream().filter(Graduate::isWorkedAtStudy).count();
            UniversityDto universityDto = DtoConverters.universityEntityToDto.apply(university);
            universityDto.setValue((int) val);
            result.add(universityDto);
        }
        return result;
    }

    public List<FieldOfStudyDto> getStatisticWorkingStudentsByFieldOfStudy(Location location) {
        List<FieldOfStudyDto> result = new ArrayList<>();
        Map<FieldOfStudy, Long> map = new HashMap<>();
        for (University university : universityRepo.findByLocation(location)) {
            university.getGraduates().stream().forEach(gr -> {
                List<FieldOfStudy> fieldOfStudies = gr.getFieldOfStudyList();
                fieldOfStudies.stream().forEach(fieldOfStudy -> {
                    map.computeIfPresent(fieldOfStudy, (k, v) -> v += 1);
                    map.putIfAbsent(fieldOfStudy, 1L);
                });
            });
        }
        for (FieldOfStudy fieldOfStudy : map.keySet()) {
            result.add(new FieldOfStudyDto(fieldOfStudy.getName(), map.get(fieldOfStudy)));
        }
        return result;
    }

    public LocationValueDto getGraduatesMoreThanOneFieldOfStudyByCountries(Location location) {
        AtomicInteger val = new AtomicInteger(0);
        universityRepo.findByLocation(location).forEach(
                university -> {
                    university.getGraduates().stream().forEach(graduate -> {
                        if (graduate.getFieldOfStudyList().size() > 1) {
                            val.incrementAndGet();
                        }
                    });
                }
        );
        return new LocationValueDto(location, val.get());
    }

    public List<UniversityDto> getGraduatesMoreThanOneFieldOfStudyByUniversities(Location location) {
        Map<University,Integer> universityLongMap = new HashMap<>();
        for (University university : universityRepo.findByLocation(location)) {
            university.getGraduates().forEach(graduate -> {
                        if(graduate.getFieldOfStudyList().size()>0){
                            universityLongMap.computeIfPresent(university,(k,v)->v+=1);
                            universityLongMap.putIfAbsent(university,1);
                        }
                    }
            );
        }
        List<UniversityDto> result = new ArrayList<>();
        for (University university: universityLongMap.keySet()){
            UniversityDto universityDto = DtoConverters.universityEntityToDto.apply(university);
            universityDto.setValue(universityLongMap.get(university));
            result.add(universityDto);
        }
        return result;
    }
}
