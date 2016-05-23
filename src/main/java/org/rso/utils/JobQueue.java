package org.rso.utils;

import lombok.extern.java.Log;
import org.rso.dto.JobEntityDto;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by Radosław on 23.05.2016.
 */
@Log
@Component
public class JobQueue {
    private Queue<JobEntityDto> todoJobs = new ConcurrentLinkedDeque<>();

    public JobEntityDto pool(){
        return this.todoJobs.poll();
    }

    public boolean isEmpty(){
        return this.todoJobs.isEmpty();
    }

    public void add(JobEntityDto jobEntityDto){
        this.todoJobs.add(jobEntityDto);
    }

    public int size(){
        return this.todoJobs.size();
    }
}
