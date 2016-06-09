package org.rso.storage.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.rso.storage.entities.responseObject.ResponseBody;
import org.rso.jobs.JobStatus;
import org.rso.jobs.JobType;
import org.rso.network.NodeInfo;

import java.util.Date;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@Builder
public class JobEntityDto {
    private int id;
    private Date date;
    private JobType jobType;
    private JobStatus jobStatus;
    private NodeInfo orderOwner;
    private NodeInfo orderCustomer;
    private ResponseBody responseBody;
}
