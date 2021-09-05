package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.requestDto.SupervisorDto;
import com.sabi.agent.core.dto.responseDto.SupervisorResponseDto;
import com.sabi.agent.core.models.Supervisor;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.SupervisorRepository;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.models.User;
import com.sabi.framework.repositories.UserRepository;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@SuppressWarnings("ALL")
@Slf4j
@Service
public class SupervisorService {
    private SupervisorRepository supervisorRepository;
    private UserRepository userRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;

    public SupervisorService(SupervisorRepository supervisorRepository, UserRepository userRepository, ModelMapper mapper, ObjectMapper objectMapper, Validations validations) {
        this.supervisorRepository = supervisorRepository;
        this.userRepository = userRepository;
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
        Supervisor supervisor = mapper.map(request,Supervisor.class);

        supervisor.setCreatedBy(0l);
        supervisor.setIsActive(true);
        supervisor = supervisorRepository.save(supervisor);
        log.debug("Create new supervisor - {}"+ new Gson().toJson(supervisor));
        return mapper.map(supervisor, SupervisorResponseDto.class);
    }

    /** <summary>
     * Supervisor update
     * </summary>
     * <remarks>this method is responsible for updating already existing Supervisor</remarks>
     */

    public SupervisorResponseDto updateSupervisor(SupervisorDto request) {
        validations.validateSupervisor(request);
        Supervisor supervisor = supervisorRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested supervisor Id does not exist!"));
        mapper.map(request, supervisor);
        supervisor.setUpdatedBy(0l);
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
                        " Supervisor Id does not exist!"));
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
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */
    public Page<Supervisor> findAll(Pageable pageable ) {
        Page<Supervisor> supervisors = supervisorRepository.findAll(pageable);
        if (supervisors == null) {
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        return supervisors;

    }

    /** <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a Supervisor</remarks>
     */
    public void enableDisableSupervisor (EnableDisEnableDto request){
        Supervisor supervisor = supervisorRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Supervisor Id does not exist!"));
        supervisor.setIsActive(request.getIsActive());
        supervisor.setUpdatedBy(0l);
        supervisorRepository.save(supervisor);

    }
}
