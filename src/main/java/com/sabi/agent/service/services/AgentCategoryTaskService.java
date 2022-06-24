package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentCategoryTaskDto;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.responseDto.AgentCategoryTaskResponseDto;
import com.sabi.agent.core.models.TargetType;
import com.sabi.agent.core.models.Task;
import com.sabi.agent.core.models.agentModel.Agent;
import com.sabi.agent.core.models.agentModel.AgentCategory;
import com.sabi.agent.core.models.agentModel.AgentCategoryTask;
import com.sabi.agent.core.models.agentModel.AgentTarget;
import com.sabi.agent.service.helper.*;
import com.sabi.agent.service.repositories.TaskRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentCategoryRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentCategoryTaskRepository;
import com.sabi.framework.exceptions.BadRequestException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.models.User;
import com.sabi.framework.service.TokenService;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
public class AgentCategoryTaskService {

    private AgentCategoryTaskRepository agentCategoryTaskRepository;
    private AgentCategoryRepository agentCategoryRepository;
    private TaskRepository taskRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;
    @Autowired
    private Exists exists;


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
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        AgentCategoryTask agentCategoryTask = mapper.map(request,AgentCategoryTask.class);
        exists.agentCategoryTaskExist(request);

        agentCategoryTask.setCreatedBy(userCurrent.getId());
        agentCategoryTask.setIsActive(false);
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
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        AgentCategoryTask agentCategoryTask = agentCategoryTaskRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Agent Category Task does not exist!"));
        mapper.map(request, agentCategoryTask);
        agentCategoryTask.setName(request.getName());
        agentCategoryTask.setUpdatedBy(userCurrent.getId());
//        exists.agentCategoryTaskExist(request);
//        exists.agentCategoryTaskUpateExist(request);
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

        AgentCategory agentCategory =  agentCategoryRepository.findById(agentCategoryTask.getAgentCategoryId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Agent Category!"));

        Task task = taskRepository.findById(agentCategoryTask.getTaskId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Task Type!"));
        AgentCategoryTaskResponseDto response = AgentCategoryTaskResponseDto.builder()
                .id(agentCategoryTask.getId())
                .name(agentCategoryTask.getName())
                .agentCategoryId(agentCategoryTask.getAgentCategoryId())
                .taskId(agentCategoryTask.getTaskId())
                .createdDate(agentCategoryTask.getCreatedDate())
                .createdBy(agentCategoryTask.getCreatedBy())
                .updatedBy(agentCategoryTask.getUpdatedBy())
                .updatedDate(agentCategoryTask.getUpdatedDate())
                .isActive(agentCategoryTask.getIsActive())
                .agentCategoryName(agentCategory.getName())
                .taskName(task.getName())
                .build();

        return response;
    }



    /** <summary>
     * Find all Agent Category Task
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */




    public Page<AgentCategoryTask> findAll(String name, Long agentCategoryId,
                                           Long taskId, Boolean isActive, LocalDate fromDate, LocalDate toDate, PageRequest pageRequest ) {
        LocalDateTime from  = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime to = fromDate != null ? toDate.plusDays(1).atStartOfDay() : null;

        GenericSpecification<AgentCategoryTask> genericSpecification = new GenericSpecification<AgentCategoryTask>();

        if (name != null && !name.isEmpty())
        {
            genericSpecification.add(new SearchCriteria("name", name, SearchOperation.MATCH));
        }

        if(agentCategoryId != null){
            genericSpecification.add(new SearchCriteria("agentCategoryId", agentCategoryId, SearchOperation.EQUAL));
        }

        if(taskId != null){
            genericSpecification.add(new SearchCriteria("taskId", taskId, SearchOperation.EQUAL));
        }


        if (from!=null){
            if (to!=null && from.isAfter(to))
                throw new BadRequestException(CustomResponseCode.BAD_REQUEST,"fromDate can't be greater than toDate");
            genericSpecification.add(new SearchCriteria("createdDate", from,SearchOperation.GREATER_THAN_EQUAL));
        }
        if (to!=null){
            if (from == null)
                throw new BadRequestException(CustomResponseCode.BAD_REQUEST,"'fromDate' must be included along with 'toDate' in the request");
            genericSpecification.add(new SearchCriteria("createdDate", to,SearchOperation.LESS_THAN_EQUAL));
        }
//        if(agentCategoryName != null && !agentCategoryName.isEmpty()){
//            genericSpecification.add(new SearchCriteria("agentCategoryName", agentCategoryName, SearchOperation.MATCH));
//        }

        if (isActive != null )
        {
            genericSpecification.add(new SearchCriteria("isActive", isActive, SearchOperation.EQUAL));
        }



        Page<AgentCategoryTask> agentCategoryTasks = agentCategoryTaskRepository.findAll(genericSpecification, pageRequest);
        if (agentCategoryTasks == null) {
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        agentCategoryTasks.stream().forEach(agentCategoryTask -> getAndSetAgentCategoryTaskParameters(agentCategoryTask));
        return agentCategoryTasks;

    }


    /** <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a Agent Category Task</remarks>
     */
    public void enableDisableAgtCatTask (EnableDisEnableDto request){
        validations.validateStatus(request.getIsActive());
//        validations.validateAgentCategoryTaskEnable(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        AgentCategoryTask agentCategoryTask = agentCategoryTaskRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Agent Category Task does not exist!"));
        agentCategoryTask.setIsActive(request.getIsActive());
        agentCategoryTask.setUpdatedBy(userCurrent.getId());
        agentCategoryTaskRepository.save(agentCategoryTask);

    }


    public List<AgentCategoryTask> getAll(Boolean isActive){
        List<AgentCategoryTask> agentCategoryTaskList = agentCategoryTaskRepository.findByIsActive(isActive);
        agentCategoryTaskList.stream().forEach(agentCategoryTask -> getAndSetAgentCategoryTaskParameters(agentCategoryTask));
        return agentCategoryTaskList;

    }

    public AgentCategoryTask getAndSetAgentCategoryTaskParameters(AgentCategoryTask agentCategoryTask) {
        AgentCategory agentCategory = agentCategoryRepository.findById(agentCategoryTask.getAgentCategoryId()).orElse(null);
        Task task =  taskRepository.findById(agentCategoryTask.getTaskId()).orElse(null);
        agentCategoryTask.setAgentCategoryName(agentCategory != null? agentCategory.getName():null);
        agentCategoryTask.setTaskName(task != null? task.getName():null);
        return agentCategoryTask;
    }

    public List<AgentCategoryTask> getByAgentCategoryId(Long agentCategoryId) {
        List<AgentCategoryTask> agentCategoryTaskList = agentCategoryTaskRepository.findByAgentCategoryId(agentCategoryId);
        agentCategoryTaskList.stream().forEach(agentCategoryTask -> getAndSetAgentCategoryTaskParameters(agentCategoryTask));
        return agentCategoryTaskList;
    }
}
