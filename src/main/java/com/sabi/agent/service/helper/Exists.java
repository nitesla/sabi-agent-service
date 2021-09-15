package com.sabi.agent.service.helper;

import com.sabi.agent.core.dto.agentDto.requestDto.AgentCategoryTargetDto;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentCategoryTaskDto;
import com.sabi.agent.core.dto.requestDto.UserTaskDto;
import com.sabi.agent.core.models.UserTask;
import com.sabi.agent.core.models.agentModel.AgentCategoryTarget;
import com.sabi.agent.core.models.agentModel.AgentCategoryTask;
import com.sabi.agent.service.repositories.*;
import com.sabi.agent.service.repositories.agentRepo.AgentCategoryTargetRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentCategoryTaskRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentRepository;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.repositories.UserRepository;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@SuppressWarnings("All")
@Slf4j
@Service
public class Exists {

    private StateRepository stateRepository;
    private AgentCategoryTargetRepository agentCategoryTargetRepository;
    private LGARepository lgaRepository;
    private TargetTypeRepository targetTypeRepository;
    private TaskRepository taskRepository;
    private UserRepository userRepository;
    private WardRepository wardRepository;
    private AgentRepository agentRepository;
    private SupervisorRepository supervisorRepository;
    @Autowired
    private UserTaskRepository userTaskRepository;
    @Autowired
    private AgentCategoryTaskRepository agentCategoryTaskRepository;


    public Exists(StateRepository stateRepository, LGARepository lgaRepository, AgentCategoryTargetRepository agentCategoryTargetRepository, TargetTypeRepository targetTypeRepository, TaskRepository taskRepository, UserRepository userRepository, WardRepository wardRepository, AgentRepository agentRepository, SupervisorRepository supervisorRepository) {
        this.stateRepository = stateRepository;
        this.lgaRepository = lgaRepository;
        this.agentCategoryTargetRepository = agentCategoryTargetRepository;
        this.targetTypeRepository = targetTypeRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.wardRepository = wardRepository;
        this.agentRepository = agentRepository;
        this.supervisorRepository = supervisorRepository;
    }

    public void agentCategoryTargetExist(AgentCategoryTargetDto request) {
        AgentCategoryTarget categoryTargetExist = agentCategoryTargetRepository.findByName(request.getName());
        if(categoryTargetExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " AgentCategoryTarget already exist");
        }
        AgentCategoryTarget agentCategory_TargetExist = agentCategoryTargetRepository.findByAgentCategoryIdAndTargetTypeId(request.getAgentCategoryId(), request.getTargetTypeId());
        if(agentCategory_TargetExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " AgentCategoryTarget already exist");
        }

       }

    public void userTaskExist(UserTaskDto request){
        UserTask userExist = userTaskRepository.findByUserId(request.getUserId());
        if(userExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " User Task already exist");
        }

        UserTask taskExist = userTaskRepository.findByTaskId(request.getTaskId());
        if(taskExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Task already exist");
        }
    }


    public void agentCategoryTaskExist(AgentCategoryTaskDto request) {
        AgentCategoryTask categoryTaskExist = agentCategoryTaskRepository.findByName(request.getName());
        if(categoryTaskExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent Category Task already exist");
        }

        AgentCategoryTask agentCategory_TaskExist = agentCategoryTaskRepository.findByAgentCategoryIdAndTaskId(request.getAgentCategoryId(), request.getTaskId());
        if(agentCategory_TaskExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent Category Task already exist");
        }

    }

}
