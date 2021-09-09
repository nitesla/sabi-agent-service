package com.sabi.agent.service.helper;


import com.sabi.agent.core.dto.agentDto.requestDto.*;
import com.sabi.agent.core.dto.requestDto.*;
import com.sabi.agent.core.models.*;
import com.sabi.agent.core.models.agentModel.AgentCategory;
import com.sabi.agent.service.repositories.*;
import com.sabi.agent.service.repositories.agentRepo.AgentCategoryRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentRepository;
import com.sabi.framework.exceptions.BadRequestException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.models.User;
import com.sabi.framework.repositories.UserRepository;
import com.sabi.framework.utils.CustomResponseCode;
import com.sabi.framework.utils.PasswordUtil;
import com.sabi.framework.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@SuppressWarnings("All")
@Slf4j
@Service
public class Validations {

    private StateRepository stateRepository;
    private AgentCategoryRepository agentCategoryRepository;
    private LGARepository lgaRepository;
    private TargetTypeRepository targetTypeRepository;
    private TaskRepository taskRepository;
    private UserRepository userRepository;
    private WardRepository wardRepository;
    private AgentRepository agentRepository;
    private SupervisorRepository supervisorRepository;


    public Validations(StateRepository stateRepository, LGARepository lgaRepository, AgentCategoryRepository agentCategoryRepository, TargetTypeRepository targetTypeRepository, TaskRepository taskRepository, UserRepository userRepository, WardRepository wardRepository, AgentRepository agentRepository, SupervisorRepository supervisorRepository) {
        this.stateRepository = stateRepository;
        this.lgaRepository = lgaRepository;
        this.agentCategoryRepository = agentCategoryRepository;
        this.targetTypeRepository = targetTypeRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.wardRepository = wardRepository;
        this.agentRepository = agentRepository;
        this.supervisorRepository = supervisorRepository;
    }

    public void validateState(StateDto stateDto) {
        if (stateDto.getName() == null || stateDto.getName().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");
    }

    public void validateTask(TaskDto taskDto) {
        if (taskDto.getName() == null || taskDto.getName().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");
        if(taskDto.getTaskType() == null || taskDto.getTaskType().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Task type cannot be empty");
        if(taskDto.getPriority() == null || taskDto.getPriority().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Priority cannot be empty");
    }


    public void validateLGA (LGADto lgaDto){
        if (lgaDto.getName() == null || lgaDto.getName().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");

        State state = stateRepository.findById(lgaDto.getStateId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid State id!"));
    }


    public void validateCountry(CountryDto countryDto) {
        if (countryDto.getName() == null || countryDto.getName().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");
        if(countryDto.getCode() == null || countryDto.getCode().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Code cannot be empty");
    }


    public void validateIdType(IdTypeDto idTypeDto) {
        if (idTypeDto.getName() == null || idTypeDto.getName().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");
    }


    public void validateBank(BankDto bankDto) {
        if (bankDto.getName() == null || bankDto.getName().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");
        if (bankDto.getBankCode() == null || bankDto.getBankCode().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Bank code cannot be empty");
    }


    public void validateAgentCategory(AgentCategoryDto agentCategoryDto) {
        if (agentCategoryDto.getName() == null || agentCategoryDto.getName().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");
    }

    public void validateMarket(MarketDto marketDto){
        if(marketDto.getName() == null || marketDto.getName().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");
        Ward ward = wardRepository.findById(marketDto.getWardId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid ward id!"));

    }

    public void validateWard (WardDto wardDto){
        if (wardDto.getName() == null || wardDto.getName().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");

        LGA lga = lgaRepository.findById(wardDto.getLgaId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid LGA id!"));
    }

    public void validateSupervisor (SupervisorDto supervisorDto){
        User user = userRepository.findById(supervisorDto.getUserId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid User ID!"));
    }

    public void validateTargetType (TargetTypeDto targetTypeDto){
        if (targetTypeDto.getName() == null || targetTypeDto.getName().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");

    }

    public void validateAgentCategoryTarget (AgentCategoryTargetDto agentCategoryTargetDto){
        if (agentCategoryTargetDto.getName() == null || agentCategoryTargetDto.getName().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");

        AgentCategory agentCategory =  agentCategoryRepository.findById(agentCategoryTargetDto.getAgentCategoryId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Agent Category!"));

        TargetType targetType = targetTypeRepository.findById(agentCategoryTargetDto.getTargetTypeId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Target Type!"));
    }

    public void validateAgentSupervisor(com.sabi.agent.core.dto.agentDto.requestDto.AgentSupervisor request) {
        if (request.getAgent() == null )
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Agent cannot be empty");
        if (request.getSupervisor() == null)
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Supervisor cannot be empty");

        agentRepository.findById(request.getAgent().getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Agent"));
        supervisorRepository.findById(request.getSupervisor().getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid supervisor"));
    }

    public void validateCreditLevel(CreditLevelDto request) {
        if (request.getLimits() == null || request.getLimits().compareTo(BigDecimal.ZERO) < 0)
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST,
                    "Limit cannot be empty or less than zero");

        AgentCategory agentCategory = agentCategoryRepository.findById(request.getAgentCategoryId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Agent category id!"));
    }



    public void validateAgentCategoryTask (AgentCategoryTaskDto agentCategoryTaskDto){
        if (agentCategoryTaskDto.getName() == null || agentCategoryTaskDto.getName().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");

        AgentCategory agentCategory =  agentCategoryRepository.findById(agentCategoryTaskDto.getAgentCategoryId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Agent Category!"));

        Task task = taskRepository.findById(agentCategoryTaskDto.getTaskId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Target Type!"));
    }
    public void validateAgentNetwork(AgentNetworkDto request) {
        if (request.getAgentId() == null )
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Agent cannot be empty");
        if (request.getSubAgentId() == null)
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Supervisor cannot be empty");
        if (request.getAgentId() == request.getSubAgentId())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, " Agent Id and Subagent Id can not be the same");

        agentRepository.findById(request.getAgentId()).orElseThrow(() ->
                new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Agent id!")
        );
        agentRepository.findById(request.getSubAgentId()).orElseThrow(() ->
                new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " SubAgent Does not Exist!")
        );

    }

    public void validateAgentTarget(AgentTargetDto request) {
        if(request.getName() == null)
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, " Agent Target name can not be empty");
        if (request.getTargetType() == null )
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Target Type cannot be empty");
        if (request.getMax() == null )
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Max cannot be empty");
        if (request.getMin() == null )
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Min cannot be empty");
        if (request.getAgent() == null )
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Agent cannot be empty");
        if (request.getSuperMax() == null )
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Super max cannot be empty");

        agentRepository.findById(request.getAgent().getId()).orElseThrow(() ->
                new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Agent Does not Exist!")
        );

    }

    public void validateAgent(CreateAgentRequestDto agent){
        if (agent.getFirstName() == null || agent.getFirstName().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "First name cannot be empty");
        if (agent.getFirstName().length() < 2 || agent.getFirstName().length() > 100)// NAME LENGTH*********
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Invalid first name  length");

        if (agent.getLastName() == null || agent.getLastName().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Last name cannot be empty");
        if (agent.getLastName().length() < 2 || agent.getLastName().length() > 100)// NAME LENGTH*********
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Invalid last name  length");

        if (agent.getEmail() == null || agent.getEmail().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "email cannot be empty");
        if (!Utility.validEmail(agent.getEmail().trim()))
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Invalid Email Address");

        if (agent.getPhone() == null || agent.getPhone().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Phone number cannot be empty");
        if (agent.getPhone().length() < 8 || agent.getPhone().length() > 14)// NAME LENGTH*********
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Invalid phone number  length");
        if (!Utility.isNumeric(agent.getPhone()))
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Invalid data type for phone number ");
        if (agent.getPassword() == null || agent.getPassword().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Password cannot be empty");
        if (agent.getPassword().length() < 8 || agent.getPassword().length() > 20)// NAME LENGTH*********
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Invalid password length");
        if (!PasswordUtil.passwordValidator(agent.getPassword()))
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Invalid Password Format");
    }

    public void validateUserTask(UserTaskDto request){
        if (request.getEndDate() == null )
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "End Date  cannot be empty");
        if (request.getDateAssigned() == null )
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Assigned Date cannot be empty");
        if (request.getStatus() == null )
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Status  cannot be empty");

        Task task =  taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Task!"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid User !"));
    }


}
