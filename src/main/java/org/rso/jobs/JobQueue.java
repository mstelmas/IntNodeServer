package org.rso.jobs;

import lombok.extern.java.Log;
import org.rso.storage.dto.JobEntityDto;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

@Log
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
}
