package com.sabi.agent.service.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sabi.agent.core.dto.agentDto.requestDto.Verification;
import com.sabi.agent.core.models.agentModel.Agent;
import com.sabi.agent.core.models.agentModel.AgentVerification;
import com.sabi.agent.service.repositories.agentRepo.AgentRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentVerificationRepository;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Date;


@Slf4j
@Service
public class AgentVerificationService {



    private AgentRepository agentRepository;
    private AgentVerificationRepository agentVerificationRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;

    public AgentVerificationService(AgentRepository agentRepository,AgentVerificationRepository agentVerificationRepository,
                                    ModelMapper mapper, ObjectMapper objectMapper) {
        this.agentRepository = agentRepository;
        this.agentVerificationRepository = agentVerificationRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }




    /** <summary>
     * Find all Agent verification
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */

    public Page<AgentVerification> findAll(Long agentId, PageRequest pageRequest ) {
        Page<AgentVerification> agentVerifications = agentVerificationRepository.agentsDetailsForVerification(agentId,pageRequest);
        if (agentVerifications == null) {
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        return agentVerifications;

    }


    /** <summary>
     * Verify agent details
     * </summary>
     * <remarks>this method is responsible for verifying agent details </remarks>
     */
    public void verification (Verification request){
        AgentVerification agentVerification = agentVerificationRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested id does not exist!"));
        agentVerification.setStatus(1);
        agentVerification.setDateVerified(new Date());
        agentVerification.setVerifierId(0l);
        agentVerification = agentVerificationRepository.save(agentVerification);

            Agent agent = agentRepository.findById(agentVerification.getAgentId())
                    .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                            "Requested agent id does not exist!"));

            agent.setVerificationStatus(1);
            agentRepository.save(agent);
    }


    /** <summary>
     * Change verification status
     * </summary>
     * <remarks>this method is responsible for changing verification status </remarks>
     */

    public void changeVerificationStatus (Verification request){
        AgentVerification agentVerification = agentVerificationRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested id does not exist!"));
        agentVerification.setStatus(0);
        agentVerification.setDateVerified(new Date());
        agentVerification.setVerifierId(0l);
        agentVerification = agentVerificationRepository.save(agentVerification);

        Agent agent = agentRepository.findById(agentVerification.getAgentId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent id does not exist!"));

        agent.setVerificationStatus(0);
        agentRepository.save(agent);
    }



}
