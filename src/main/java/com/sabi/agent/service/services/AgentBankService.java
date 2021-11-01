package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentBankDto;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.responseDto.AgentBankResponseDto;
import com.sabi.agent.core.models.Bank;
import com.sabi.agent.core.models.agentModel.Agent;
import com.sabi.agent.core.models.agentModel.AgentBank;
import com.sabi.agent.service.helper.*;
import com.sabi.agent.service.repositories.BankRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentBankRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentRepository;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.models.User;
import com.sabi.framework.repositories.UserRepository;
import com.sabi.framework.service.TokenService;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@SuppressWarnings("ALL")
@Slf4j
@Service
public class AgentBankService {

    private AgentRepository agentRepository;
    private BankRepository bankRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;
    @Autowired
    private Exists exists;
    @Autowired
    private AgentBankRepository agentBankRepository;
    private final UserRepository userRepository;


    public AgentBankService(AgentRepository agentRepository, BankRepository bankRepository, ModelMapper mapper, ObjectMapper objectMapper, Validations validations, UserRepository userRepository) {
        this.agentRepository = agentRepository;
        this.bankRepository = bankRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
        this.userRepository = userRepository;
    }

    /**
     * <summary>
     * AgentBank creation
     * </summary>
     * <remarks>this method is responsible for creation of new Agent Bank</remarks>
     */

    public AgentBankResponseDto createAgentBank(AgentBankDto request) {
        validations.validateAgentBank(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        log.info("User fetched " + userCurrent);
        AgentBank agentBank = mapper.map(request, AgentBank.class);
        exists.agentBankExist(request);
        agentBank.setCreatedBy(userCurrent.getId());
        agentBank.setIsActive(false);
        agentBank.setDefault(false);
        agentBank = agentBankRepository.save(agentBank);
        log.debug("Create new Agent Bank - {}" + new Gson().toJson(agentBank));
        return getAgentBankResponseDto(agentBank);
    }

    private AgentBankResponseDto getAgentBankResponseDto(AgentBank agentBank) {
        AgentBankResponseDto map = mapper.map(agentBank, AgentBankResponseDto.class);
        Agent agent = agentRepository.findById(agentBank.getAgentId()).orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                "Requested Agent does not exist!"));
        long uId = agent.getUserId();
        log.info("User IDs    " + uId);

        Optional<User> user = userRepository.findById(uId);
        if (user.isPresent()) {
            map.setAgentFirstName(user.get().getFirstName());
            map.setAgentLastName(user.get().getLastName());
        }
        Optional<Bank> bank = bankRepository.findById(agentBank.getBankId());

        if (bank.isPresent())
            map.setBankName(bank.get().getName());
        return map;
    }


    /**
     * <summary>
     * Agent Bank update
     * </summary>
     * <remarks>this method is responsible for updating already existing Agent Bank</remarks>
     */

    public AgentBankResponseDto updateAgentBank(AgentBankDto request) {
        validations.validateAgentBank(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        AgentBank agentBank = agentBankRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Agent Bank does not exist!"));
        mapper.map(request, agentBank);
        agentBank.setUpdatedBy(userCurrent.getId());
        GenericSpecification<AgentBank> genericSpecification = new GenericSpecification<AgentBank>();
        genericSpecification.add(new SearchCriteria("agentId", agentBank.getAgentId(), SearchOperation.EQUAL));
        genericSpecification.add(new SearchCriteria("accountNumber", agentBank.getAccountNumber(), SearchOperation.EQUAL));
        genericSpecification.add(new SearchCriteria("bankId", agentBank.getBankId(), SearchOperation.EQUAL));
        List<AgentBank> agentBanks = agentBankRepository.findAll(genericSpecification);
        if (!agentBanks.isEmpty())
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " AgentBank already exist");
        agentBank.setDefault(agentBank.isDefault());
        agentBankRepository.save(agentBank);
        log.debug("Agent Bank record updated - {}" + new Gson().toJson(agentBank));
        return getAgentBankResponseDto(agentBank);
    }

    @Transactional
    public AgentBankResponseDto setDefalult(long id) {
        AgentBank agentBank = agentBankRepository.findById(id).orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                "Requested Agent Bank does not exist!"));
        agentBankRepository.updateIsDefault();
        agentBank.setDefault(true);
        agentBankRepository.save(agentBank);
        return getAgentBankResponseDto(agentBank);
    }


    /**
     * <summary>
     * Find Agent Bank
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */
    public AgentBankResponseDto findAgentBank(Long id) {
        AgentBank agentBank = agentBankRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Agent Bank Id does not exist!"));

        Agent agent = agentRepository.findById(agentBank.getAgentId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Agent!"));

        Bank bank = bankRepository.findById(agentBank.getBankId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Bank ID!"));
        return getAgentBankResponseDto(agentBank);
    }


    /**
     * <summary>
     * Find all Agent Bank
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     *
     * @return
     */
    public List<AgentBankResponseDto> findAll(Long agentId, Long bankId, String bankName, String accountNumber, PageRequest pageRequest) {

        GenericSpecification<AgentBank> genericSpecification = new GenericSpecification<AgentBank>();

        if (agentId != null) {
            genericSpecification.add(new SearchCriteria("agentId", agentId, SearchOperation.EQUAL));
        }

        if (bankId != null) {
            genericSpecification.add(new SearchCriteria("bankId", bankId, SearchOperation.EQUAL));
        }
        if (bankName != null) {
            genericSpecification.add(new SearchCriteria("bankName", bankName, SearchOperation.EQUAL));
        }


        if (accountNumber != null) {
            genericSpecification.add(new SearchCriteria("accountNumber", accountNumber, SearchOperation.EQUAL));
        }

        log.info("Searching for Data");
        Page<AgentBank> agentBanks = agentBankRepository.findAll(genericSpecification, pageRequest);

        if (agentBanks == null) {
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        return agentBanks.stream().map((agentBank) -> getAgentBankResponseDto(agentBank)).collect(Collectors.toList());
    }


    /**
     * <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a Agent Bank</remarks>
     */
    public void enableDisableAgentBank(EnableDisEnableDto request) {
        validations.validateStatus(request.getIsActive());
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        AgentBank agentBank = agentBankRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Agent Bank does not exist!"));
        agentBank.setIsActive(request.getIsActive());
        agentBank.setUpdatedBy(userCurrent.getId());
        agentBankRepository.save(agentBank);

    }

    public List<AgentBankResponseDto> getAll(Boolean isActive) {
        List<AgentBank> agentBanks = agentBankRepository.findByIsActive(isActive);
        return agentBanks.stream().map((agentBank ->
                getAgentBankResponseDto(agentBank))).collect(Collectors.toList());
    }


}
