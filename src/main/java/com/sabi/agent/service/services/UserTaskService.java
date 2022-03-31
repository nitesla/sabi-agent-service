package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.requestDto.UserTaskDto;
import com.sabi.agent.core.dto.responseDto.UserTaskResponseDto;
import com.sabi.agent.core.models.Task;
import com.sabi.agent.core.models.UserTask;
import com.sabi.agent.service.helper.*;
import com.sabi.agent.service.repositories.TaskRepository;
import com.sabi.agent.service.repositories.UserTaskRepository;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.models.User;
import com.sabi.framework.repositories.UserRepository;
import com.sabi.framework.service.AuditTrailService;
import com.sabi.framework.service.TokenService;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class UserTaskService {
    @Autowired
    private UserTaskRepository userTaskRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskRepository taskRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;
    private final AuditTrailService auditTrailService;
    @Autowired
    private Exists exists;

    public UserTaskService(ModelMapper mapper, ObjectMapper objectMapper, Validations validations,
                           AuditTrailService auditTrailService) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
        this.auditTrailService = auditTrailService;
    }

    /** <summary>
     * User Task creation
     * </summary>
     * <remarks>this method is responsible for creation of new User Task</remarks>
     */

    public UserTaskResponseDto createUserTask(UserTaskDto request) {
        validations.validateUserTask(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        log.info("User fetched " + userCurrent);
        UserTask userTask = mapper.map(request,UserTask.class);
        exists.userTaskExist(request);
        userTask.setCreatedBy(userCurrent.getId());
        userTask.setIsActive(false);
        userTask = userTaskRepository.save(userTask);
        log.debug("Create new User Task - {}"+ new Gson().toJson(userTask));
        return mapper.map(userTask, UserTaskResponseDto.class);
    }

    /** <summary>
     * User Task update
     * </summary>
     * <remarks>this method is responsible for updating already existing User Task</remarks>
     */
    public UserTaskResponseDto updateUserTask(UserTaskDto request) {
        validations.validateUserTask(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        log.info("User fetched " + userCurrent);
        UserTask userTask = userTaskRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested User Task does not exist!"));
        mapper.map(request, userTask);
        userTask.setUpdatedBy(userCurrent.getId());
//        exists.userTaskUpateExist(request);
        userTaskRepository.save(userTask);
        log.debug("User Task record updated - {}" + new Gson().toJson(userTask));
        return mapper.map(userTask, UserTaskResponseDto.class);
    }

    /** <summary>
     * Find User Task
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */

    public UserTaskResponseDto findUserTask(Long id){
        UserTask userTask = userTaskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested User Task Id does not exist!"));
        Task task =  taskRepository.findById(userTask.getTaskId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Task Id does not exist!"));

        User user = userRepository.findById(userTask.getUserId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " User Id does not exist! "));


        UserTaskResponseDto response = UserTaskResponseDto.builder()
                .id(userTask.getId())
                .userId(user.getId())
                .taskId(task.getId())
                .dateAssigned(userTask.getDateAssigned())
                .endDate(userTask.getEndDate())
                .status(userTask.getStatus())
                .createdDate(userTask.getCreatedDate())
                .createdBy(userTask.getCreatedBy())
                .updatedBy(userTask.getUpdatedBy())
                .updatedDate(userTask.getUpdatedDate())
                .isActive(userTask.getIsActive())
                .build();
        return response;
    }

    /** <summary>
     * Find all User Task
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */
    public Page<UserTask> findAll(Date endDate, Date dateAssigned, String status, Integer agentId, PageRequest pageRequest ) {
        GenericSpecification<UserTask> genericSpecification = new GenericSpecification<UserTask>();

        if (endDate != null && endDate.after(dateAssigned))
        {
            genericSpecification.add(new SearchCriteria("endDate", endDate, SearchOperation.EQUAL));
        }

        if (dateAssigned != null && dateAssigned.before(endDate))
        {
            genericSpecification.add(new SearchCriteria("dateAssigned", dateAssigned, SearchOperation.EQUAL));
        }

        if (status != null && !status.isEmpty())
        {
            genericSpecification.add(new SearchCriteria("status", status, SearchOperation.EQUAL));
        }
        if (agentId != null) {
            genericSpecification.add(new SearchCriteria("agentId", agentId, SearchOperation.EQUAL));
        }


        Page<UserTask> userTasks = userTaskRepository.findAll(genericSpecification, pageRequest);
        if (userTasks == null) {
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        return userTasks;

    }

    /** <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a User Task</remarks>
     */
    public void enableDisableUserTask (EnableDisEnableDto request){
        validations.validateStatus(request.getIsActive());
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        log.info("User fetched " + userCurrent);
        UserTask userTask = userTaskRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested User Task Id does not exist!"));
        userTask.setIsActive(request.getIsActive());
        userTask.setUpdatedBy(userCurrent.getId());
        userTaskRepository.save(userTask);

    }

    public List<UserTask> getAll(Boolean isActive){
        List<UserTask> userTaskList = userTaskRepository.findByIsActive(isActive);
        return userTaskList;

    }
}
