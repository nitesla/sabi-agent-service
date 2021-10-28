package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentCategoryTargetDto;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.responseDto.AgentCategoryTargetResponseDto;
import com.sabi.agent.core.models.TargetType;
import com.sabi.agent.core.models.agentModel.AgentCategory;
import com.sabi.agent.core.models.agentModel.AgentCategoryTarget;
import com.sabi.agent.service.helper.*;
import com.sabi.agent.service.repositories.TargetTypeRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentCategoryRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentCategoryTargetRepository;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.models.User;
import com.sabi.framework.service.TokenService;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@SuppressWarnings("ALL")
@Slf4j
@Service
public class AgentCategoryTargetService {

    private AgentCategoryTargetRepository agentCategoryTargetRepository;
    private AgentCategoryRepository agentCategoryRepository;
    private TargetTypeRepository targetTypeRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;
    @Autowired
    private Exists exists;


    public AgentCategoryTargetService(AgentCategoryTargetRepository agentCategoryTargetRepository, AgentCategoryRepository agentCategoryRepository, TargetTypeRepository targetTypeRepository, ModelMapper mapper, ObjectMapper objectMapper, Validations validations, Exists exists) {
        this.agentCategoryTargetRepository = agentCategoryTargetRepository;
        this.agentCategoryRepository = agentCategoryRepository;
        this.targetTypeRepository = targetTypeRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
        this.exists = exists;
    }

    /**
     * <summary>
     * AgentCategoryTarget creation
     * </summary>
     * <remarks>this method is responsible for creation of new Agent Category Target</remarks>
     */

    public AgentCategoryTargetResponseDto createAgentCategoryTarget(AgentCategoryTargetDto request) {
        validations.validateAgentCategoryTarget(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        AgentCategoryTarget agentCategoryTarget = mapper.map(request, AgentCategoryTarget.class);
        exists.agentCategoryTargetExist(request);
        AgentCategoryTarget exist = agentCategoryTargetRepository.findByName(request.getName());
        if(exist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent Category Target already exist");
        }
        exists.agentCategoryTargetExist(request);
        agentCategoryTarget.setCreatedBy(userCurrent.getId());
        agentCategoryTarget.setIsActive(false);
        agentCategoryTarget = agentCategoryTargetRepository.save(agentCategoryTarget);
        log.debug("Create new Agent Category Target - {}" + new Gson().toJson(agentCategoryTarget));
        //AgentCategoryTargetResponseDto map = mapper.map(agentCategoryTarget, AgentCategoryTargetResponseDto.class);
        //        AgentCategory agentCategory = agentCategoryRepository.findById(agentCategoryTarget.getAgentCategoryId()).orElseThrow(()->
        //                new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
        //                        "Requested Agent Category Target does not exist!"));
        return mapper.map(agentCategoryTarget, AgentCategoryTargetResponseDto.class);
    }


    /**
     * <summary>
     * Agent Category Target update
     * </summary>
     * <remarks>this method is responsible for updating already existing Agent Category Target</remarks>
     */

    public AgentCategoryTargetResponseDto updateAgentCategoryTarget(AgentCategoryTargetDto request) {
        validations.validateAgentCategoryTarget(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        AgentCategoryTarget agentCategoryTarget = agentCategoryTargetRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Agent Category Target does not exist!"));
        mapper.map(request, agentCategoryTarget);
        agentCategoryTarget.setUpdatedBy(userCurrent.getId());
        exists.agentCategoryTargetExist(request);
        exists.agentCategoryTargetUpateExist(request);
        agentCategoryTargetRepository.save(agentCategoryTarget);
        log.debug("Agent Category Target record updated - {}" + new Gson().toJson(agentCategoryTarget));
        return mapper.map(agentCategoryTarget, AgentCategoryTargetResponseDto.class);
    }


    /**
     * <summary>
     * Find Agent Category Target
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */
//    public AgentCategoryTargetResponseDto findAgentCategoryTarget(Long id) {
//        AgentCategoryTarget agentCategoryTarget = agentCategoryTargetRepository.findById(id)
//                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
//                        "Requested Agent Category Target Id does not exist!"));
//
//        AgentCategory agentCategory = agentCategoryRepository.findById(agentCategoryTarget.getAgentCategoryId())
//                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
//                        " Enter a valid Agent Category!"));
//
//        TargetType targetType = targetTypeRepository.findById(agentCategoryTarget.getTargetTypeId())
//                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
//                        " Enter a valid Target Type!"));
//        AgentCategoryTargetResponseDto response = AgentCategoryTargetResponseDto.builder()
//                .id(agentCategoryTarget.getId())
//                .name(agentCategoryTarget.getName())
//                .agentCategoryId(agentCategoryTarget.getAgentCategoryId())
//                .targetTypeId(agentCategoryTarget.getTargetTypeId())
//                .min(agentCategoryTarget.getMin())
//                .max(agentCategoryTarget.getMax())
//                .superMax(agentCategoryTarget.getSuperMax())
//                .createdDate(agentCategoryTarget.getCreatedDate())
//                .createdBy(agentCategoryTarget.getCreatedBy())
//                .updatedBy(agentCategoryTarget.getUpdatedBy())
//                .updatedDate(agentCategoryTarget.getUpdatedDate())
//                .isActive(agentCategoryTarget.isActive())
//                .build();
//
//        return response;
//    }

    public AgentCategoryTargetResponseDto findAgentCategoryTarget(Long id){
        validations.validateId(id);
        AgentCategoryTarget agentCategoryTarget = agentCategoryTargetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Agent Category Target Id does not exist!"));

        AgentCategory agentCategory = agentCategoryRepository.findById(agentCategoryTarget.getAgentCategoryId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Agent Category!"));
        TargetType targetType = targetTypeRepository.findById(agentCategoryTarget.getTargetTypeId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Target Type!"));
        return mapper.map(agentCategoryTarget,AgentCategoryTargetResponseDto.class);
    }


    /**
     * <summary>
     * Find all Agent Category Target
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */


    public Page<AgentCategoryTarget> findAll(String name, Boolean isActive, Integer min, Integer max, Integer superMax, PageRequest pageRequest) {

        GenericSpecification<AgentCategoryTarget> genericSpecification = new GenericSpecification<AgentCategoryTarget>();

        if (name != null && !name.isEmpty()) {
            genericSpecification.add(new SearchCriteria("name", name, SearchOperation.MATCH));
        }

        if (isActive != null) {
            genericSpecification.add(new SearchCriteria("isActive", isActive, SearchOperation.EQUAL));
        }

        if (min != null) {
            genericSpecification.add(new SearchCriteria("min", min, SearchOperation.EQUAL));
        }

        if (max != null) {
            genericSpecification.add(new SearchCriteria("max", max, SearchOperation.EQUAL));
        }

        if (superMax != null) {
            genericSpecification.add(new SearchCriteria("superMax", superMax, SearchOperation.EQUAL));
        }

        Page<AgentCategoryTarget> agentCategoryTargets = agentCategoryTargetRepository.findAll(genericSpecification, pageRequest);

        if (agentCategoryTargets == null) {
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }

        return agentCategoryTargets;

    }


    /**
     * <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a Agent Category Target</remarks>
     */
    public void enableDisableAgtCatTarget(EnableDisEnableDto request) {
        validations.validateStatus(request.isActive());
//        validations.validateAgentCategoryTaskEnable(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
            AgentCategoryTarget agentCategoryTarget = agentCategoryTargetRepository.findById(request.getId())
                    .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                            "Requested Agent Category Target does not exist!"));
            agentCategoryTarget.setIsActive(request.isActive());
            agentCategoryTarget.setUpdatedBy(userCurrent.getId());
            agentCategoryTargetRepository.save(agentCategoryTarget);

    }

    public List<AgentCategoryTarget> getAll(Boolean isActive) {
        List<AgentCategoryTarget> agentCategoryTargetList = agentCategoryTargetRepository.findByIsActive(isActive);
        return agentCategoryTargetList;

    }

    public List<AgentCategoryTargetResponseDto> getByAgentCatID(long catId) {
        agentCategoryRepository.findById(catId).orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                " Agent Category does not exist!"));
        List<AgentCategoryTarget> targets = agentCategoryTargetRepository.findByAgentCategoryId(catId);
        return targets.stream()
                .map(agentCategoryTarget -> mapper.map(agentCategoryTarget, AgentCategoryTargetResponseDto.class))
                .collect(Collectors.toList());
    }
}
