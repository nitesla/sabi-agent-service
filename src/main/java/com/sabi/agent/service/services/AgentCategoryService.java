package com.sabi.agent.service.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentCategoryDto;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentPhotoRequest;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.responseDto.AgentCategoryModResponse;
import com.sabi.agent.core.dto.responseDto.AgentCategoryResponseDto;
import com.sabi.agent.core.models.agentModel.AgentCategory;
import com.sabi.agent.core.models.agentModel.AgentCategoryTarget;
import com.sabi.agent.service.helper.GenericSpecification;
import com.sabi.agent.service.helper.SearchCriteria;
import com.sabi.agent.service.helper.SearchOperation;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.agentRepo.AgentCategoryRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentCategoryTargetRepository;
import com.sabi.framework.exceptions.BadRequestException;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AgentCategoryService {

    private AgentCategoryRepository agentCategoryRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;

    @Autowired
    private AgentCategoryTargetRepository agentCategoryTargetRepository;

    public AgentCategoryService(AgentCategoryRepository agentCategoryRepository, ModelMapper mapper, ObjectMapper objectMapper, Validations validations) {
        this.agentCategoryRepository = agentCategoryRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
    }



    /** <summary>
     * Agent category creation
     * </summary>
     * <remarks>this method is responsible for creation of new agent category</remarks>
     */

    public AgentCategoryResponseDto createAgentCategory(AgentCategoryDto request) {
        validations.validateAgentCategory(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        AgentCategory agentCategory = mapper.map(request,AgentCategory.class);
        AgentCategory catExist = agentCategoryRepository.findByName(request.getName());
        if(catExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent category already exist");
        }
        agentCategory.setCreatedBy(userCurrent.getId());
        agentCategory.setIsActive(true);
        agentCategory.setIsDefault(false);
        agentCategory = agentCategoryRepository.save(agentCategory);
        log.debug("Create new agent category - {}"+ new Gson().toJson(agentCategory));
        return mapper.map(agentCategory, AgentCategoryResponseDto.class);
    }



    /** <summary>
     * Agent category update
     * </summary>
     * <remarks>this method is responsible for updating already existing Agent category</remarks>
     */

    public AgentCategoryResponseDto updateAgentCategory(AgentCategoryDto request) {
        validations.validateAgentCategory(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        AgentCategory agentCategory = agentCategoryRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent category id does not exist!"));
        mapper.map(request, agentCategory);

        if(request.getIsDefault().equals(true)){
            AgentCategory getDefault = agentCategoryRepository.findAgentCategoriesByIsDefault(true);
            if(getDefault !=null)
                throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Default category already exist" );

        }
                agentCategory.setUpdatedBy(userCurrent.getId());
                agentCategoryRepository.save(agentCategory);
                log.debug("Agent category record updated - {}" + new Gson().toJson(agentCategory));

        return mapper.map(agentCategory, AgentCategoryResponseDto.class);
    }


    /** <summary>
     * Find AgentCategory
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */
    public AgentCategoryResponseDto findAgentCategory(Long id){
        AgentCategory agentCategory  = agentCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent category id does not exist!"));
        return mapper.map(agentCategory,AgentCategoryResponseDto.class);
    }


    /** <summary>
     * Find all AgentCategory
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */
    public Page<AgentCategory> findAll(String name,Boolean isActive ,PageRequest pageRequest ){

        GenericSpecification<AgentCategory> genericSpecification = new GenericSpecification<AgentCategory>();

        if (name != null && !name.isEmpty())
        {
            genericSpecification.add(new SearchCriteria("name", name, SearchOperation.MATCH));
        }

        if (isActive != null )
        {
            genericSpecification.add(new SearchCriteria("isActive", isActive, SearchOperation.EQUAL));
        }

        Page<AgentCategory> agentCategories = agentCategoryRepository.findAll(genericSpecification, pageRequest);


        return agentCategories;

    }


    /** <summary>
     * Enable disable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a country</remarks>
     */
    public void enableDisEnableState (EnableDisEnableDto request){
        validations.validateStatus(request.getIsActive());
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        AgentCategory agentCategory  = agentCategoryRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent category Id does not exist!"));
        agentCategory.setIsActive(request.getIsActive());
        agentCategory.setUpdatedBy(userCurrent.getId());
        agentCategoryRepository.save(agentCategory);

    }

    public List<AgentCategoryResponseDto> getAllByStatus(Boolean isActive) {
        List<AgentCategory> agentCategories = agentCategoryRepository.findByIsActive(isActive);
        return agentCategories
                .stream()
                .map(user -> mapper.map(user, AgentCategoryResponseDto.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public AgentCategoryResponseDto setDefalult(long id){
        AgentCategory agentCategory = agentCategoryRepository.findById(id).orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                "Requested Agent Bank does not exist!"));
        agentCategoryRepository.updateIsDefault();
        agentCategory.setIsDefault(true);
        AgentCategory defaultAgentCat = agentCategoryRepository.save(agentCategory);
        return mapper.map(defaultAgentCat, AgentCategoryResponseDto.class);
    }

    public List<AgentCategoryResponseDto> getDefault(){
        List<AgentCategory> agentCategories = agentCategoryRepository.findByIsDefault(true);
        return  agentCategories.stream()
                .map(agentCategory -> mapper.map(agentCategory, AgentCategoryResponseDto.class))
                .collect(Collectors.toList());
    }

    public AgentCategoryResponseDto setCategoryPhoto(AgentPhotoRequest request){
        AgentCategory agentCategory = agentCategoryRepository.findById(request.getAgentId()).orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                "Enter a valid Agent Id"));
        agentCategory.setImage(request.getImageUrl());
        log.info("Saving Agent category: " + agentCategory);
        AgentCategory saved = agentCategoryRepository.save(agentCategory);
        return mapper.map(saved, AgentCategoryResponseDto.class);
    }

    public List<AgentCategoryModResponse> getAgentCAtegoryAndTarget(){
        List<AgentCategoryModResponse> responseList = new ArrayList<>();
        List<AgentCategory> agentCategories = agentCategoryRepository.findAll();
        if(agentCategories.isEmpty())
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, "Agent category empty");

        agentCategories.forEach((agentCategory -> {
            AgentCategoryModResponse response = new AgentCategoryModResponse();
            List<AgentCategoryTarget> agentCategoryTargets = agentCategoryTargetRepository.findByAgentCategoryId(agentCategory.getId());
            response.setAgentTarget(agentCategoryTargets);
            response.setName(agentCategory.getName());
            response.setDefault(agentCategory.getIsDefault());
            response.setNextCategory(agentCategory.getNextAgentCategory());
            response.setDescription(agentCategory.getDescription());
            responseList.add(response);

        }));
        return responseList;
    }
}
