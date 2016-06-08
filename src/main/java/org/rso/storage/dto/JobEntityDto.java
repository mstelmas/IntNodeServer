package org.rso.storage.dto;

import lombok.*;
import org.rso.storage.entities.responseObject.ResponseBody;
import org.rso.utils.JobStatus;
import org.rso.utils.JobType;
import org.rso.utils.NodeInfo;

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
