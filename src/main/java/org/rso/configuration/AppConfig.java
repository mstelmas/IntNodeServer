package org.rso.configuration;

import org.rso.storage.services.JobService;
import org.rso.storage.services.JobServiceImpl;
import org.rso.storage.tasks.JobTask;
import org.rso.storage.tasks.JobTaskExecutorService;
import org.rso.jobs.JobQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public JobService jobService(){
        return new JobServiceImpl();
    }


    @Bean
    public JobQueue jobQueue(){
        return new JobQueue();
    }


    @Bean
    public JobTaskExecutorService jobTaskExecutorService(){
        final JobTaskExecutorService jobTaskExecutorService = new JobTaskExecutorService(5, 10, 5000);
        jobTaskExecutorService.addNewTask(new JobTask("default", jobQueue(), jobService()));
        return jobTaskExecutorService;
    }
}
