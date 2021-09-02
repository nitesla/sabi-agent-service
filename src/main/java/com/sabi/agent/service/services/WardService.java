package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.requestDto.WardDto;
import com.sabi.agent.core.dto.responseDto.WardResponseDto;
import com.sabi.agent.core.models.LGA;
import com.sabi.agent.core.models.Ward;
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

    public WardResponseDto updateWard(WardDto request) {
        validations.validateWard(request);
        Ward ward = wardRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Ward Id does not exist!"));
        mapper.map(request, ward);
        ward.setUpdatedBy(0l);
        wardRepository.save(ward);
        log.debug("LGA record updated - {}" + new Gson().toJson(ward));
        return mapper.map(ward, WardResponseDto.class);
    }

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

    public Page<Ward> findAll(String name, PageRequest pageRequest ) {
        Page<Ward> wards = wardRepository.findWards(name, pageRequest);
        if (wards == null) {
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        return wards;

    }

    public void enableDisEnableWard (EnableDisEnableDto request){
        Ward ward = wardRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Ward Id does not exist!"));
        ward.setIsActive(request.getIsActive());
        ward.setUpdatedBy(0l);
        wardRepository.save(ward);

    }
}
