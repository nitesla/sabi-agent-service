package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.requestDto.CreditLevelDto;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.responseDto.CreditLevelResponseDto;
import com.sabi.agent.core.models.CreditLevel;
import com.sabi.agent.core.models.Market;
import com.sabi.agent.service.helper.GenericSpecification;
import com.sabi.agent.service.helper.SearchCriteria;
import com.sabi.agent.service.helper.SearchOperation;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.CreditLevelRepository;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//ken
@Slf4j
@Service
public class CreditLevelService {
    private final CreditLevelRepository creditLevelRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;


    public CreditLevelService(CreditLevelRepository creditLevelRepository, ModelMapper mapper, ObjectMapper objectMapper, Validations validations) {
        this.creditLevelRepository = creditLevelRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
    }

    /**
     * <summary>
     * creditLevel creation
     * </summary>
     * <remarks>this method is responsible for creation of new CreditLevel</remarks>
     */

    public CreditLevelResponseDto createCreditLevel(CreditLevelDto request) {
        validations.validateCreditLevel(request);
        CreditLevel creditLevel = mapper.map(request, CreditLevel.class);
//        GenericSpecification<CreditLevel> genericSpecification = new GenericSpecification<CreditLevel>();
//        genericSpecification.add(new SearchCriteria("limits", request.getLimits(), SearchOperation.EQUAL));
//        genericSpecification.add(new SearchCriteria("repaymentPeriod", request.getRepaymentPeriod(), SearchOperation.EQUAL));
//        genericSpecification.add(new SearchCriteria("agentCategoryId", request.getAgentCategoryId(), SearchOperation.EQUAL));
//        CreditLevel creditLevelExist = creditLevelRepository.findOne(genericSpecification).orElseThrow(() ->
//         new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " creditLevel already exist"));
        boolean exists = creditLevelRepository.exists(Example.of(creditLevel));
        if(exists) throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " creditLevel already exist");
        creditLevel.setCreatedBy(0L);
        creditLevel.setIsActive(false);
        creditLevel = creditLevelRepository.save(creditLevel);
        log.debug("Create new creditLevel - {}" + new Gson().toJson(creditLevel));
        return mapper.map(creditLevel, CreditLevelResponseDto.class);
    }

    public CreditLevelDto updateCreditLevel(CreditLevelDto request) {
        validations.validateCreditLevel(request);
        CreditLevel creditLevel = creditLevelRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Credit level Id does not exist!"));
        mapper.map(request, creditLevel);
        boolean exists = creditLevelRepository.exists(Example.of(creditLevel));
        if (exists) throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " creditLevel already exist");
        creditLevel.setUpdatedBy(0L);
        creditLevelRepository.save(creditLevel);
        log.debug("Credit level record updated - {}" + new Gson().toJson(creditLevel));
        return mapper.map(creditLevel, CreditLevelDto.class);
    }

    /**
     * <summary>
     * Find creditLevel
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */
    public CreditLevelResponseDto findCreditLevel(Long id) {
        CreditLevel creditLevel = creditLevelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested creditLevel id does not exist!"));
        return mapper.map(creditLevel, CreditLevelResponseDto.class);
    }

    /**
     * <summary>
     * Find all creditLevels
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */
    public Page<CreditLevel> findAll(Long limit, Boolean isActive, Long repaymentPeriod, PageRequest pageRequest) {
        GenericSpecification<CreditLevel> genericSpecification = new GenericSpecification<CreditLevel>();

        if (limit != null) {
            genericSpecification.add(new SearchCriteria("limit", limit, SearchOperation.EQUAL));
        }
        if (repaymentPeriod != null) {
            genericSpecification.add(new SearchCriteria("repaymentPeriod", repaymentPeriod, SearchOperation.EQUAL));
        }
        if (isActive != null) {
            genericSpecification.add(new SearchCriteria("isActive", isActive, SearchOperation.EQUAL));
        }
        Page<CreditLevel> creditLevel = creditLevelRepository.findAll(genericSpecification, pageRequest);

        return creditLevel;

    }

    /**
     * <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a creditLevel</remarks>
     */
    public void enableDisEnableState(EnableDisEnableDto request) {
        CreditLevel creditLevel = creditLevelRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested creditLevel id does not exist!"));
        creditLevel.setIsActive(request.getIsActive());
        creditLevel.setUpdatedBy(0L);
        creditLevelRepository.save(creditLevel);

    }

    public List<CreditLevelDto> getAllByStatus(Boolean isActive) {
        List<CreditLevel> creditLevels = creditLevelRepository.findByIsActive(isActive);
        return creditLevels
                .stream()
                .map(user -> mapper.map(user, CreditLevelDto.class))
                .collect(Collectors.toList());
    }
}
