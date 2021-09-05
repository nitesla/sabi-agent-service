package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.responseDto.AgentSupervisorResponseDto;
import com.sabi.agent.core.models.agentModel.AgentSupervisor;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.agentRepo.AgentSupervisorRepository;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@SuppressWarnings("ALL")
@Slf4j
@Service
public class AgentSupervisorService {

    private AgentSupervisorRepository agentSupervisorRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;

    public AgentSupervisorService(AgentSupervisorRepository agentSupervisorRepository,
                                  ModelMapper mapper, ObjectMapper objectMapper, Validations validations) {
        this.agentSupervisorRepository = agentSupervisorRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
    }

    public AgentSupervisorResponseDto createAgentSupervisor(com.sabi.agent.core.dto.agentDto.requestDto.AgentSupervisor request) {
        validations.validateAgentSupervisor(request);
        AgentSupervisor agentSupervisor = mapper.map(request, AgentSupervisor.class);
//todo:
//        Optional<AgentSupervisor> agentSupervisorExist = agentSupervisorRepository.findById(request.getId());
//        if(agentSupervisorExist.isPresent()){
//            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " agentSupervisor already exist");
//        }
        agentSupervisor.setCreatedBy(0L);
        agentSupervisor.setIsActive(true);
        agentSupervisor = agentSupervisorRepository.save(agentSupervisor);
        log.debug("Create new agentSupervisor - {}"+ new Gson().toJson(agentSupervisor));
        return mapper.map(agentSupervisor, AgentSupervisorResponseDto.class);
    }



    /** <summary>
     * agentSupervisor update
     * </summary>
     * <remarks>this method is responsible for updating already existing agentSupervisor</remarks>
     */

    public AgentSupervisorResponseDto updateAgentSupervisor(com.sabi.agent.core.dto.agentDto.requestDto.AgentSupervisor request) {
        validations.validateAgentSupervisor(request);
        AgentSupervisor agentSupervisor = agentSupervisorRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agentSupervisor id does not exist!"));
        mapper.map(request, agentSupervisor);
        agentSupervisor.setUpdatedBy(0L);
        agentSupervisorRepository.save(agentSupervisor);
        log.debug("agentSupervisor record updated - {}"+ new Gson().toJson(agentSupervisor));
        return mapper.map(agentSupervisor, AgentSupervisorResponseDto.class);
    }



    /** <summary>
     * Find agentSupervisor
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */
    public AgentSupervisorResponseDto findAgentSupervisor(Long id){
        AgentSupervisor agentSupervisor  = agentSupervisorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agentSupervisor id does not exist!"));
        return mapper.map(agentSupervisor, AgentSupervisorResponseDto.class);
    }


    /** <summary>
     * Find all agentSupervisors
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */
    public Page<AgentSupervisor> findAll(String agentType, PageRequest pageRequest){
        Page<AgentSupervisor> agentSupervisor = agentSupervisorRepository
                .findAll(pageRequest);
        if(agentSupervisor == null){
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        return agentSupervisor;
    }


    /** <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a country</remarks>
     */
    public void enableDisEnableState (EnableDisEnableDto request){
        AgentSupervisor agentSupervisor  = agentSupervisorRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agentSupervisor Id does not exist!"));
        agentSupervisor.setIsActive(request.getIsActive());
        agentSupervisor.setUpdatedBy(0L);
        agentSupervisorRepository.save(agentSupervisor);

    }
}
