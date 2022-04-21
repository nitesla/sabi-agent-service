package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentSupervisorDto;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.responseDto.AgentSupervisorResponseDto;
import com.sabi.agent.core.models.Supervisor;
import com.sabi.agent.core.models.agentModel.Agent;
import com.sabi.agent.core.models.agentModel.AgentSupervisor;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.SupervisorRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentSupervisorRepository;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.models.User;
import com.sabi.framework.repositories.UserRepository;
import com.sabi.framework.service.TokenService;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//ken
@SuppressWarnings("ALL")
@Slf4j
@Service
public class AgentSupervisorService {

    private AgentSupervisorRepository agentSupervisorRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;
    private AgentRepository agentRepository;
    private SupervisorRepository supervisorRepository;
    private UserRepository userRepository;

    public AgentSupervisorService(AgentSupervisorRepository agentSupervisorRepository,
                                  ModelMapper mapper, ObjectMapper objectMapper, Validations validations,
                                  AgentRepository agentRepository, SupervisorRepository supervisorRepository,
                                  UserRepository userRepository) {
        this.agentSupervisorRepository = agentSupervisorRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
        this.agentRepository = agentRepository;
        this.supervisorRepository = supervisorRepository;
        this.userRepository = userRepository;
    }

    public AgentSupervisorResponseDto createAgentSupervisor(AgentSupervisorDto request) {
        validations.validateAgentSupervisor(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        log.info("User fetched " + userCurrent);
        AgentSupervisor agentSupervisor = mapper.map(request, AgentSupervisor.class);
//        GenericSpecification<AgentSupervisor> genericSpecification = new GenericSpecification<AgentSupervisor>();
//        genericSpecification.add(new SearchCriteria("agentId", request.getAgentId(), SearchOperation.EQUAL));
//        genericSpecification.add(new SearchCriteria("supervisorId", request.getSupervisorId(), SearchOperation.EQUAL));
//        Optional agentSupervisorExist = agentSupervisorRepository.findOne(genericSpecification);
        boolean exists = agentSupervisorRepository.exists(Example.of(agentSupervisor));
        if (exists) {
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " agentSupervisor already exist");
        }
        agentSupervisor.setCreatedBy(userCurrent.getId());
        agentSupervisor.setIsActive(true); // so that one can easily fetch/query by 'isActive' status
        agentSupervisor = agentSupervisorRepository.save(agentSupervisor);
        log.debug("Create new agentSupervisor - {}" + agentSupervisor);
        return mapper.map(agentSupervisor, AgentSupervisorResponseDto.class);
    }


    /**
     * <summary>
     * agentSupervisor update
     * </summary>
     * <remarks>this method is responsible for updating already existing agentSupervisor</remarks>
     */

    public AgentSupervisorResponseDto updateAgentSupervisor(AgentSupervisorDto request) {
        validations.validateAgentSupervisor(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        log.info("User fetched " + userCurrent);
        AgentSupervisor agentSupervisor = agentSupervisorRepository.findById(request.getId())
                .orElseThrow(()-> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agentSupervisor id does not exist!"));
        mapper.map(request, agentSupervisor);
        boolean supervisor = agentSupervisorRepository.exists(Example.of(agentSupervisor));
        if(supervisor)
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " agentSupervisor already exist");
        agentSupervisor.setUpdatedBy(userCurrent.getId());
        agentSupervisorRepository.save(agentSupervisor);
        log.debug("agentSupervisor record updated - {}" + new Gson().toJson(agentSupervisor));
        return mapper.map(agentSupervisor, AgentSupervisorResponseDto.class);
    }


    /**
     * <summary>
     * Find agentSupervisor
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */
    public AgentSupervisorResponseDto findAgentSupervisor(Long id) {
        AgentSupervisor agentSupervisor = agentSupervisorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agentSupervisor id does not exist!"));
        return  composeAgentSupervisorResponse(agentSupervisor);
    }


    /**
     * <summary>
     * Find all agentSupervisors
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     * @return
     */
    /**
     * @Description: new Feature: Search by supervisorname, agentName and createdDate.
     * Fixed a bug: Also changed the return tyoe to be a Page instead of just a list.
     * @Date: 20/04/2022
     * @Author: Afam Okonkwo
     * @param supervisorName
     * @param agentName
     * @param isActive
     * @param createdDate
     * @param pageable
     * @return
     */
    /**
    public List<AgentSupervisorResponseDto> findAll(PageRequest pageRequest) {
        Page<AgentSupervisor> agentSupervisor = agentSupervisorRepository
                .findAll(pageRequest);
     */
    public  Page<AgentSupervisor> findAll(String supervisorName, String agentName, Boolean isActive, LocalDate createdDate, Pageable pageable) {
        LocalDateTime lowerDateTime = null, upperDateTime = null;
        if (createdDate!=null){
            lowerDateTime = LocalDateTime.of(createdDate.getYear(),createdDate.getMonthValue(),createdDate.getDayOfMonth(),00,00,00);
            upperDateTime = lowerDateTime.plusDays(1l);
            log.info("my lowerDate =={}",lowerDateTime);
            log.info("my upperDate=={}",upperDateTime);
        }
        Page<AgentSupervisor> agentSupervisor = agentSupervisorRepository
                                                .searchAgentSupervisors(supervisorName, agentName, isActive, lowerDateTime, upperDateTime, pageable);
        if (agentSupervisor == null) {
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        agentSupervisor.getContent().stream().forEach(this::composeAgentSupervisor);
        return agentSupervisor;
    }


    /**
     * <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a country</remarks>
     */
    public void enableDisEnableState(EnableDisEnableDto request) {
        validations.validateStatus(request.getIsActive());
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        log.info("User fetched " + userCurrent);
        AgentSupervisor agentSupervisor = agentSupervisorRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agentSupervisor Id does not exist!"));
        agentSupervisor.setIsActive(request.getIsActive());
        agentSupervisor.setUpdatedBy(userCurrent.getId());
        agentSupervisorRepository.save(agentSupervisor);

    }

    private AgentSupervisorResponseDto composeAgentSupervisorResponse(AgentSupervisor agentSupervisor){
        AgentSupervisorResponseDto agentSupervisorResponseDto = mapper.map(agentSupervisor, AgentSupervisorResponseDto.class);
        Optional<Agent> agent= agentRepository.findById(agentSupervisor.getAgentId());
        Optional<Supervisor> supervisor = supervisorRepository.findById(agentSupervisor.getSupervisorId());

        Optional<User> agentAsUser = userRepository.findById(agent.get().getUserId());
        Optional<User> supervisorAsUser = userRepository.findById(supervisor.get().getUserId());
        agentSupervisorResponseDto.setAgentName(agentAsUser.get().getFirstName() + " " + agentAsUser.get().getLastName());
        agentSupervisorResponseDto.setSupervisorName(supervisorAsUser.get().getFirstName() + " " + supervisorAsUser.get().getLastName());
        return agentSupervisorResponseDto;
    }

    public List<AgentSupervisorResponseDto> getAllByStatus(Boolean isActive) {
        List<AgentSupervisor> agentSupervisorsList = agentSupervisorRepository.findByIsActive(isActive);
        List<AgentSupervisorResponseDto> agentSupervisorResponseDtos = new ArrayList<>();
        agentSupervisorsList
                .stream()
                .forEach((agentSupervisor)->{
                    agentSupervisorResponseDtos.add(composeAgentSupervisorResponse(agentSupervisor));
                });

        return agentSupervisorResponseDtos;

    }

    private AgentSupervisor composeAgentSupervisor(AgentSupervisor agentSupervisor){
        Optional<Agent> agent= agentRepository.findById(agentSupervisor.getAgentId());
        Optional<Supervisor> supervisor = supervisorRepository.findById(agentSupervisor.getSupervisorId());

        Optional<User> agentAsUser = userRepository.findById(agent.get().getUserId());
        Optional<User> supervisorAsUser = userRepository.findById(supervisor.get().getUserId());
        agentSupervisor.setAgentName(agentAsUser.get().getFirstName() + " " + agentAsUser.get().getLastName());
        agentSupervisor.setSupervisorName(supervisorAsUser.get().getFirstName() + " " + supervisorAsUser.get().getLastName());
        return agentSupervisor;
    }
}
