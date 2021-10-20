package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.requestDto.BankDto;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.responseDto.BankResponseDto;
import com.sabi.agent.core.models.Bank;
import com.sabi.agent.core.models.agentModel.AgentBank;
import com.sabi.agent.service.helper.GenericSpecification;
import com.sabi.agent.service.helper.SearchCriteria;
import com.sabi.agent.service.helper.SearchOperation;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.BankRepository;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.models.User;
import com.sabi.framework.service.TokenService;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class BankService {

    private BankRepository bankRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;

    public BankService(BankRepository bankRepository, ModelMapper mapper, ObjectMapper objectMapper, Validations validations) {
        this.bankRepository = bankRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
    }



    /** <summary>
     * Bank creation
     * </summary>
     * <remarks>this method is responsible for creation of new bank</remarks>
     */

    public BankResponseDto createBank(BankDto request) {
        validations.validateBank(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        Bank bank = mapper.map(request,Bank.class);
        Bank bankExist = bankRepository.findByName(request.getName());
        if(bankExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Bank already exist");
        }
        bank.setCreatedBy(userCurrent.getId());
        bank.setActive(true);
        bank = bankRepository.save(bank);
        log.debug("Create new bank - {}"+ new Gson().toJson(bank));
        return mapper.map(bank, BankResponseDto.class);
    }



    /** <summary>
     * Bank update
     * </summary>
     * <remarks>this method is responsible for updating already existing bank</remarks>
     */

    public BankResponseDto updateBank(BankDto request) {
        validations.validateBank(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        Bank bank = bankRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested bank id does not exist!"));
        mapper.map(request, bank);
        bank.setUpdatedBy(userCurrent.getId());
        GenericSpecification<Bank> genericSpecification = new GenericSpecification<>();
        genericSpecification.add(new SearchCriteria("name", bank.getName(), SearchOperation.EQUAL));
        genericSpecification.add(new SearchCriteria("bankCode", bank.getBankCode(), SearchOperation.EQUAL));
        List<Bank> banks = bankRepository.findAll(genericSpecification);
        if(!banks.isEmpty())
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, "Bank already exist");

        bankRepository.save(bank);
        log.debug("Bank record updated - {}"+ new Gson().toJson(bank));
        return mapper.map(bank, BankResponseDto.class);
    }



    /** <summary>
     * Find Bank
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */
    public BankResponseDto findBank(Long id){
        Bank bank  = bankRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested bank id does not exist!"));
        return mapper.map(bank,BankResponseDto.class);
    }


    /** <summary>
     * Find all Banks
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */
    public Page<Bank> findAll(String name,String bankCode, PageRequest pageRequest ){
        Page<Bank> bank = bankRepository.findBanks(name,bankCode,pageRequest);
        if(bank == null){
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        return bank;

    }


    /** <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a country</remarks>
     */
    public void enableDisEnableState (EnableDisEnableDto request){
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        Bank bank  = bankRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested bank Id does not exist!"));
        bank.setActive(request.isActive());
        bank.setUpdatedBy(userCurrent.getId());
        bankRepository.save(bank);

    }


    public List<Bank> getAll(Boolean isActive){
        List<Bank> banks = bankRepository.findByIsActive(isActive);
        return banks;

    }
}
