package com.sabi.agent.service.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentNetworkDto;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.responseDto.AgentNetworkResponseDto;
import com.sabi.agent.core.models.agentModel.AgentNetwork;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.agentRepo.AgentNetworkRepository;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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



    /** <summary>
     * Agent category creation
     * </summary>
     * <remarks>this method is responsible for creation of new agent category</remarks>
     */

    public AgentNetworkResponseDto createAgentNetwork(AgentNetworkDto request) {
        validations.validateAgentNetwork(request);
        AgentNetwork agentNetwork = mapper.map(request, AgentNetwork.class);
        AgentNetwork catExist = agentNetworkRepository.findByAgentId(request.getAgentId());
        if(catExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent category already exist");
        }
        agentNetwork.setCreatedBy(0L);
        agentNetwork.setIsActive(true);
        agentNetwork = agentNetworkRepository.save(agentNetwork);
        log.debug("Create new agent category - {}"+ new Gson().toJson(agentNetwork));
        return mapper.map(agentNetwork, AgentNetworkResponseDto.class);
    }



    /** <summary>
     * Agent category update
     * </summary>
     * <remarks>this method is responsible for updating already existing Agent category</remarks>
     */

    public AgentNetworkResponseDto updateAgentNetwork(AgentNetworkDto request) {
        validations.validateAgentNetwork(request);
        AgentNetwork agentNetwork = agentNetworkRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent category id does not exist!"));
        mapper.map(request, agentNetwork);
        agentNetwork.setUpdatedBy(0L);
        agentNetworkRepository.save(agentNetwork);
        log.debug("Agent category record updated - {}"+ new Gson().toJson(agentNetwork));
        return mapper.map(agentNetwork, AgentNetworkResponseDto.class);
    }


    /** <summary>
     * Find agentNetwork
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */
    public AgentNetworkResponseDto findAgentNetwork(Long id){
        AgentNetwork agentNetwork  = agentNetworkRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent category id does not exist!"));
        return mapper.map(agentNetwork, AgentNetworkResponseDto.class);
    }


    /** <summary>
     * Find all agentNetwork
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */
    public Page<AgentNetwork> findAll(Long agentId, Boolean isActive , PageRequest pageRequest ){
        Page<AgentNetwork> agentNetworks = agentNetworkRepository.findAgentNetwork(agentId, isActive, pageRequest);
        if(agentNetworks == null){
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        return agentNetworks;

    }


    /** <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a country</remarks>
     */
    public void enableDisEnableState (EnableDisEnableDto request){
        AgentNetwork agentNetwork  = agentNetworkRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent category Id does not exist!"));
        agentNetwork.setIsActive(request.getIsActive());
        agentNetwork.setUpdatedBy(0L);
        agentNetworkRepository.save(agentNetwork);

    }
}
