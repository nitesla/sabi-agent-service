package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentLocationDto;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.responseDto.AgentLocationResponseDto;
import com.sabi.agent.core.models.agentModel.AgentLocation;
import com.sabi.agent.service.helper.GenericSpecification;
import com.sabi.agent.service.helper.SearchCriteria;
import com.sabi.agent.service.helper.SearchOperation;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.agentRepo.AgentLocationRepository;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

//ken
@Service
@Slf4j
public class AgentLocationService {
    private AgentLocationRepository agentLocationRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;


    public AgentLocationService(AgentLocationRepository agentLocationRepository, ModelMapper mapper, ObjectMapper objectMapper, Validations validations) {
        this.agentLocationRepository = agentLocationRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
    }

    /**
     * <summary>
     * agentLocation creation
     * </summary>
     * <remarks>this method is responsible for creation of new agentLocation</remarks>
     */

    public AgentLocationResponseDto createAgentLocation(AgentLocationDto request) {
        validations.validateAgentLocation(request);
        AgentLocation agentLocation = mapper.map(request, AgentLocation.class);
        boolean agentLocationExist = agentLocationRepository.existsByAgentId(request.getAgentId());
        if (agentLocationExist) {
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " agentLocation already exist");
        }
        agentLocation.setCreatedBy(0L);
        agentLocation.setActive(false);
        agentLocation = agentLocationRepository.save(agentLocation);
        log.debug("Create new agentLocation - {}" + new Gson().toJson(agentLocation));
        return mapper.map(agentLocation, AgentLocationResponseDto.class);
    }

    public AgentLocationDto updateAgentLocation(AgentLocationDto request) {
        validations.validateAgentLocation(request);
        AgentLocation agentLocation = agentLocationRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Country Id does not exist!"));
        mapper.map(request, agentLocation);
        agentLocation.setUpdatedBy(0L);
        boolean alreadyExists = agentLocationRepository.exists(Example.of(agentLocation));
        if (alreadyExists) {
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " agentLocation already exist");
        }
        agentLocationRepository.save(agentLocation);
        log.debug("Country record updated - {}" + new Gson().toJson(agentLocation));
        return mapper.map(agentLocation, AgentLocationDto.class);
    }

    /**
     * <summary>
     * Find agentLocation
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */
    public AgentLocationResponseDto findAgentLocation(Long id) {
        AgentLocation agentLocation = agentLocationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agentLocation id does not exist!"));
        return mapper.map(agentLocation, AgentLocationResponseDto.class);
    }

    /**
     * <summary>
     * Find all agentLocations
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */
    public Page<AgentLocation> findAll(String name, Boolean isActive, PageRequest pageRequest) {
        GenericSpecification<AgentLocation> genericSpecification = new GenericSpecification<AgentLocation>();

        if (name != null && !name.isEmpty()) {
            genericSpecification.add(new SearchCriteria("name", name, SearchOperation.MATCH));
        }
        if (isActive != null) {
            genericSpecification.add(new SearchCriteria("isActive", isActive, SearchOperation.EQUAL));
        }

        Page<AgentLocation> agentLocation = agentLocationRepository.findAll(genericSpecification, pageRequest);

        return agentLocation;
    }

    /**
     * <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a agentLocation</remarks>
     */
    public void enableDisEnableState(EnableDisEnableDto request) {
        AgentLocation agentLocation = agentLocationRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agentLocation id does not exist!"));
        agentLocation.setActive(request.isActive());
        agentLocation.setUpdatedBy(0L);
        agentLocationRepository.save(agentLocation);

    }

    /**
     * <summary>
     * Get all by status
     * </summary>
     * <remarks>this method returns a list of agent location based on their active status</remarks>
     */
    public List<AgentLocationResponseDto> getAllByStatus(Boolean isActive) {
        List<AgentLocation> agentLocations = agentLocationRepository.findByIsActive(isActive);
        return agentLocations
                .stream()
                .map(user -> mapper.map(user, AgentLocationResponseDto.class))
                .collect(Collectors.toList());
    }
}
