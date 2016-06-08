package org.rso.storage.tasks;

import lombok.extern.java.Log;
import org.rso.storage.dto.JobEntityDto;
import org.rso.storage.services.JobService;
import org.rso.configuration.services.AppProperty;
import org.rso.jobs.JobQueue;
import org.rso.jobs.JobType;

/**
 * Created by Radosław on 23.05.2016.
 */
@Log
public class JobTask implements Runnable {
    private String name;
    private JobQueue jobQueue;
    private JobService jobService;

    private AppProperty appProperty = AppProperty.getInstance();


    public JobTask(String name, JobQueue jobQueue, JobService jobService) {
        this.name = name;
        this.jobQueue = jobQueue;
        this.jobService = jobService;
    }

    @Override
    public void run() {
        while (true){
            while (jobQueue.isEmpty()){

            }

            JobEntityDto jobEntityDto = jobQueue.pool();
            if(jobEntityDto!=null){
                log.info("i have taska tralalalalala "+name);
                handleOrder(jobEntityDto);
            }
        }
    }

    private void handleOrder(JobEntityDto jobEntityDto) {
        JobType jobType = jobEntityDto.getJobType();
        switch (jobType){
            case GET_GRADUATES_FROM_ALL_COUNTRIES:
                jobService.getGraduatesFromAllCountriesJob(jobEntityDto);
                break;
            case GET_GRADUATES_FROM_ALL_UNIVERSITIES:
                jobService.getGraduatesFromAllUniversities(jobEntityDto);
                break;
            case GET_GRADUATES_FROM_ALL_FIELD_OF_STUDY:
                jobService.getGraduatesFromAllFieldOfStudies(jobEntityDto);
                break;
            case GET_STATISTIC_ORGIN_FROM_LAND:
                jobService.getStatisticOrginFromLand(jobEntityDto);
                break;
            case GET_STATISTIC_ORGIN_FROM_COUNTRIES:
                jobService.getStatisticOrginFromCountries(jobEntityDto);
                break;
            case GET_STATISTIC_ORGIN_FROM_UNIVERSITIES:
                jobService.getStatisticOrginFromUniversities(jobEntityDto);
                break;
            case GET_STATISTIC_ORGIN_FROM_FIELD_OF_STUDY:
                jobService.getStatisticOrginFromFieldOfStudy(jobEntityDto);
                break;
            case GET_STATISTIC_WORKING_STUDENTS_COUNTRIES:
                jobService.getStatisticWorkingStudentsCountries(jobEntityDto);
                break;
            case GET_STATISTIC_WORKING_STUDENTS_UNIVERSITIES:
                jobService.getStatisticWorkingStudentsUniversities(jobEntityDto);
                break;
            case GET_STATISTIC_WORKING_STUDENTS_FIELD_OF_STUDY:
                jobService.getStatisticWorkingStudentsFieldOfStudy(jobEntityDto);
                break;
            case GET_GRADUATES_MORE_THAN_ONE_FIELD_OF_STUDY_COUNTRIES:
                jobService.getGraduatesMoreThanOneFieldOfStudyCountries(jobEntityDto);
                break;
            case GET_GRADUATES_MORE_THAN_ONE_FIELD_OF_STUDY_UNIVERSITIES:
                jobService.getGraduatesMoreThanOneFieldOfStudyUniversities(jobEntityDto);
                break;

            default:
                break;
        }
    }
}
