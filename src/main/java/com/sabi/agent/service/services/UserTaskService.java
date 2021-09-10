package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.requestDto.UserTaskDto;
import com.sabi.agent.core.dto.responseDto.UserTaskResponseDto;
import com.sabi.agent.core.models.Task;
import com.sabi.agent.core.models.UserTask;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.TaskRepository;
import com.sabi.agent.service.repositories.UserTaskRepository;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.models.User;
import com.sabi.framework.repositories.UserRepository;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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

    public UserTaskService(ModelMapper mapper, ObjectMapper objectMapper, Validations validations) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
    }

    /** <summary>
     * User Task creation
     * </summary>
     * <remarks>this method is responsible for creation of new User Task</remarks>
     */

    public UserTaskResponseDto createUserTask(UserTaskDto request) {
        validations.validateUserTask(request);
        UserTask userTask = mapper.map(request,UserTask.class);
        UserTask userTaskExist = userTaskRepository.findByTaskId(request.getTaskId());
        if(userTaskExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " User Task already exist");
        }
        userTask.setCreatedBy(0l);
        userTask.setIsActive(true);
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
        UserTask userTask = userTaskRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested User Task does not exist!"));
        mapper.map(request, userTask);
        userTask.setUpdatedBy(0l);
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
        Task task =  taskRepository.findById(userTask.getTaskId().getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Task Id does not exist!"));

        User user = userRepository.findById(userTask.getUserId().getId())
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
    public Page<UserTask> findAll(PageRequest pageRequest ) {
        Page<UserTask> userTasks = userTaskRepository.findAll(pageRequest);
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
        UserTask userTask = userTaskRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested User Task Id does not exist!"));
        userTask.setIsActive(request.getIsActive());
        userTask.setUpdatedBy(0l);
        userTaskRepository.save(userTask);

    }
}
