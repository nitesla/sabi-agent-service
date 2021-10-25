package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.requestDto.CreditLevelDto;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.responseDto.CreditLevelResponseDto;
import com.sabi.agent.core.models.CreditLevel;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.CreditLevelRepository;
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

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class CreditLevelService {
    private CreditLevelRepository creditLevelRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;



    public CreditLevelService(CreditLevelRepository creditLevelRepository, ModelMapper mapper, ObjectMapper objectMapper, Validations validations) {
        this.creditLevelRepository = creditLevelRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
    }

    /** <summary>
     * creditLevel creation
     * </summary>
     * <remarks>this method is responsible for creation of new CreditLevel</remarks>
     */

    public CreditLevelResponseDto createCreditLevel(CreditLevelDto request) {
        validations.validateCreditLevel(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        CreditLevel creditLevel = mapper.map(request, CreditLevel.class);
        CreditLevel creditLevelExist = creditLevelRepository.findCreditLevelByLimits(request.getLimits());
        if(creditLevelExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " creditLevel already exist");
        }
        creditLevel.setCreatedBy(userCurrent.getId());
        creditLevel.setActive(true);
        creditLevel = creditLevelRepository.save(creditLevel);
        log.debug("Create new creditLevel - {}"+ new Gson().toJson(creditLevel));
        return mapper.map(creditLevel, CreditLevelResponseDto.class);
    }

    public CreditLevelDto updateCreditLevel(CreditLevelDto request) {
        validations.validateCreditLevel(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        CreditLevel creditLevel = creditLevelRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested credit level Id does not exist!"));
        mapper.map(request, creditLevel);
        creditLevel.setUpdatedBy(userCurrent.getId());
        creditLevelRepository.save(creditLevel);
        log.debug("Country record updated - {}"+ new Gson().toJson(creditLevel));
        return mapper.map(creditLevel, CreditLevelDto.class);
    }

    /** <summary>
     * Find creditLevel
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */
    public CreditLevelResponseDto findCreditLevel(Long id){
        CreditLevel creditLevel  = creditLevelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested creditLevel id does not exist!"));
        return mapper.map(creditLevel, CreditLevelResponseDto.class);
    }

    /** <summary>
     * Find all creditLevels
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */
    public Page<CreditLevel> findAll(BigDecimal limits,Boolean isActive, PageRequest pageRequest ){
        Page<CreditLevel> creditLevel = creditLevelRepository.findCreditLevel(limits, isActive, pageRequest);
        if(creditLevel == null){
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        return creditLevel;

    }

    /** <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a creditLevel</remarks>
     */
    public void enableDisEnableState (EnableDisEnableDto request){
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        CreditLevel creditLevel  = creditLevelRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested creditLevel id does not exist!"));
        creditLevel.setActive(request.isActive());
        creditLevel.setUpdatedBy(userCurrent.getId());
        creditLevelRepository.save(creditLevel);

    }


    public List<CreditLevel> getAll(Boolean isActive){
        List<CreditLevel> creditLevel = creditLevelRepository.findByIsActive(isActive);
        return creditLevel;

    }


}
