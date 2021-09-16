package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentTargetDto;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.requestDto.MarketDto;
import com.sabi.agent.core.dto.responseDto.AgentTargetResponseDto;
import com.sabi.agent.core.models.Market;
import com.sabi.agent.core.models.agentModel.AgentTarget;
import com.sabi.agent.service.helper.GenericSpecification;
import com.sabi.agent.service.helper.SearchCriteria;
import com.sabi.agent.service.helper.SearchOperation;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.agentRepo.AgentTargetRepository;
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
import java.util.Optional;
import java.util.stream.Collectors;

//ken
@Slf4j
@Service
public class AgentTargetService {
    private AgentTargetRepository agentTargetRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;

    public AgentTargetService(AgentTargetRepository agentTargetRepository, ModelMapper mapper, ObjectMapper objectMapper, Validations validations) {
        this.agentTargetRepository = agentTargetRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
    }



    /** <summary>
     * Agent target creation
     * </summary>
     * <remarks>this method is responsible for creation of new agent target</remarks>
     */

    public AgentTargetResponseDto createAgentTarget(AgentTargetDto request) {
        validations.validateAgentTarget(request);
        AgentTarget agentTarget = mapper.map(request, AgentTarget.class);
        Optional<AgentTarget> agentTargetExist = agentTargetRepository.findByName(request.getName());
        if(agentTargetExist.isPresent()){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent target already exist");
        }
        agentTarget.setCreatedBy(0L);
        agentTarget.setIsActive(false);
        agentTarget = agentTargetRepository.save(agentTarget);
        log.debug("Create new agent target - {}"+ new Gson().toJson(agentTarget));
        return mapper.map(agentTarget, AgentTargetResponseDto.class);
    }



    /** <summary>
     * Agent target update
     * </summary>
     * <remarks>this method is responsible for updating already existing Agent target</remarks>
     */

    public AgentTargetResponseDto updateAgentTarget(AgentTargetDto request) {
        validations.validateAgentTarget(request);
        AgentTarget agentTarget = agentTargetRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent target id does not exist!"));
        mapper.map(request, agentTarget);
        boolean exists = agentTargetRepository.exists(Example.of(agentTarget));
        if(exists)throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent target already exist");
        agentTarget.setUpdatedBy(0L);
        agentTargetRepository.save(agentTarget);
        log.debug("Agent target record updated - {}"+ new Gson().toJson(agentTarget));
        return mapper.map(agentTarget, AgentTargetResponseDto.class);
    }


    /** <summary>
     * Find agentTarget
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */
    public AgentTargetResponseDto findAgentTarget(Long id){
        AgentTarget agentTarget  = agentTargetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent target id does not exist!"));
        return mapper.map(agentTarget, AgentTargetResponseDto.class);
    }


    /** <summary>
     * Find all agentTarget
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */
    public Page<AgentTarget> findAll(String name, Boolean isActive,
                                     Integer min, Integer max, Integer superMax, PageRequest pageRequest ){
        GenericSpecification<AgentTarget> genericSpecification = new GenericSpecification<AgentTarget>();

        if (name != null && !name.isEmpty()) {
            genericSpecification.add(new SearchCriteria("name", name, SearchOperation.MATCH));
        }
        if (isActive != null) {
            genericSpecification.add(new SearchCriteria("isActive", isActive, SearchOperation.EQUAL));
        }
        if (max != null) {
            genericSpecification.add(new SearchCriteria("max", max, SearchOperation.EQUAL));
        }
        if (min != null) {
            genericSpecification.add(new SearchCriteria("min", min, SearchOperation.EQUAL));
        }
        if (superMax != null) {
            genericSpecification.add(new SearchCriteria("superMax", superMax, SearchOperation.EQUAL));
        }
        Page<AgentTarget> agentTargets = agentTargetRepository.findAll(genericSpecification, pageRequest);
        return agentTargets;

    }


    /** <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a country</remarks>
     */
    public void enableDisEnableState (EnableDisEnableDto request){
        AgentTarget agentTarget  = agentTargetRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent target Id does not exist!"));
        agentTarget.setIsActive(request.getIsActive());
        agentTarget.setUpdatedBy(0L);
        agentTargetRepository.save(agentTarget);

    }

    public List<MarketDto> getAllByStatus(Boolean isActive) {
        List<Market> markets = agentTargetRepository.findByIsActive(isActive);
        return markets
                .stream()
                .map(user -> mapper.map(user, MarketDto.class))
                .collect(Collectors.toList());
    }
}
