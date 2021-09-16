package com.sabi.agent.service.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.requestDto.IdTypeDto;
import com.sabi.agent.core.dto.responseDto.IdTypeResponseDto;
import com.sabi.agent.core.models.IdType;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.IdTypeRepository;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class IdTypeService {

    private IdTypeRepository idTypeRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;

    public IdTypeService(IdTypeRepository idTypeRepository, ModelMapper mapper, ObjectMapper objectMapper, Validations validations) {
        this.idTypeRepository = idTypeRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
    }


    /** <summary>
     * IdType creation
     * </summary>
     * <remarks>this method is responsible for creation of new country</remarks>
     */

    public IdTypeResponseDto createIdType(IdTypeDto request) {
        validations.validateIdType(request);
        IdType idType = mapper.map(request,IdType.class);
        IdType idExist = idTypeRepository.findByName(request.getName());
        if(idExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " IdType already exist");
        }
        idType.setCreatedBy(0l);
        idType.setIsActive(true);
        idType = idTypeRepository.save(idType);
        log.debug("Create new IdType - {}"+ new Gson().toJson(idType));
        return mapper.map(idType, IdTypeResponseDto.class);
    }


    /** <summary>
     * IdType update
     * </summary>
     * <remarks>this method is responsible for updating already existing idType</remarks>
     */

    public IdTypeResponseDto updateIdType(IdTypeDto request) {
        validations.validateIdType(request);
        IdType idType = idTypeRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Id type does not exist!"));
        mapper.map(request, idType);
        idType.setUpdatedBy(0l);
        idTypeRepository.save(idType);
        log.debug("Id Type record updated - {}"+ new Gson().toJson(idType));
        return mapper.map(idType, IdTypeResponseDto.class);
    }



    /** <summary>
     * Find IdType
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */
    public IdTypeResponseDto findIdType(Long id){
        IdType idType  = idTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Id type does not exist!"));
        return mapper.map(idType,IdTypeResponseDto.class);
    }



    /** <summary>
     * Find all Id Type
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */
    public Page<IdType> findAll(String name, PageRequest pageRequest ){
        Page<IdType> idTypes = idTypeRepository.findIdTypes(name,pageRequest);
        if(idTypes == null){
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        return idTypes;

    }




    /** <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a country</remarks>
     */
    public void enableDisEnableState (EnableDisEnableDto request){
        IdType idType  = idTypeRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Id type does not exist!"));
        idType.setIsActive(request.getIsActive());
        idType.setUpdatedBy(0L);
        idTypeRepository.save(idType);

    }


    public List<IdType> getAll(Boolean isActive){
        List<IdType> idTypes = idTypeRepository.findByIsActive(isActive);
        return idTypes;

    }

}
