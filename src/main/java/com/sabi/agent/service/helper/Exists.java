package com.sabi.agent.service.helper;

import com.sabi.agent.core.dto.agentDto.requestDto.AgentBankDto;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentCategoryTargetDto;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentCategoryTaskDto;
import com.sabi.agent.core.dto.requestDto.CreditLevelDto;
import com.sabi.agent.core.dto.requestDto.UserTaskDto;
import com.sabi.agent.core.models.CreditLevel;
import com.sabi.agent.core.models.UserTask;
import com.sabi.agent.core.models.agentModel.AgentBank;
import com.sabi.agent.core.models.agentModel.AgentCategoryTarget;
import com.sabi.agent.core.models.agentModel.AgentCategoryTask;
import com.sabi.agent.service.repositories.*;
import com.sabi.agent.service.repositories.agentRepo.AgentBankRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentCategoryTargetRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentCategoryTaskRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentRepository;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.repositories.UserRepository;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
    @Autowired
    private AgentBankRepository agentBankRepository;
    @Autowired
    private CreditLevelRepository creditLevelRepository;


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
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent Category Target already exist");
        }
        AgentCategoryTarget agentCategory_TargetExist = agentCategoryTargetRepository.findByAgentCategoryIdAndTargetTypeId(request.getAgentCategoryId(), request.getTargetTypeId());
        if(agentCategory_TargetExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent Category Target already exist");
        }

       }

    public void agentBankExist(AgentBankDto request) {
        AgentBank bankExist = agentBankRepository.findByAccountNumber(request.getAccountNumber());
        if(bankExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent Bank already exist");
        }
        AgentBank agent_BankExist = agentBankRepository.findByAgentIdAndBankId(request.getAgentId(), request.getBankId());
        if(agent_BankExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent Bank already exist");
        }

    }

    public void creditLevelExist(CreditLevelDto request) {
        CreditLevel creditLevel = creditLevelRepository.findByLimitsAndRepaymentPeriod(request.getLimits(), request.getRepaymentPeriod());
        if(creditLevel !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Credit level already exist");
        }

    }

    public void agentCategoryTargetUpateExist(AgentCategoryTargetDto request) {

        agentCategoryTargetNameExist(request.getName(), request.getId());
        agentCategoryTargetKeyExist(request.getAgentCategoryId(),request.getTargetTypeId(),request.getId());

    }

    public void agentCategoryTargetNameExist(String name, Long id) {
        GenericSpecification<AgentCategoryTarget> genericSpecification = new GenericSpecification<AgentCategoryTarget>();
        if (id > 0) {
            genericSpecification.add(new SearchCriteria("id", id, SearchOperation.NOT_EQUAL));
        }

        genericSpecification.add(new SearchCriteria("name", name, SearchOperation.EQUAL));


        List<AgentCategoryTarget> agentCategoryTargetList = agentCategoryTargetRepository.findAll(genericSpecification);
        if(agentCategoryTargetList !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent Category Target already exist");
        }

    }

    public void agentCategoryTargetKeyExist(Long categoryId, Long targetTypeId, Long id) {
        GenericSpecification<AgentCategoryTarget> genericSpecification = new GenericSpecification<AgentCategoryTarget>();
        if (id > 0) {
            genericSpecification.add(new SearchCriteria("id", id, SearchOperation.NOT_EQUAL));
        }

        genericSpecification.add(new SearchCriteria("agentCategoryId", categoryId, SearchOperation.EQUAL));
        genericSpecification.add(new SearchCriteria("targetTypeId", targetTypeId, SearchOperation.EQUAL));

        List<AgentCategoryTarget> agentCategoryTargetList = agentCategoryTargetRepository.findAll(genericSpecification);
        if(agentCategoryTargetList !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent Category Target already exist");
        }

    }

    public void userTaskExist(UserTaskDto request){
//        UserTask userExist = userTaskRepository.findByUserId(request.getUserId());
//        if(userExist !=null){
//            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " User already exist");
//        }

        UserTask taskExist = userTaskRepository.findByTaskId(request.getTaskId());
        if(taskExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Task already exist");
        }
    }

    public void userTaskUpateExist(UserTaskDto request){
        userTaskIdExist(request.getTaskId(), request.getId());

    }

    public void userTaskIdExist(Long taskId, Long id) {
        GenericSpecification<UserTask> genericSpecification = new GenericSpecification<UserTask>();
        if (id > 0) {
            genericSpecification.add(new SearchCriteria("id", id, SearchOperation.NOT_EQUAL));
        }

        genericSpecification.add(new SearchCriteria("taskId", taskId, SearchOperation.EQUAL));


        List<UserTask> userTaskExist = userTaskRepository.findAll(genericSpecification);
        if(userTaskExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " User Task already exist");
        }

    }

    public void AgentBankUpateExist(AgentBankDto request){
        agentAccountNumberExist(request.getAgentId(), request.getAccountNumber(), request.getId());

    }

    public void agentAccountNumberExist(Long agentId, String accountNumber, Long id) {
        GenericSpecification<AgentBank> genericSpecification = new GenericSpecification<AgentBank>();
        if (id > 0) {
            genericSpecification.add(new SearchCriteria("id", id, SearchOperation.NOT_EQUAL));
        }

        genericSpecification.add(new SearchCriteria("agentId", agentId, SearchOperation.EQUAL));
        genericSpecification.add(new SearchCriteria("accountNumber", accountNumber, SearchOperation.EQUAL));


        List<AgentBank> agentBankExist = agentBankRepository.findAll(genericSpecification);
        if(agentBankExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent Bank already exist");
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

    public void agentCategoryTaskUpateExist(AgentCategoryTaskDto request) {

        agentCategoryNameExist(request.getName(), request.getId());
        agentCategoryKeyExist(request.getAgentCategoryId(),request.getTaskId(),request.getId());

    }

    public void agentCategoryNameExist(String name, Long id) {
        GenericSpecification<AgentCategoryTask> genericSpecification = new GenericSpecification<AgentCategoryTask>();
        if (id > 0) {
            genericSpecification.add(new SearchCriteria("id", id, SearchOperation.NOT_EQUAL));
        }

        genericSpecification.add(new SearchCriteria("name", name, SearchOperation.EQUAL));


        List<AgentCategoryTask> categoryTaskExist = agentCategoryTaskRepository.findAll(genericSpecification);
        if(categoryTaskExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent Category Task already exist");
        }

    }

    public void agentCategoryKeyExist(Long categoryId, Long taskId, Long id) {
        GenericSpecification<AgentCategoryTask> genericSpecification = new GenericSpecification<AgentCategoryTask>();
        if (id > 0) {
            genericSpecification.add(new SearchCriteria("id", id, SearchOperation.NOT_EQUAL));
        }

        genericSpecification.add(new SearchCriteria("agentCategoryId", categoryId, SearchOperation.EQUAL));
        genericSpecification.add(new SearchCriteria("taskId", taskId, SearchOperation.EQUAL));

        List<AgentCategoryTask> categoryTaskExist = agentCategoryTaskRepository.findAll(genericSpecification);
        if(categoryTaskExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent Category Task already exist");
        }

    }
}
