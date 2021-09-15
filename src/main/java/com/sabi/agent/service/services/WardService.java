package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.requestDto.WardDto;
import com.sabi.agent.core.dto.responseDto.WardResponseDto;
import com.sabi.agent.core.models.LGA;
import com.sabi.agent.core.models.Ward;
import com.sabi.agent.service.helper.GenericSpecification;
import com.sabi.agent.service.helper.SearchCriteria;
import com.sabi.agent.service.helper.SearchOperation;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.LGARepository;
import com.sabi.agent.service.repositories.WardRepository;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@SuppressWarnings("ALL")
@Slf4j
@Service
public class WardService {
    private WardRepository wardRepository;
    private LGARepository lgaRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;

    public WardService(WardRepository wardRepository, LGARepository lgaRepository, ModelMapper mapper, ObjectMapper objectMapper, Validations validations) {
        this.wardRepository = wardRepository;
        this.wardRepository = wardRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
    }

    /** <summary>
     * Ward creation
     * </summary>
     * <remarks>this method is responsible for creation of new Ward</remarks>
     */

    public WardResponseDto createWard(WardDto request) {
        validations.validateWard(request);
        Ward ward = mapper.map(request,Ward.class);
        Ward wardExist = wardRepository.findByName(request.getName());
        if(wardExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Ward already exist");
        }
        ward.setCreatedBy(0l);
        ward.setIsActive(true);
        ward = wardRepository.save(ward);
        log.debug("Create new Ward - {}"+ new Gson().toJson(ward));
        return mapper.map(ward, WardResponseDto.class);
    }

    /** <summary>
     * Ward update
     * </summary>
     * <remarks>this method is responsible for updating already existing Ward</remarks>
     */
    public WardResponseDto updateWard(WardDto request) {
        validations.validateWard(request);
        Ward ward = wardRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Ward Id does not exist!"));
        mapper.map(request, ward);
        ward.setUpdatedBy(0l);
        Ward wardExist = wardRepository.findByName(ward.getName());
        if(wardExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Ward name already exist");
        }
        wardRepository.save(ward);
        log.debug("Ward record updated - {}" + new Gson().toJson(ward));
        return mapper.map(ward, WardResponseDto.class);
    }

    /** <summary>
     * Find Ward
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */

    public WardResponseDto findWard(Long id){
        Ward ward = wardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Ward Id does not exist!"));
        LGA lga = lgaRepository.findById(ward.getLgaId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " LGA Id does not exist!"));
        WardResponseDto response = WardResponseDto.builder()
                .id(ward.getId())
                .name(ward.getName())
                .lgaId(ward.getLgaId())
                .lga(lga.getName())
                .createdDate(ward.getCreatedDate())
                .createdBy(ward.getCreatedBy())
                .updatedBy(ward.getUpdatedBy())
                .updatedDate(ward.getUpdatedDate())
                .isActive(ward.getIsActive())
                .build();
        return response;
    }

    /** <summary>
     * Find all Ward
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */
    public Page<Ward> findAll(String name, Boolean isActive, Long lgaId, PageRequest pageRequest ) {
        GenericSpecification<Ward> genericSpecification = new GenericSpecification<Ward>();

        if (name != null && !name.isEmpty())
        {
            genericSpecification.add(new SearchCriteria("name", name, SearchOperation.MATCH));
        }

        if (isActive != null )
        {
            genericSpecification.add(new SearchCriteria("isActive", isActive, SearchOperation.EQUAL));
        }

        if (lgaId != null)
        {
            genericSpecification.add(new SearchCriteria("lgaId", lgaId, SearchOperation.EQUAL));
        }

        Page<Ward> wards = wardRepository.findAll(genericSpecification, pageRequest);
        if (wards == null) {
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        return wards;

    }

    /** <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a Ward</remarks>
     */
    public void enableDisableWard (EnableDisEnableDto request){
        Ward ward = wardRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Ward Id does not exist!"));
        ward.setIsActive(request.getIsActive());
        ward.setUpdatedBy(0l);
        wardRepository.save(ward);

    }

    public List<Ward> getAll(Boolean isActive){
        List<Ward> wardList = wardRepository.findByIsActive(isActive);
        return wardList;

    }
}
