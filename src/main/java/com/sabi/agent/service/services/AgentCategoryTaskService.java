package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentCategoryTaskDto;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.responseDto.AgentCategoryTaskResponseDto;
import com.sabi.agent.core.models.Task;
import com.sabi.agent.core.models.agentModel.AgentCategory;
import com.sabi.agent.core.models.agentModel.AgentCategoryTask;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.TaskRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentCategoryRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentCategoryTaskRepository;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;


@SuppressWarnings("ALL")
@Slf4j
@Service
public class AgentCategoryTaskService {

    private AgentCategoryTaskRepository agentCategoryTaskRepository;
    private AgentCategoryRepository agentCategoryRepository;
    private TaskRepository taskRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;


    public AgentCategoryTaskService(AgentCategoryTaskRepository agentCategoryTaskRepository, AgentCategoryRepository agentCategoryRepository, TaskRepository taskRepository, ModelMapper mapper, ObjectMapper objectMapper, Validations validations) {
        this.agentCategoryTaskRepository = agentCategoryTaskRepository;
        this.agentCategoryRepository = agentCategoryRepository;
        this.taskRepository = taskRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
    }

    /** <summary>
     * AgentCategoryTask creation
     * </summary>
     * <remarks>this method is responsible for creation of new Agent Category Task</remarks>
     */

    public AgentCategoryTaskResponseDto createAgentCategoryTask(AgentCategoryTaskDto request) {
        validations.validateAgentCategoryTask(request);
        AgentCategoryTask agentCategoryTask = mapper.map(request,AgentCategoryTask.class);
        AgentCategoryTask categoryTaskExist = agentCategoryTaskRepository.findByName(request.getName());
        if(categoryTaskExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent Category Task already exist");
        }
        agentCategoryTask.setCreatedBy(0l);
        agentCategoryTask.setIsActive(true);
        agentCategoryTask = agentCategoryTaskRepository.save(agentCategoryTask);
        log.debug("Create new Agent Category Task - {}"+ new Gson().toJson(agentCategoryTask));
        return mapper.map(agentCategoryTask, AgentCategoryTaskResponseDto.class);
    }



    /** <summary>
     * Agent Category Task update
     * </summary>
     * <remarks>this method is responsible for updating already existing Agent Category Task</remarks>
     */

    public AgentCategoryTaskResponseDto updateAgentCategoryTask(AgentCategoryTaskDto request) {
        validations.validateAgentCategoryTask(request);
        AgentCategoryTask agentCategoryTask = agentCategoryTaskRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Agent Category Task does not exist!"));
        mapper.map(request, agentCategoryTask);
        agentCategoryTask.setUpdatedBy(0l);
        agentCategoryTaskRepository.save(agentCategoryTask);
        log.debug("Agent Category Task record updated - {}" + new Gson().toJson(agentCategoryTask));
        return mapper.map(agentCategoryTask, AgentCategoryTaskResponseDto.class);
    }





    /** <summary>
     * Find Agent Category Task
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */
    public AgentCategoryTaskResponseDto findAgentCategoryTask(Long id){
        AgentCategoryTask agentCategoryTask = agentCategoryTaskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Agent Category Task Id does not exist!"));

        AgentCategory agentCategory =  agentCategoryRepository.findById(agentCategoryTask.getAgentCategory().getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Agent Category!"));

        Task task = taskRepository.findById(agentCategoryTask.getTask().getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Task Type!"));
        AgentCategoryTaskResponseDto response = AgentCategoryTaskResponseDto.builder()
                .id(agentCategoryTask.getId())
                .name(agentCategoryTask.getName())
                .agentCategoryId(agentCategoryTask.getAgentCategory().getId())
                .taskId(agentCategoryTask.getTask().getId())
                .createdDate(agentCategoryTask.getCreatedDate())
                .createdBy(agentCategoryTask.getCreatedBy())
                .updatedBy(agentCategoryTask.getUpdatedBy())
                .updatedDate(agentCategoryTask.getUpdatedDate())
                .isActive(agentCategoryTask.getIsActive())
                .build();

        return response;
    }



    /** <summary>
     * Find all Agent Category Task
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */




    public Page<AgentCategoryTask> findAll(String name, Boolean isActive, PageRequest pageRequest ) {
        Page<AgentCategoryTask> agentCategoryTasks = agentCategoryTaskRepository.findAgentCategoryTasks(name, isActive, pageRequest);
        if (agentCategoryTasks == null) {
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        return agentCategoryTasks;

    }


    /** <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a Agent Category Task</remarks>
     */
    public void enableDisableAgtCatTask (EnableDisEnableDto request){
        AgentCategoryTask agentCategoryTask = agentCategoryTaskRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Agent Category Task does not exist!"));
        agentCategoryTask.setIsActive(request.getIsActive());
        agentCategoryTask.setUpdatedBy(0l);
        agentCategoryTaskRepository.save(agentCategoryTask);

    }
}