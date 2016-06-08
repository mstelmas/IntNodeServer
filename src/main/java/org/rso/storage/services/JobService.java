package org.rso.storage.services;

import org.rso.storage.dto.JobEntityDto;

public interface JobService {

    void registerJob(JobEntityDto jobEntityDto);

    void getGraduatesFromAllCountriesJob(JobEntityDto jobEntityDto);

    void getGraduatesFromAllUniversities(JobEntityDto jobEntityDto);

    void getGraduatesFromAllFieldOfStudies(JobEntityDto jobEntityDto);

    void getStatisticOrginFromLand(JobEntityDto jobEntityDto);

    void getStatisticOrginFromCountries(JobEntityDto jobEntityDto);

    void getStatisticOrginFromUniversities(JobEntityDto jobEntityDto);

    void getStatisticOrginFromFieldOfStudy(JobEntityDto jobEntityDto);

    void getStatisticWorkingStudentsCountries(JobEntityDto jobEntityDto);

    void getStatisticWorkingStudentsUniversities(JobEntityDto jobEntityDto);

    void getStatisticWorkingStudentsFieldOfStudy(JobEntityDto jobEntityDto);

    void getGraduatesMoreThanOneFieldOfStudyCountries(JobEntityDto jobEntityDto);

    void getGraduatesMoreThanOneFieldOfStudyUniversities(JobEntityDto jobEntityDto);
}
