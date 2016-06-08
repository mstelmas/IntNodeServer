package org.rso.storage.services;

import lombok.extern.java.Log;
import org.rso.storage.dto.*;
import org.rso.storage.entities.FieldOfStudy;
import org.rso.storage.entities.responseObject.LocationMapResponse;
import org.rso.network.dto.NetworkStatusDto;
import org.rso.network.dto.NodeStatusDto;
import org.rso.network.services.NodeNetworkService;
import org.rso.configuration.services.AppProperty;
import org.rso.storage.types.ComeFrom;
import org.rso.jobs.JobQueue;
import org.rso.storage.types.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.util.*;


@Log
@Service
@Transactional
public class JobServiceImpl implements JobService {

    private final static int EXTERNAL_NODES_PORT = 9000;

    @Autowired
    private JobQueue jobQueue;

    @Resource
    private NodeNetworkService nodeNetworkService;

    private final AppProperty appProperty = AppProperty.getInstance();

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${timeout.request.read}")
    private int readTimeout;

    @Value("${timeout.request.connect}")
    private int connectionTimeout;


    private static final String GRADUATE_BY_LOCATION = "http://{ip}:{port}/int/graduatesByLocation/{location}";
    private static final String GRADUATE_BY_UNIVERSITIES = "http://{ip}:{port}/int/getGraduatesByLocationInAllUniversities/{location}";
    private static final String GRADUATE_BY_FIELD_OF_STUDIES = "http://{ip}:{port}/int/getGraduatesByLocationInAllFieldOfStudy/{location}";
    private static final String GRADUATES_BY_ORGIN_BY_COUNTRIES = "http://{ip}:{port}/int/getStatisticOrginGraduateByLocation/countries/{location}";
    private static final String GRADUATES_BY_ORGIN_BY_UNIVERSITIES = "http://{ip}:{port}/int/getStatisticOrginGraduateByLocation/universities/{location}";
    private static final String GRADUATES_BY_ORGIN_BY_FIELD_OF_STUDY = "http://{ip}:{port}/int/getStatisticOrginGraduateByLocation/fieldOfStudy/{location}";
    private static final String GRADUATES_WORKING_BY_COUNTRIES = "http://{ip}:{port}/int/getStatisticWorkingStudents/countries/{location}";
    private static final String GRADUATES_WORKING_BY_UNIVERSITIES = "http://{ip}:{port}/int/getStatisticWorkingStudents/universities/{location}";
    private static final String GRADUATES_WORKING_BY_FIELD_OF_STUDY = "http://{ip}:{port}/int/getStatisticWorkingStudents/fieldOfStudy/{location}";
    private static final String GRADUATES_MORE_THAN_ONE_FIELD_OF_STUDY_COUNTRIES = "http://{ip}:{port}/int/getGraduatesMoreThanOneFieldOfStudyByCountries/{location}";
    private static final String GRADUATES_MORE_THAN_ONE_FIELD_OF_STUDY_UNIVERSITIES = "http://{ip}:{port}/int/getGraduatesMoreThanOneFieldOfStudyByUniversities/{location}";
    private static final int BASE_PORT = 8080;


    @PostConstruct
    public void initialize() {
        ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(readTimeout);
        ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(connectionTimeout);
    }

    @Override
    public void registerJob(JobEntityDto jobEntityDto) {
        log.info("addEntry to queue " + jobEntityDto);
        jobQueue.add(jobEntityDto);
    }

    @Override
    public void getGraduatesFromAllCountriesJob(JobEntityDto jobEntityDto) {

        List<LocationValueDto> result = new ArrayList<>();
        LocationMapResponse locationMapResponse = new LocationMapResponse();
        for (Location location : avaiableLocation()) {
            final String ipAddress = getResourceNodeIp(location);
            LocationValueDto res = restTemplate.getForObject(
                    GRADUATE_BY_LOCATION,
                    LocationValueDto.class,
                    ipAddress,
                    BASE_PORT,
                    location
            );
//            TODO if not responding try agian
//            log.info(res.toString());
            result.add(res);
            locationMapResponse.addToMap(location, (int) res.getValue());
        }
        jobEntityDto.setResponseBody(locationMapResponse);
        sendResponse(jobEntityDto, result);
    }

    @Override
    public void getGraduatesFromAllUniversities(JobEntityDto jobEntityDto) {
        List<UniversityDto> result = new ArrayList<>();
        getFromUniversityDto(result,GRADUATE_BY_UNIVERSITIES);
        sendResponse(jobEntityDto, result);
    }

    @Override
    public void getGraduatesFromAllFieldOfStudies(JobEntityDto jobEntityDto) {
        fieldOfStudyDtoJob(jobEntityDto,GRADUATE_BY_FIELD_OF_STUDIES);
    }

    private void fieldOfStudyDtoJob(JobEntityDto jobEntityDto, String url) {
        List<FieldOfStudyDto> result = new ArrayList<>();
        Map<String, Long> map = new HashMap<>();
        for (Location location : avaiableLocation()) {
            final String ipAddress = getResourceNodeIp(location);
            ResponseEntity<List<FieldOfStudyDto>> listResponseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<FieldOfStudyDto>>() {
                        @Override
                        public Type getType() {
                            return super.getType();
                        }
                    },
                    ipAddress,
                    BASE_PORT,
                    location
            );
//            TODO if not responding try agian
//            reduceResult(listResponseEntity.getBody(),map);
            listResponseEntity.getBody().stream().forEach(le -> {
                map.computeIfPresent(le.getName(), (k, v) -> v += le.getVal());
                map.putIfAbsent(le.getName(), le.getVal());
            });

        }

        for (String str : map.keySet()) {
            result.add(new FieldOfStudyDto(str, map.get(str)));
        }
        sendResponse(jobEntityDto, result);
    }

    @Override
    public void getStatisticOrginFromUniversities(JobEntityDto jobEntityDto) {
        List<UniversityComeFromDto> result = new ArrayList<>();
        for (Location location : avaiableLocation()) {
            final String ipAddress = getResourceNodeIp(location);
            ResponseEntity<List<UniversityComeFromDto>> listResponseEntity = restTemplate.exchange(
                    GRADUATES_BY_ORGIN_BY_UNIVERSITIES,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<UniversityComeFromDto>>() {
                        @Override
                        public Type getType() {
                            return super.getType();
                        }
                    },
                    ipAddress,
                    BASE_PORT,
                    location
            );
//            TODO if not responding try agian
            result.addAll(listResponseEntity.getBody());
        }
        sendResponse(jobEntityDto, result);
    }

    @Override
    public void getStatisticOrginFromFieldOfStudy(JobEntityDto jobEntityDto) {
//        TODO not implemented!
        List<FieldOfStudyComeFromDto> result = new ArrayList<>();
        result.add(new FieldOfStudyComeFromDto(new FieldOfStudy("TODO"),Arrays.asList()));
        sendResponse(jobEntityDto, result);
    }

    @Override
    public void getStatisticWorkingStudentsCountries(JobEntityDto jobEntityDto) {
        List<LocationValueDto> result = new ArrayList<>();
        for(Location location: avaiableLocation()){
            final String ipAddress = getResourceNodeIp(location);
            ResponseEntity<LocationValueDto> locationValueDtoResponseEntity = restTemplate.exchange(
                    GRADUATES_WORKING_BY_COUNTRIES,
                    HttpMethod.GET,
                    null,
                    LocationValueDto.class,
                    ipAddress,
                    BASE_PORT,
                    location
            );
            result.add(locationValueDtoResponseEntity.getBody());
//            TODO if not responding try agian
        }
        sendResponse(jobEntityDto,result);
    }

    @Override
    public void getStatisticWorkingStudentsUniversities(JobEntityDto jobEntityDto) {
        List<UniversityDto> res = new ArrayList<>();
        getFromUniversityDto(res,GRADUATES_WORKING_BY_UNIVERSITIES);
        sendResponse(jobEntityDto,res);
    }

    private void getFromUniversityDto(List<UniversityDto> res, String url) {
        for(Location location: avaiableLocation()){
            final String ipAddress = getResourceNodeIp(location);
//            log.info("");
            ResponseEntity<List<UniversityDto>> listResponseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<UniversityDto>>() {
                        @Override
                        public Type getType() {
                            return super.getType();
                        }
                    },
                    ipAddress,
                    BASE_PORT,
                    location
            );
//            TODO if not responding try agian
            res.addAll(listResponseEntity.getBody());
        }
    }

    @Override
    public void getStatisticWorkingStudentsFieldOfStudy(JobEntityDto jobEntityDto) {
        fieldOfStudyDtoJob(jobEntityDto,GRADUATES_WORKING_BY_FIELD_OF_STUDY);
    }

    @Override
    public void getGraduatesMoreThanOneFieldOfStudyCountries(JobEntityDto jobEntityDto) {
        List<LocationValueDto> result = new ArrayList<>();
        for (Location location: avaiableLocation()){
            final String ipAddress = getResourceNodeIp(location);
            ResponseEntity<LocationValueDto> responseEntity = restTemplate.exchange(
                    GRADUATES_MORE_THAN_ONE_FIELD_OF_STUDY_COUNTRIES,
                    HttpMethod.GET,
                    null,
                    LocationValueDto.class,
                    ipAddress,
                    BASE_PORT,
                    location
            );
            result.add(responseEntity.getBody());
//            TODO retransmit
        }
        sendResponse(jobEntityDto,result);
    }

    @Override
    public void getGraduatesMoreThanOneFieldOfStudyUniversities(JobEntityDto jobEntityDto) {
        List<UniversityDto> result = new ArrayList<>();
        getFromUniversityDto(result,GRADUATES_MORE_THAN_ONE_FIELD_OF_STUDY_UNIVERSITIES);
        sendResponse(jobEntityDto,result);
    }

    @Override
    public void getStatisticOrginFromLand(JobEntityDto jobEntityDto) {
        List<ComeFromDto> result = new ArrayList<>();
        Map<ComeFrom, Long> map = new HashMap<>();
        getStatisticOrginFromCountriesAbstract().stream()
                .map(elem -> elem.getComeFromDtos())
                .forEach(
                        elem -> elem.stream().forEach(p -> {
                                    map.computeIfPresent(p.getComeFrom(), (k, v) -> v += p.getVal());
                                    map.putIfAbsent(p.getComeFrom(), p.getVal());
                                }
                        )
                );
        for (ComeFrom comeFrom : map.keySet()) {
            result.add(new ComeFromDto(comeFrom, map.get(comeFrom)));
        }
        sendResponse(jobEntityDto, result);
    }

    @Override
    public void getStatisticOrginFromCountries(JobEntityDto jobEntityDto) {
        sendResponse(jobEntityDto, getStatisticOrginFromCountriesAbstract());
    }


    private void sendResponse(JobEntityDto jobEntityDto, Object result) {
        String ipAddress = jobEntityDto.getOrderCustomer().getNodeIPAddress();
        final String jobUrl = jobEntityDto.getJobType().getUrl();
        final String resUrl = "http://{ip}:{port}" + jobUrl + "/{jobId}";
//        log.info("wysylam cialo "+jobEntityDto.getResponsBody().toString());
        try {
            restTemplate.postForObject(
                    resUrl,
                    result,
                    Void.class,
                    ipAddress,
                    EXTERNAL_NODES_PORT,
                    jobEntityDto.getId()
            );
        } catch (Exception e) {
            e.printStackTrace();
            log.info("exception to retransision ");
        }
    }

    // TODO: alternative path if coordinator is not present
    private String getResourceNodeIp(Location location) {

        final NetworkStatusDto networkStatusDto = nodeNetworkService.getNetworkStatus(appProperty.getCoordinatorNode())
                .getOrElseThrow(throwable -> new RuntimeException(throwable));

        final NodeStatusDto coordinatorNodeStatusDto = Optional.ofNullable(networkStatusDto.getCoordinator())
                .orElseThrow(() -> new RuntimeException("Network does not currently have any coordinator!"));

        // check coordinator in the first order
        if(coordinatorNodeStatusDto.getLocations().contains(location)) {
            return coordinatorNodeStatusDto.getNodeIPAddress();
        }

        final NodeStatusDto resourceNodeStatus = Optional.ofNullable(networkStatusDto.getNodes())
                .orElse(Collections.emptyList()).stream()
                .filter(nodeInfo -> nodeInfo.getLocations().contains(location))
                .findAny()
                .orElseThrow(() -> new RuntimeException(String.format("No nodes containing location: %s found", location)));

        log.info(
                String.format("Using node %d [%s] as a resource for location: %s",
                        resourceNodeStatus.getNodeId(),
                        resourceNodeStatus.getNodeIPAddress(),
                        location)
        );

        return resourceNodeStatus.getNodeIPAddress();
    }

    private List<Location> avaiableLocation() {
        return Arrays.asList(
                Location.MAZOWIECKIE,
                Location.MALOPOLSKIE,
                Location.DOLNOSLASKIE,
                Location.POMORSKIE,
                Location.SWIETOKRZYSKIE,
                Location.SLASKIE,
                Location.PODKARPADZKIE
        );
    }

    public List<LocationComeFromDto> getStatisticOrginFromCountriesAbstract() {
        List<LocationComeFromDto> res = new ArrayList<>();
        for (Location location : avaiableLocation()) {
            final String ipAddress = getResourceNodeIp(location);
            ResponseEntity<List<ComeFromDto>> listResponseEntity = restTemplate.exchange(
                    GRADUATES_BY_ORGIN_BY_COUNTRIES,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ComeFromDto>>() {
                        @Override
                        public Type getType() {
                            return super.getType();
                        }
                    },
                    ipAddress,
                    BASE_PORT,
                    location
            );
//            TODO if not responding try agian
            res.add(new LocationComeFromDto(location, listResponseEntity.getBody()));
        }
        return res;
    }
}
