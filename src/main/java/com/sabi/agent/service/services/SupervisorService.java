package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.requestDto.SupervisorDto;
import com.sabi.agent.core.dto.responseDto.SupervisorResponseDto;
import com.sabi.agent.core.models.Supervisor;
import com.sabi.agent.core.models.agentModel.Agent;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.SupervisorRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentRepository;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.models.User;
import com.sabi.framework.repositories.UserRepository;
import com.sabi.framework.service.TokenService;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("ALL")
@Slf4j
@Service
public class SupervisorService {
    private SupervisorRepository supervisorRepository;
    private UserRepository userRepository;
    private final AgentRepository agentRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;

    public SupervisorService(SupervisorRepository supervisorRepository, UserRepository userRepository, AgentRepository agentRepository, ModelMapper mapper, ObjectMapper objectMapper, Validations validations) {
        this.supervisorRepository = supervisorRepository;
        this.userRepository = userRepository;
        this.agentRepository = agentRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
    }

    /** <summary>
     * Supervisor creation
     * </summary>
     * <remarks>this method is responsible for creation of new Supervisor</remarks>
     */

    public SupervisorResponseDto createSupervisor(SupervisorDto request) {
        validations.validateSupervisor(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        log.info("User fetched " + userCurrent);
        Supervisor supervisor = mapper.map(request,Supervisor.class);
        Supervisor userExist = supervisorRepository.findByUserId(request.getUserId());
        if(userExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " This Supervisor already exist");
        }
        supervisor.setCreatedBy(userCurrent.getId());
        supervisor.setIsActive(true);
        supervisor = supervisorRepository.save(supervisor);
        log.debug("Create new supervisor - {}",supervisor);
        return mapper.map(supervisor, SupervisorResponseDto.class);
    }

    /** <summary>
     * Supervisor update
     * </summary>
     * <remarks>this method is responsible for updating already existing Supervisor</remarks>
     */

    public SupervisorResponseDto updateSupervisor(SupervisorDto request) {
        validations.validateSupervisor(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        log.info("User fetched " + userCurrent);
        Supervisor supervisor = supervisorRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested supervisor Id does not exist!"));
        mapper.map(request, supervisor);
        supervisor.setUpdatedBy(userCurrent.getId());
        supervisorRepository.save(supervisor);
        log.debug("Supervisor record updated - {}" + new Gson().toJson(supervisor));
        return mapper.map(supervisor, SupervisorResponseDto.class);
    }

    /** <summary>
     * Find Supervisor
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */

    public SupervisorResponseDto findSupervisor(Long id){
        Supervisor supervisor = supervisorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Supervisor Id does not exist!"));
        User user = userRepository.findById(supervisor.getUserId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Supervisor UserId does not exist!"));
        SupervisorResponseDto response = SupervisorResponseDto.builder()
                .id(supervisor.getId())
                .userId(supervisor.getUserId())
                .user((user.getFirstName())+ " " + (user.getLastName()))
                .createdDate(supervisor.getCreatedDate())
                .createdBy(supervisor.getCreatedBy())
                .updatedBy(supervisor.getUpdatedBy())
                .updatedDate(supervisor.getUpdatedDate())
                .isActive(supervisor.getIsActive())
                .build();
        return response;
    }
    /** <summary>
     * Find all Supervisor
     * </summary>
     * <remarks>this method is responsible for searching all records and getting a pagination</remarks>
     */
    public Page<Supervisor> findAll(String supervisorName, LocalDate createdDate, LocalDateTime lowerDateTime,
                                    LocalDateTime upperDateTime, Boolean isActive, Pageable pageable ) {
//        LocalDateTime lowerDateTime = null, upperDateTime = null;
//        if (createdDate!=null){
//            lowerDateTime = LocalDateTime.of(createdDate.getYear(),
//                    createdDate.getMonthValue(),createdDate.getDayOfMonth(),00,00,00);

//            upperDateTime = lowerDateTime.plusDays(1l);
//            log.info("my lowerDate == {}",lowerDateTime);
//            log.info("my upperDate == {}",upperDateTime);
//        }
        Page<Supervisor> supervisors = supervisorRepository.searchSupervisors(supervisorName,isActive,lowerDateTime,upperDateTime,pageable);
        if (supervisors == null) {
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        supervisors.getContent().stream().forEach(this::setSupervisorDetails);
        return supervisors;

    }

    /** <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a Supervisor</remarks>
     */
    public void enableDisableSupervisor (EnableDisEnableDto request){
        validations.validateStatus(request.getIsActive());
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        log.info("User fetched " + userCurrent);
        Supervisor supervisor = supervisorRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Supervisor Id does not exist!"));
        supervisor.setIsActive(request.getIsActive());
        supervisor.setUpdatedBy(userCurrent.getId());
        supervisorRepository.save(supervisor);

    }
    public List<Supervisor> getAll(Boolean isActive){
        List<Supervisor> supervisorList = supervisorRepository.findByIsActive(isActive);
        supervisorList.stream().forEach(this::setSupervisorDetails);
        return supervisorList;

    }

    private void setSupervisorDetails(Supervisor supervisor) {
        User user = userRepository.findById(supervisor.getUserId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Supervisor UserId does not exist!"));
        supervisor.setUser(user.getFirstName() + " " + user.getLastName());
    }
}
