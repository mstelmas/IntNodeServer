package org.rso.utils;

import com.google.common.base.Predicate;
import javaslang.control.Try;
import org.rso.storage.dto.GraduateDto;
import org.rso.storage.entities.FieldOfStudy;
import org.rso.storage.entities.Graduate;
import org.rso.storage.entities.University;
import org.rso.storage.repositories.UniversityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataBasePopulator {

    private static final int BASE_POPULATION = 1000;

    @Autowired
    private UniversityRepository universityRepository;

    private final AppProperty appProperty = AppProperty.getInstance();

    private final Random random = new Random();

    private static final Predicate<Location> locationPredicate = location -> location != Location.LAND;


    @PostConstruct
    public void init() {
        universityRepository.clear();

        Try.of(appProperty::isSelfNodeCoordinator)
                .onSuccess(isSelfNodeCoordinator -> {
                    if (isSelfNodeCoordinator) {
                        populateDB();
                    }
                });
    }

    private void populateDB() {
        for (University university : getUniversities()) {
            Random random = new Random();
            int val = random.nextInt(1000) + BASE_POPULATION;
            List<GraduateDto> graduateDtoList = new ArrayList<>();
            for (int i = 0; i < val; i++) {
                Graduate graduate = this.createGraduate();
                university.getGraduates().add(graduate);
            }
            this.universityRepository.insert(university);
        }
    }

    private Graduate createGraduate() {
        return Graduate.builder()
                .name(randomName())
                .surname(randomSurname())
                .comeFrom(randoComeFrom())
                .locationFrom(randomLocation())
                .fieldOfStudyList(randomFieldOfStudy())
                .workedAtStudy(randomWorked())
                .build();
    }

    private boolean randomWorked() {
        return random.nextInt() % 15 == 0;
    }

    private List<FieldOfStudy> randomFieldOfStudy() {
        List<FieldOfStudy> fieldOfStudies = Arrays.asList(
                new FieldOfStudy("Informatyka"),
                new FieldOfStudy("Chemia"),
                new FieldOfStudy("Matematyka"),
                new FieldOfStudy("Biologia"),
                new FieldOfStudy("Fizyka"),
                new FieldOfStudy("Historia"),
                new FieldOfStudy("Kulturoznastwo"),
                new FieldOfStudy("Administracja"),
                new FieldOfStudy("Elektronika"),
                new FieldOfStudy("Telekomunikacja"),
                new FieldOfStudy("Radiolgia"),
                new FieldOfStudy("Budownictwo"),
                new FieldOfStudy("Religioznastwo")
        );
        List<FieldOfStudy> res = new ArrayList<>();
        res.add(fieldOfStudies.get(random.nextInt(fieldOfStudies.size())));
        if(random.nextInt()%137==0){
            res.add(fieldOfStudies.get(random.nextInt(fieldOfStudies.size())));
        }
        return res;
    }

    private Location randomLocation() {

        List location = Arrays.asList(Location.values()).stream().filter(l -> locationPredicate.apply(l)).collect(Collectors.toList());
        return (Location) location.get(random.nextInt(location.size()));
    }

    private ComeFrom randoComeFrom() {
        return Arrays.asList(ComeFrom.values()).get(random.nextInt(ComeFrom.values().length));
    }

    private String randomName() {

        List<String> names = Arrays.asList("radek", "tomek", "marek", "lukasz", "bartek", "kamil", "darek", "michal",
                "kamila", "lucja", "lucyna", "marta", "agata", "krystyna", "gosia", "mariola");
        return names.get(random.nextInt(names.size()));
    }

    private String randomSurname() {

        List<String> surnames = Arrays.asList("xxx", "xxy", "xxz", "aaa", "xbs", "jdk", "java", "python", "anaconda");
        return surnames.get(random.nextInt(surnames.size()));
    }

    private List<University> getUniversities() {
        return Arrays.asList(
                new University("PW", "1820", Location.MAZOWIECKIE, UniversityType.POLYTECHNIC),
                new University("UJ", "1364", Location.MALOPOLSKIE, UniversityType.UNIVERSITY),
                new University("SGGW", "1820", Location.MAZOWIECKIE, UniversityType.AGRICULTURAL_UNIVERSITY),
                new University("PK", "1820", Location.MALOPOLSKIE, UniversityType.POLYTECHNIC),
                new University("Politechnika Wroclawska", "1920", Location.DOLNOSLASKIE, UniversityType.POLYTECHNIC),
                new University("Politechnika Gdanska", "1930", Location.POMORSKIE, UniversityType.POLYTECHNIC),
                new University("UW", "1820", Location.MAZOWIECKIE, UniversityType.UNIVERSITY),
                new University("SGH", "1876", Location.MAZOWIECKIE, UniversityType.UNIVERSITY_OF_ECONOMICS),
                new University("Politechnika Swietokrzyska", "1820", Location.SWIETOKRZYSKIE, UniversityType.POLYTECHNIC),
                new University("UJK", "1820", Location.SWIETOKRZYSKIE, UniversityType.UNIVERSITY),
                new University("AWF Katowice", "1820", Location.SLASKIE, UniversityType.OTHER),
                new University("Uniwersytet Slaski", "1820", Location.SLASKIE, UniversityType.UNIVERSITY),
                new University("UP", "1820", Location.MALOPOLSKIE, UniversityType.UNIVERSITY),
                new University("AGH", "1820", Location.MALOPOLSKIE, UniversityType.POLYTECHNIC),
                new University("Uniwersytet Wroclawski", "1820", Location.DOLNOSLASKIE, UniversityType.UNIVERSITY),
                new University("Politechnika Rzeszowska", "1820", Location.PODKARPADZKIE, UniversityType.POLYTECHNIC),
                new University("Uniwersytet Ekonomiczny w Krakowie", "1820", Location.MALOPOLSKIE, UniversityType.UNIVERSITY),
                new University("Uniwersytet Warmińsko-Mazurski w Olsztynie", "1820", Location.WARMINSKO_MAZURSKIE, UniversityType.UNIVERSITY),
                new University("Uniwersytet Medyczny w Białymstoku", "1820", Location.PODLASKIE, UniversityType.UNIVERSITY),
                new University("Uniwersytet Zielonogórski w Zielonej Górze", "1820", Location.LUBUSKIE, UniversityType.UNIVERSITY),
                new University("Uniwersytet Ekonomiczny w Krakowie", "1820", Location.MALOPOLSKIE, UniversityType.UNIVERSITY),
                new University("Uniwersytet Opolski", "1820", Location.OPOLSKIE, UniversityType.UNIVERSITY),
                new University("Uniwersytet Ekonomiczny w Krakowie", "1820", Location.MALOPOLSKIE, UniversityType.UNIVERSITY),
                new University("Uniwersytet Mikołaja Kopernika w Toruniu", "1945", Location.KUJAWSKO_POMORSKIE, UniversityType.UNIVERSITY),
                new University("Uniwersytet Adama Mickiewicza w Poznaniu", "1820", Location.WIELKOPOLSKIE, UniversityType.UNIVERSITY),
                new University("Uniwersytet Marii Curie-Skłodowskiej w Lublinie", "1820", Location.LUBELSKIE, UniversityType.UNIVERSITY),
                new University("Uniwersytet Łódzki", "1820", Location.LODZKIE, UniversityType.UNIVERSITY),
                new University("Uniwersytet Ekonomiczny w Katowicach", "1820", Location.MALOPOLSKIE, UniversityType.UNIVERSITY),
                new University("Uniwersytet Ekonomiczny w Krakowie", "1820", Location.MALOPOLSKIE, UniversityType.UNIVERSITY),
                new University("Uniwersytet Pedagogiczny im. Komisji Edukacji Narodowej w Krakowie", "1820", Location.MALOPOLSKIE, UniversityType.UNIVERSITY),
                new University("Uniwersytet Przyrodniczo-Humanistyczny w Siedlcach", "1820", Location.MAZOWIECKIE, UniversityType.UNIVERSITY),
                new University("Uniwersytet Przyrodniczy we Wrozławiu", "1820", Location.DOLNOSLASKIE, UniversityType.UNIVERSITY),
                new University("Uniwersytet Technologiczno-Przyrodniczy im. Jana i Jędrzeja Śniadeckich w Bydgoszczy", "1820", Location.KUJAWSKO_POMORSKIE, UniversityType.UNIVERSITY),
                new University("Uniwersytet Przyrodniczy w Poznaniu", "1820", Location.WIELKOPOLSKIE, UniversityType.UNIVERSITY),
                new University("Uniwersytet Rolniczy im. Hugona Kołłątaja w Krakowie", "1820", Location.MALOPOLSKIE, UniversityType.UNIVERSITY),
                new University("Uniwersytet Przyrodniczy w Lublinie", "1820", Location.LUBELSKIE, UniversityType.UNIVERSITY),
                new University("Uniwersytet Technologiczny w Szczecinie", "1820", Location.ZACHODNIO_POMORSKIE, UniversityType.UNIVERSITY),
                new University("Uniwersytet Technologiczno-Humanistyczny im. Kazimierza Pułaskiego w Radomiu", "1820", Location.MALOPOLSKIE, UniversityType.UNIVERSITY)

        );
    }
}
