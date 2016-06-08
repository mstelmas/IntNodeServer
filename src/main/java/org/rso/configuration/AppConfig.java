package org.rso.configuration;

import org.rso.services.JobService;
import org.rso.services.JobServiceImpl;
import org.rso.tasks.JobTask;
import org.rso.tasks.JobTaskExecutorService;
import org.rso.utils.AppProperty;
import org.rso.utils.JobQueue;
import org.rso.utils.Location;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    private static final AppProperty appProperty = AppProperty.getInstance();

    @Bean
    public JobService jobService(){
        return new JobServiceImpl();
    }

//    @Bean
//    public LocationMap locationMap(){
//        LocationMap locationMap = new LocationMap();
//        for(Location location: Location.values()){
//            locationMap.addEntry(location,appProperty.getSelfNode());
//        }
//        return locationMap;
//    }

    @Bean
    public JobQueue jobQueue(){
        return new JobQueue();
    }


    @Bean
    public JobTaskExecutorService jobTaskExecutorService(){
        JobTaskExecutorService jobTaskExecutorService = new JobTaskExecutorService(5,10,5000);
        jobTaskExecutorService.addNewTask(new JobTask("kamil",jobQueue(),jobService()));
//        jobTaskExecutorService.addNewTask(new JobTask("rafal",jobQueue(),jobService()));
//        jobTaskExecutorService.addNewTask(new JobTask("maks",jobQueue(),jobService()));
        return jobTaskExecutorService;
    }
}
