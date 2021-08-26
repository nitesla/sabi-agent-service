package com.sabi.agent.service.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentCategoryDto;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.responseDto.AgentCategoryResponseDto;
import com.sabi.agent.core.models.agentModel.AgentCategory;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.agentRepo.AgentCategoryRepository;
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
public class AgentCategoryService {

    private AgentCategoryRepository agentCategoryRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;

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
        AgentCategory agentCategory = mapper.map(request,AgentCategory.class);
        AgentCategory catExist = agentCategoryRepository.findByName(request.getName());
        if(catExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent category already exist");
        }
        agentCategory.setCreatedBy(0l);
        agentCategory.setIsActive(true);
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
        AgentCategory agentCategory = agentCategoryRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent category id does not exist!"));
        mapper.map(request, agentCategory);
        agentCategory.setUpdatedBy(0l);
        agentCategoryRepository.save(agentCategory);
        log.debug("Agent category record updated - {}"+ new Gson().toJson(agentCategory));
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
    public Page<AgentCategory> findAll(String name, PageRequest pageRequest ){
        Page<AgentCategory> agentCategories = agentCategoryRepository.findAgentCategories(name,pageRequest);
        if(agentCategories == null){
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        return agentCategories;

    }


    /** <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a country</remarks>
     */
    public void enableDisEnableState (EnableDisEnableDto request){
        AgentCategory agentCategory  = agentCategoryRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent category Id does not exist!"));
        agentCategory.setIsActive(request.getIsActive());
        agentCategory.setUpdatedBy(0l);
        agentCategoryRepository.save(agentCategory);

    }
}
