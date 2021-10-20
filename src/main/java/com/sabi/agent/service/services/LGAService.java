package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.requestDto.LGADto;
import com.sabi.agent.core.dto.responseDto.LGAResponseDto;
import com.sabi.agent.core.models.LGA;
import com.sabi.agent.core.models.State;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.LGARepository;
import com.sabi.agent.service.repositories.StateRepository;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.models.User;
import com.sabi.framework.service.TokenService;
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
public class LGAService {

    private LGARepository lgaRepository;
    private StateRepository stateRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;

    public LGAService(LGARepository lgaRepository,StateRepository stateRepository, ModelMapper mapper, ObjectMapper objectMapper,Validations validations) {
        this.lgaRepository = lgaRepository;
        this.stateRepository = stateRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
    }


    /** <summary>
     * LGA creation
     * </summary>
     * <remarks>this method is responsible for creation of new LGA</remarks>
     */

    public LGAResponseDto createLga(LGADto request) {
        validations.validateLGA(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        LGA lga = mapper.map(request,LGA.class);
        LGA lgaExist = lgaRepository.findByName(request.getName());
        if(lgaExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " LGA already exist");
        }
        lga.setCreatedBy(userCurrent.getId());
        lga.setActive(true);
        lga = lgaRepository.save(lga);
        log.debug("Create new LGA - {}"+ new Gson().toJson(lga));
        return mapper.map(lga, LGAResponseDto.class);
    }



    /** <summary>
     * LGA update
     * </summary>
     * <remarks>this method is responsible for updating already existing LGA</remarks>
     */

    public LGAResponseDto updateLga(LGADto request) {
        validations.validateLGA(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        LGA lga = lgaRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested State Id does not exist!"));
        mapper.map(request, lga);
        lga.setUpdatedBy(userCurrent.getId());
        lgaRepository.save(lga);
        log.debug("LGA record updated - {}" + new Gson().toJson(lga));
        return mapper.map(lga, LGAResponseDto.class);
    }





    /** <summary>
     * Find LGA
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */
    public LGAResponseDto findLga(Long id){
        LGA lga = lgaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested LGA Id does not exist!"));
        State state = stateRepository.findById(lga.getStateId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " State Id does not exist!"));
        LGAResponseDto response = LGAResponseDto.builder()
                .id(lga.getId())
                .name(lga.getName())
                .stateId(lga.getStateId())
                .state(state.getName())
                .createdDate(lga.getCreatedDate())
                .createdBy(lga.getCreatedBy())
                .updatedBy(lga.getUpdatedBy())
                .updatedDate(lga.getUpdatedDate())
                .isActive(lga.isActive())
                .build();
        return response;
    }



    /** <summary>
     * Find all LGA
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */




    public Page<LGA> findAll(String name, PageRequest pageRequest ) {
        Page<LGA> lga = lgaRepository.findLgas(name, pageRequest);
        if (lga == null) {
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        return lga;

    }


    /** <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a country</remarks>
     */
    public void enableDisEnableState (EnableDisEnableDto request){
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        LGA lga = lgaRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested LGA Id does not exist!"));
        lga.setActive(request.isActive());
        lga.setUpdatedBy(userCurrent.getId());
        lgaRepository.save(lga);

    }


    public List<LGA> getAll(Boolean isActive){
        List<LGA> lga = lgaRepository.findByIsActive(isActive);
        return lga;

    }
}
