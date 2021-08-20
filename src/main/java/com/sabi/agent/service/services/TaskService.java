package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.requestDto.TaskDto;
import com.sabi.agent.core.dto.responseDto.TaskResponseDto;
import com.sabi.agent.core.helpers.Validations;
import com.sabi.agent.core.models.Task;
import com.sabi.agent.service.repositories.TaskRepository;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Date;


@Slf4j
@Service
public class TaskService {


    private TaskRepository taskRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;

    public TaskService(TaskRepository taskRepository, ModelMapper mapper, ObjectMapper objectMapper,Validations validations) {
        this.taskRepository = taskRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
    }


    /** <summary>
     * State creation
     * </summary>
     * <remarks>this method is responsible for creation of new task</remarks>
     */

    public TaskResponseDto createTask(TaskDto request) {
        validations.validateTask(request);
        Task task = mapper.map(request,Task.class);
        Task taskExist = taskRepository.findByNameAndTaskType(request.getName(),request.getTaskType());
        if(taskExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Task already exist");
        }
        task.setCreatedBy(0l);
        task.setIsactive(true);
        task = taskRepository.save(task);
        log.debug("Create new Task - {}", new Gson().toJson(task));
        return mapper.map(task, TaskResponseDto.class);
    }


    /** <summary>
     * Task update
     * </summary>
     * <remarks>this method is responsible for updating already existing task</remarks>
     */

    public TaskResponseDto updateTask(TaskDto request) {
        validations.validateTask(request);
        Task task = taskRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Task Id does not exist!"));
        mapper.map(request, task);
        task.setUpdatedBy(0l);
        task.setUpdatedDate(new Date());
        taskRepository.save(task);
        log.debug("task record updated - {}", new Gson().toJson(task));
        return mapper.map(task, TaskResponseDto.class);
    }
}
