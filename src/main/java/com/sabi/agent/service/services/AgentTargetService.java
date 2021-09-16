package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentTargetDto;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.responseDto.AgentTargetResponseDto;
import com.sabi.agent.core.models.agentModel.AgentTarget;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.agentRepo.AgentTargetRepository;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
     * Agent category creation
     * </summary>
     * <remarks>this method is responsible for creation of new agent category</remarks>
     */

    public AgentTargetResponseDto createAgentTarget(AgentTargetDto request) {
        validations.validateAgentTarget(request);
        AgentTarget agentTarget = mapper.map(request, AgentTarget.class);
        Optional<AgentTarget> agentTargetExist = agentTargetRepository.findById(request.getId());
        if(agentTargetExist.isPresent()){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent category already exist");
        }
        agentTarget.setCreatedBy(0L);
        agentTarget.setIsActive(true);
        agentTarget = agentTargetRepository.save(agentTarget);
        log.debug("Create new agent category - {}"+ new Gson().toJson(agentTarget));
        return mapper.map(agentTarget, AgentTargetResponseDto.class);
    }



    /** <summary>
     * Agent category update
     * </summary>
     * <remarks>this method is responsible for updating already existing Agent category</remarks>
     */

    public AgentTargetResponseDto updateAgentTarget(AgentTargetDto request) {
        validations.validateAgentTarget(request);
        AgentTarget agentTarget = agentTargetRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent category id does not exist!"));
        mapper.map(request, agentTarget);
        agentTarget.setUpdatedBy(0L);
        agentTargetRepository.save(agentTarget);
        log.debug("Agent category record updated - {}"+ new Gson().toJson(agentTarget));
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
                        "Requested agent category id does not exist!"));
        return mapper.map(agentTarget, AgentTargetResponseDto.class);
    }


    /** <summary>
     * Find all agentTarget
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */
    public Page<AgentTarget> findAll(String name, Boolean isActive , PageRequest pageRequest ){
        Page<AgentTarget> agentTargets = agentTargetRepository.findAgentTarget(name, isActive, pageRequest);
        if(agentTargets == null){
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
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
                        "Requested agent category Id does not exist!"));
        agentTarget.setIsActive(request.getIsActive());
        agentTarget.setUpdatedBy(0L);
        agentTargetRepository.save(agentTarget);

    }


    public List<AgentTarget> getAll(Boolean isActive){
        List<AgentTarget> agentTargets = agentTargetRepository.findByIsActive(isActive);
        return agentTargets;

    }
}
