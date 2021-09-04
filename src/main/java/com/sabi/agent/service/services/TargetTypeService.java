package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.requestDto.TargetTypeDto;
import com.sabi.agent.core.dto.responseDto.TargetTypeResponseDto;
import com.sabi.agent.core.models.TargetType;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.TargetTypeRepository;
import com.sabi.framework.exceptions.ConflictException;
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
public class TargetTypeService {
    private TargetTypeRepository targetTypeRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;

    public TargetTypeService(TargetTypeRepository targetTypeRepository, ModelMapper mapper, ObjectMapper objectMapper, Validations validations) {
        this.targetTypeRepository = targetTypeRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
    }

    /** <summary>
     * TargetType creation
     * </summary>
     * <remarks>this method is responsible for creation of new TargetType</remarks>
     */

    public TargetTypeResponseDto createTargetType(TargetTypeDto request) {
        validations.validateTargetType(request);
        TargetType targetType = mapper.map(request,TargetType.class);
        TargetType targetTypeExist = targetTypeRepository.findByName(request.getName());
        if(targetTypeExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Ward already exist");
        }
        targetType.setCreatedBy(0l);
        targetType.setIsActive(true);
        targetType = targetTypeRepository.save(targetType);
        log.debug("Create new Target Type - {}"+ new Gson().toJson(targetType));
        return mapper.map(targetType, TargetTypeResponseDto.class);
    }

    /** <summary>
     * TargetType update
     * </summary>
     * <remarks>this method is responsible for updating already existing TargetType</remarks>
     */

    public TargetTypeResponseDto updateTargetType(TargetTypeDto request) {
        validations.validateTargetType(request);
        TargetType targetType = targetTypeRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Target Type Id does not exist!"));
        mapper.map(request, targetType);
        targetType.setUpdatedBy(0l);
        targetTypeRepository.save(targetType);
        log.debug("Target Type record updated - {}" + new Gson().toJson(targetType));
        return mapper.map(targetType, TargetTypeResponseDto.class);
    }

    /** <summary>
     * Find TargetType
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */

    public TargetTypeResponseDto findTargetType(Long id){
        TargetType targetType = targetTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Target Type Id does not exist!"));

        TargetTypeResponseDto response = TargetTypeResponseDto.builder()
                .id(targetType.getId())
                .name(targetType.getName())
                .createdDate(targetType.getCreatedDate())
                .createdBy(targetType.getCreatedBy())
                .updatedBy(targetType.getUpdatedBy())
                .updatedDate(targetType.getUpdatedDate())
                .isActive(targetType.getIsActive())
                .build();
        return response;
    }
    /** <summary>
     * Find all TargetType
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */

    public Page<TargetType> findAll(String name, PageRequest pageRequest ) {
        Page<TargetType> targetTypes = targetTypeRepository.findTargetTypes(name, pageRequest);
        if (targetTypes == null) {
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        return targetTypes;

    }
    /** <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a TargetType</remarks>
     */

    public void enableDisableTargetType (EnableDisEnableDto request){
        TargetType targetType = targetTypeRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Target Type does not exist!"));
        targetType.setIsActive(request.getIsActive());
        targetType.setUpdatedBy(0l);
        targetTypeRepository.save(targetType);

    }
}
