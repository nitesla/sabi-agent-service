package com.sabi.agent.service.helper;


import com.sabi.agent.core.dto.agentDto.requestDto.*;
import com.sabi.agent.core.dto.requestDto.*;
import com.sabi.agent.core.integrations.order.PlaceOrder;
import com.sabi.agent.core.merchant_integration.request.MerchantSignUpRequest;
import com.sabi.agent.core.models.*;
import com.sabi.agent.core.models.agentModel.Agent;
import com.sabi.agent.core.models.agentModel.AgentCategory;
import com.sabi.agent.core.models.agentModel.AgentVerification;
import com.sabi.agent.service.repositories.*;
import com.sabi.agent.service.repositories.agentRepo.AgentCategoryRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentVerificationRepository;
import com.sabi.framework.exceptions.BadRequestException;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.models.User;
import com.sabi.framework.repositories.UserRepository;
import com.sabi.framework.utils.CustomResponseCode;
import com.sabi.framework.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private MarketRepository marketRepository;
    private AgentVerificationRepository agentVerificationRepository;

    @Autowired
    private BankRepository bankRepository;


    public Validations(StateRepository stateRepository, MarketRepository marketRepository,
                       LGARepository lgaRepository, AgentCategoryRepository agentCategoryRepository,
                       TargetTypeRepository targetTypeRepository, TaskRepository taskRepository,
                       UserRepository userRepository, WardRepository wardRepository, AgentRepository agentRepository,
                       SupervisorRepository supervisorRepository, AgentVerificationRepository agentVerificationRepository) {
        this.stateRepository = stateRepository;
        this.lgaRepository = lgaRepository;
        this.agentCategoryRepository = agentCategoryRepository;
        this.targetTypeRepository = targetTypeRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.wardRepository = wardRepository;
        this.agentRepository = agentRepository;
        this.marketRepository = marketRepository;
        this.supervisorRepository = supervisorRepository;
        this.agentVerificationRepository = agentVerificationRepository;
    }

    public void validateState(StateDto stateDto) {
        if (stateDto.getName() == null || stateDto.getName().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");
        if (stateDto.getName() == null || stateDto.getName().trim().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");
        if (!Utility.validateName(stateDto.getName()))
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Invalid data type for Name ");

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
        if (lgaDto.getName() == null || lgaDto.getName().trim().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");
        if (!Utility.validateName(lgaDto.getName()))
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Invalid data type for Name ");

        State state = stateRepository.findById(lgaDto.getStateId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid State id!"));
    }


    public void validateCountry(CountryDto countryDto) {
        String valName = countryDto.getName();
        char valCharName = valName.charAt(0);
        if (Character.isDigit(valCharName)){
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name can not start with a number");
        }
        if ((Character.isWhitespace(countryDto.getName().charAt(0))) ||(Character.isWhitespace(countryDto.getCode().charAt(0))) ){
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Request parameter can not start with space! ");
        }
        if (countryDto.getName() == null || countryDto.getName().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");
        if(countryDto.getCode() == null || countryDto.getCode().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Code cannot be empty");
    }


    public void validateIdType(IdTypeDto idTypeDto) {
        if (idTypeDto.getName() == null || idTypeDto.getName().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");
        if (idTypeDto.getName() == null || idTypeDto.getName().trim().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");
        if (!Utility.validateName(idTypeDto.getName()))
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Invalid data type for Name ");

    }

    public void validateStatus(Boolean status) {
        if (status == null)
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Status cannot be empty");
//        if (!Utility.validateEnableDisable(status))
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Invalid data type for Status ");

    }


    public void validateBank(BankDto bankDto) {
        String valName = bankDto.getName();
        char valCharName = valName.charAt(0);
        if (Character.isDigit(valCharName)){
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name can not start with a number");
        }
        if (bankDto.getName() == null || bankDto.getName().trim().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");
        if (bankDto.getBankCode() == null || bankDto.getBankCode().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Bank code cannot be empty");
    }


    public void validateAgentCategory(AgentCategoryDto agentCategoryDto) {
        String valName = agentCategoryDto.getName();
        String valDescription = agentCategoryDto.getDescription();
        char valCharName = valName.charAt(0);
        char valcharDescription  = valDescription.charAt(0);
        if (Character.isDigit(valCharName)){
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name can not start with a number");
        }
        if (Character.isDigit(valcharDescription)){
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Description can not start with a number");
        }
        if ((Character.isWhitespace(agentCategoryDto.getName().charAt(0))) ||(Character.isWhitespace(agentCategoryDto.getDescription().charAt(0))) ){
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Request parameter can not start with space! ");
        }
        if(agentCategoryDto.getDescription() == null || agentCategoryDto.getDescription().trim().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Description cannot be empty");
        if(agentCategoryDto.getImage().trim()==null || agentCategoryDto.getImage().trim().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Image cannot be empty");
        if (agentCategoryDto.getName() == null || agentCategoryDto.getName().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");
    }

    public void validateMarket(MarketDto marketDto){
        if(marketDto.getName() == null || marketDto.getName().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");
        if(marketDto.getWardId() == null || marketDto.getWardId() < 0 )
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Ward Id cannot be empty");
        Ward ward = wardRepository.findById(marketDto.getWardId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid ward id!"));

    }

    public void validateWard (WardDto wardDto){
        if (wardDto.getName() == null || wardDto.getName().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");

        if (!Utility.validateName(wardDto.getName()))
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Invalid data type for Name ");


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
        if (!Utility.validateName(targetTypeDto.getName()))
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Invalid data type for Name ");

    }

    public void validateAgentCategoryTarget (AgentCategoryTargetDto agentCategoryTargetDto){
        if (agentCategoryTargetDto.getName() == null || agentCategoryTargetDto.getName().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Name cannot be empty");
        if (!Utility.validateName(agentCategoryTargetDto.getName()))
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Invalid data type for Name ");

        AgentCategory agentCategory =  agentCategoryRepository.findById(agentCategoryTargetDto.getAgentCategoryId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Agent Category!"));

        TargetType targetType = targetTypeRepository.findById(agentCategoryTargetDto.getTargetTypeId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Target Type!"));
    }

    public void validateAgentBank (AgentBankDto agentBankDto){
        if (agentBankDto.getAccountNumber().trim() == null)
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Account Number cannot be empty");
        if (!Utility.isNumeric(agentBankDto.getAccountNumber()))
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Invalid data type for Account Number ");


        Agent agent =  agentRepository.findById(agentBankDto.getAgentId()).orElseThrow(() ->
                new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Agent Id!")
        );

        Bank bank = bankRepository.findById(agentBankDto.getBankId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Bank!"));
    }

    public void validateAgentSupervisor(AgentSupervisorDto request) {
        if (request.getAgentId() == null )
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Agent cannot be empty");
        if (request.getSupervisorId() == null)
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Supervisor cannot be empty");

        agentRepository.findById(request.getAgentId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Agent"));
        supervisorRepository.findById(request.getSupervisorId())
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
        if (!Utility.validateName(agentCategoryTaskDto.getName()))
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Invalid data type for Name ");


        AgentCategory agentCategory =  agentCategoryRepository.findById(agentCategoryTaskDto.getAgentCategoryId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Agent Category!"));

        Task task = taskRepository.findById(agentCategoryTaskDto.getTaskId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Task!"));
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
        if (request.getTargetId() == null )
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Target Type cannot be empty");
        if (request.getMax() == null )
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Max cannot be empty");
        if (request.getMin() == null )
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Min cannot be empty");
        if (request.getAgentId() == null )
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Agent Id cannot be empty");
        if (request.getSuperMax() == null )
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Super max cannot be empty");

        agentRepository.findById(request.getAgentId()).orElseThrow(() ->
                new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Agent Does not Exist!")
        );

        targetTypeRepository.findById(request.getTargetId()).orElseThrow(() ->
                new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Target type Does not Exist!")
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
         User user = userRepository.findByEmail(agent.getEmail());
         if(user !=null){
             throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Email already exist");
         }

        if (agent.getPhone() == null || agent.getPhone().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Phone number cannot be empty");
        if (agent.getPhone().length() < 8 || agent.getPhone().length() > 14)// NAME LENGTH*********
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Invalid phone number  length");
        if (!Utility.isNumeric(agent.getPhone()))
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Invalid data type for phone number ");
//        User userExist = userRepository.findByPhone(agent.getPhone());
//        if(userExist !=null){
//            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent user already exist");
//        }
    }


    public void validateUserTask(UserTaskDto request){
        if (request.getEndDate() == null )
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "End Date  cannot be empty");
        if (request.getDateAssigned() == null )
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Assigned Date cannot be empty");
        if (request.getStatus() == null )
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Status  cannot be empty");
        if (!Utility.containsAlphabet(request.getStatus()))
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Invalid data type for Status ");

        Task task =  taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid Task!"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        " Enter a valid User !"));
    }


    public void validateAgentLocation(AgentLocationDto request) {
        if(request.getLocationType().isEmpty() || request.getLocationType() == null)
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Location type  cannot be empty");
        if (request.getAgentId() == null || request.getAgentId() == 0)
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Agent Id type  cannot be empty or zero");
        if(request.getLocationId() == null || request.getLocationId() == 0)
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Location Id  cannot be empty or zero");
        if(request.getLocationName().isEmpty() || request.getLocationName() == null)
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Location name  cannot be empty");

        switch (request.getLocationType()){
            case "Ward":
                wardRepository.findById(request.getLocationId()).orElseThrow(()->
                        new BadRequestException(CustomResponseCode.BAD_REQUEST, "Enter valid Ward Id"));
                 break;
            case "Market":
                marketRepository.findById(request.getLocationId()).orElseThrow(()->
                        new BadRequestException(CustomResponseCode.BAD_REQUEST, "Enter valid Market Id"));
                break;
            case "Lga":
                lgaRepository.findById(request.getLocationId()).orElseThrow(()->
                        new BadRequestException(CustomResponseCode.BAD_REQUEST, "Enter valid Lga Id"));
                break;
            case "State":
                stateRepository.findById(request.getLocationId()).orElseThrow(()->
                        new BadRequestException(CustomResponseCode.BAD_REQUEST, "Enter valid State Id"));
                break;
            default:
                throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Enter a valid location type");
        }
    }


    public void validateVerification (Verification request){
        if (request.getStatus() != CustomResponseCode.ENABLE_VERIFICATION_STATUS || request.getStatus() != CustomResponseCode.FAILED_VERIFICATION_STATUS)
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Invalid status");
    }


    public void validateComponentVerification(AgentVerification request){

        AgentVerification agentVerification = agentVerificationRepository.findByAgentIdAndComponent(request.getAgentId(),request.getComponent());
        if(agentVerification !=null){
            AgentVerification saveVerification = agentVerificationRepository.getOne(agentVerification.getId());
            saveVerification.setComponent(request.getComponent());
            saveVerification.setAgentId(request.getAgentId());

            agentVerificationRepository.save(agentVerification);

        }
    }



    public void validateOrder(PlaceOrder request){
        Agent agent  = agentRepository.findById(request.getAgentId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Agent id does not exist!"));
    }

    public void validateMerchant(MerchantSignUpRequest signUpRequest){
        if(signUpRequest.getAgentId() == null)
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Agent Id can not be null");
        if (signUpRequest.getAgentId().isEmpty())
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Agent Id can not be empty");
        agentRepository.findById(Long.parseLong(signUpRequest.getAgentId())).orElseThrow(()->
                new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Agent Signing up merchant  does not exist"));
    }

//    public void validateAgentCategoryTaskEnable(EnableDisEnableDto enableRequest) {
//        if (!("true".equals(enableRequest.isActive())) || (!("false".equals(enableRequest.isActive())))) {
////            return "true".equals(value) || "false".equals(value);
//            new BadRequestException(CustomResponseCode.BAD_REQUEST,
//                    "Bad Request");
//        }
//    }
}
