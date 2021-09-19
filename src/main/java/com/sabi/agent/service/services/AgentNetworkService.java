package com.sabi.agent.service.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentNetworkDto;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.responseDto.AgentNetworkResponseDto;
import com.sabi.agent.core.models.agentModel.AgentNetwork;
import com.sabi.agent.service.helper.GenericSpecification;
import com.sabi.agent.service.helper.SearchCriteria;
import com.sabi.agent.service.helper.SearchOperation;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.agentRepo.AgentNetworkRepository;
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
@Slf4j
@Service
public class AgentNetworkService {
    private AgentNetworkRepository agentNetworkRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;

    public AgentNetworkService(AgentNetworkRepository agentNetworkRepository, ModelMapper mapper, ObjectMapper objectMapper, Validations validations) {
        this.agentNetworkRepository = agentNetworkRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
    }


    /**
     * <summary>
     * Agent network creation
     * </summary>
     * <remarks>this method is responsible for creation of new agent network</remarks>
     */

    public AgentNetworkResponseDto createAgentNetwork(AgentNetworkDto request) {
        validations.validateAgentNetwork(request);
        AgentNetwork agentNetwork = mapper.map(request, AgentNetwork.class);
        AgentNetwork catExist = agentNetworkRepository.findByAgentId(request.getAgentId());
        if (catExist != null) {
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent network with this agent Id already exist");
        }
        agentNetwork.setCreatedBy(0L);
        agentNetwork.setIsActive(false);
        agentNetwork = agentNetworkRepository.save(agentNetwork);
        log.debug("Create new agent network - {}" + new Gson().toJson(agentNetwork));
        return mapper.map(agentNetwork, AgentNetworkResponseDto.class);
    }


    /**
     * <summary>
     * Agent network update
     * </summary>
     * <remarks>this method is responsible for updating already existing Agent network</remarks>
     */

    public AgentNetworkResponseDto updateAgentNetwork(AgentNetworkDto request) {
        validations.validateAgentNetwork(request);
        AgentNetwork agentNetwork = agentNetworkRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent network id does not exist!"));
        mapper.map(request, agentNetwork);
        boolean exists = agentNetworkRepository.exists(Example.of(agentNetwork));
        if(exists) throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent network already exist");
        agentNetwork.setUpdatedBy(0L);
        agentNetworkRepository.save(agentNetwork);
        log.debug("Agent network record updated - {}" + new Gson().toJson(agentNetwork));
        return mapper.map(agentNetwork, AgentNetworkResponseDto.class);
    }


    /**
     * <summary>
     * Find agentNetwork
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */
    public AgentNetworkResponseDto findAgentNetwork(Long id) {
        AgentNetwork agentNetwork = agentNetworkRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent network id does not exist!"));
        return mapper.map(agentNetwork, AgentNetworkResponseDto.class);
    }


    /**
     * <summary>
     * Find all agentNetwork
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */
    public Page<AgentNetwork> findAll(Long agentId, Boolean isActive, PageRequest pageRequest) {
        GenericSpecification<AgentNetwork> genericSpecification = new GenericSpecification<AgentNetwork>();

        if (agentId != null) {
            genericSpecification.add(new SearchCriteria("agentId", agentId, SearchOperation.EQUAL));
        }
        if (isActive != null) {
            genericSpecification.add(new SearchCriteria("isActive", isActive, SearchOperation.EQUAL));
        }
        Page<AgentNetwork> agentNetworks = agentNetworkRepository.findAll(genericSpecification, pageRequest);
        return agentNetworks;

    }


    /**
     * <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a country</remarks>
     */
    public void enableDisEnableState(EnableDisEnableDto request) {
        AgentNetwork agentNetwork = agentNetworkRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent network Id does not exist!"));
        agentNetwork.setIsActive(request.getIsActive());
        agentNetwork.setUpdatedBy(0L);
        agentNetworkRepository.save(agentNetwork);

    }

    public List<AgentNetworkResponseDto> getAllByStatus(Boolean isActive) {
        List<AgentNetwork> networks = agentNetworkRepository.findByIsActive(isActive);
        return networks
                .stream()
                .map(user -> mapper.map(user, AgentNetworkResponseDto.class))
                .collect(Collectors.toList());
    }
}
