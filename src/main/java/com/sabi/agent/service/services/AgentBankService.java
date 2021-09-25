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
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;


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


    public AgentBankService(AgentRepository agentRepository, BankRepository bankRepository, ModelMapper mapper, ObjectMapper objectMapper, Validations validations) {
        this.agentRepository = agentRepository;
        this.bankRepository = bankRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
    }

    /** <summary>
     * AgentBank creation
     * </summary>
     * <remarks>this method is responsible for creation of new Agent Bank</remarks>
     */

    public AgentBankResponseDto createAgentBank(AgentBankDto request) {
        validations.validateAgentBank(request);
        AgentBank agentBank = mapper.map(request,AgentBank.class);
        exists.agentBankExist(request);
        agentBank.setCreatedBy(0l);
        agentBank.setActive(false);
        agentBank.setDefault(false);
        agentBank = agentBankRepository.save(agentBank);
        log.debug("Create new Agent Bank - {}"+ new Gson().toJson(agentBank));
        return mapper.map(agentBank, AgentBankResponseDto.class);
    }



    /** <summary>
     * Agent Bank update
     * </summary>
     * <remarks>this method is responsible for updating already existing Agent Bank</remarks>
     */

    public AgentBankResponseDto updateAgentBank(AgentBankDto request) {
        validations.validateAgentBank(request);
        AgentBank agentBank = agentBankRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Agent Bank does not exist!"));
        mapper.map(request, agentBank);
        agentBank.setUpdatedBy(0l);
        exists.AgentBankUpateExist(request);
        agentBankRepository.save(agentBank);
        log.debug("Agent Bank record updated - {}" + new Gson().toJson(agentBank));
        return mapper.map(agentBank, AgentBankResponseDto.class);
    }


    /** <summary>
     * Find Agent Bank
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */
    public AgentBankResponseDto findAgentBank(Long id){
        AgentBank agentBank = agentBankRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Agent Bank Id does not exist!"));

        Agent agent =  agentRepository.findById(agentBank.getAgentId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Agent!"));

        Bank bank = bankRepository.findById(agentBank.getBankId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Bank ID!"));
        AgentBankResponseDto response = AgentBankResponseDto.builder()
                .id(agentBank.getId())
                .agentId(agentBank.getAgentId())
                .bankId(agentBank.getBankId())
                .bankName(agentBank.getBankName())
                .isDefault(agentBank.isDefault())
                .accountNumber(agentBank.getAccountNumber())
                .createdDate(agentBank.getCreatedDate())
                .createdBy(agentBank.getCreatedBy())
                .updatedBy(agentBank.getUpdatedBy())
                .updatedDate(agentBank.getUpdatedDate())
                .isActive(agentBank.isActive())
                .build();

        return response;
    }



    /** <summary>
     * Find all Agent Bank
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */




    public Page<AgentBank> findAll(Long agentId, Long bankId, String bankName, Integer accountNumber, PageRequest pageRequest ) {

        GenericSpecification<AgentBank> genericSpecification = new GenericSpecification<AgentBank>();

        if (agentId != null)
        {
            genericSpecification.add(new SearchCriteria("agentId", agentId, SearchOperation.EQUAL));
        }

        if (bankId != null )
        {
            genericSpecification.add(new SearchCriteria("bankId", bankId, SearchOperation.EQUAL));
        }
        if (bankName != null )
        {
            genericSpecification.add(new SearchCriteria("bankName", bankName, SearchOperation.EQUAL));
        }


        if (accountNumber != null)
        {
            genericSpecification.add(new SearchCriteria("accountNumber", accountNumber, SearchOperation.EQUAL));
        }

        Page<AgentBank> agentBanks = agentBankRepository.findAll(genericSpecification, pageRequest);

        if (agentBanks == null) {
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }

        return agentBanks;

    }


    /** <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a Agent Bank</remarks>
     */
    public void enableDisableAgentBank (EnableDisEnableDto request){
        AgentBank agentBank = agentBankRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Agent Bank does not exist!"));
        agentBank.setActive(request.isActive());
        agentBank.setUpdatedBy(0l);
        agentBankRepository.save(agentBank);

    }

    public List<AgentBank> getAll(Boolean isActive){
        List<AgentBank> agentBanks = agentBankRepository.findByIsActive(isActive);
        return agentBanks;

    }


}
