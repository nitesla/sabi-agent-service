package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentBvnVerificationDto;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentUpdateDto;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentVerificationDto;
import com.sabi.agent.core.dto.agentDto.requestDto.CreateAgentRequestDto;
import com.sabi.agent.core.dto.requestDto.BvnVerificationData;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.requestDto.ValidateOTPRequest;
import com.sabi.agent.core.dto.responseDto.AgentBvnVerificationResponse;
import com.sabi.agent.core.dto.responseDto.AgentUpdateResponseDto;
import com.sabi.agent.core.dto.responseDto.CreateAgentResponseDto;
import com.sabi.agent.core.dto.responseDto.QueryAgentResponseDto;
import com.sabi.agent.core.models.*;
import com.sabi.agent.core.models.agentModel.Agent;
import com.sabi.agent.core.models.agentModel.AgentCategory;
import com.sabi.agent.core.models.agentModel.AgentVerification;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.*;
import com.sabi.agent.service.repositories.agentRepo.AgentCategoryRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentVerificationRepository;
import com.sabi.framework.dto.requestDto.ChangePasswordDto;
import com.sabi.framework.exceptions.BadRequestException;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.helpers.API;
import com.sabi.framework.models.PreviousPasswords;
import com.sabi.framework.models.User;
import com.sabi.framework.repositories.PreviousPasswordRepository;
import com.sabi.framework.repositories.UserRepository;
import com.sabi.framework.service.ExternalTokenService;
import com.sabi.framework.utils.Constants;
import com.sabi.framework.utils.CustomResponseCode;
import com.sabi.framework.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AgentService {

    @Autowired
    private API api;
    @Value("${bvn.url}")
    private String bvnUrl;

    @Autowired
    private PasswordEncoder passwordEncoder;
    private AgentVerificationRepository agentVerificationRepository;
    private ExternalTokenService externalTokenService;
    private CountryRepository countryRepository;
    private BankRepository bankRepository;
    private StateRepository stateRepository;
    private IdTypeRepository idTypeRepository;
    private CreditLevelRepository creditLevelRepository;
    private SupervisorRepository supervisorRepository;
    private PreviousPasswordRepository previousPasswordRepository;
    private UserRepository userRepository;
    private AgentRepository agentRepository;
    private AgentCategoryRepository agentCategoryRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;

    public AgentService(AgentVerificationRepository agentVerificationRepository,ExternalTokenService externalTokenService,CountryRepository countryRepository,
                        BankRepository bankRepository,StateRepository stateRepository,IdTypeRepository idTypeRepository,
                        CreditLevelRepository creditLevelRepository,SupervisorRepository supervisorRepository,
                        PreviousPasswordRepository previousPasswordRepository,UserRepository userRepository,AgentRepository agentRepository,
                        AgentCategoryRepository agentCategoryRepository, ModelMapper mapper, ObjectMapper objectMapper,
                        Validations validations) {
        this.agentVerificationRepository = agentVerificationRepository;
        this.externalTokenService = externalTokenService;
        this.countryRepository = countryRepository;
        this.bankRepository = bankRepository;
        this.stateRepository = stateRepository;
        this.idTypeRepository = idTypeRepository;
        this.creditLevelRepository = creditLevelRepository;
        this.supervisorRepository = supervisorRepository;
        this.previousPasswordRepository = previousPasswordRepository;
        this.userRepository = userRepository;
        this.agentRepository = agentRepository;
        this.agentCategoryRepository = agentCategoryRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
    }


    /** <summary>
     * Agent  creation
     * </summary>
     * <remarks>this method is responsible for creation of new agent </remarks>
     */
    public CreateAgentResponseDto agentSignUp(CreateAgentRequestDto request) {
         validations.validateAgent(request);
        User user = mapper.map(request,User.class);
        User userExist = userRepository.findByPhone(request.getPhone());
        if(userExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent user already exist");
        }
        String password = Utility.getSaltString();
        user.setPassword(passwordEncoder.encode(password));
        user.setUserCategory(Constants.AGENT_USER);
        user.setCreatedBy(0l);
        user.setActive(false);
        user = userRepository.save(user);
        log.debug("Create new agent user - {}"+ new Gson().toJson(user));

        PreviousPasswords previousPasswords = PreviousPasswords.builder()
                .userId(user.getId())
                .password(user.getPassword())
                .build();
        previousPasswordRepository.save(previousPasswords);

        Agent saveAgent = new Agent();
                saveAgent.setUserId(user.getId());
                saveAgent.setReferrer(Utility.guidID());
                saveAgent.setRegistrationToken(Utility.registrationCode());
                saveAgent.setRegistrationTokenExpiration(Utility.expiredTime());
                saveAgent.setActive(false);
                saveAgent.setCreatedBy(0l);
           agentRepository.save(saveAgent);
        return mapper.map(user, CreateAgentResponseDto.class);
    }



    /** <summary>
     * OTP verification
     * </summary>
     * <remarks>this method is responsible for verifying OTP</remarks>
     */
      public void validateOTP (ValidateOTPRequest request){
          Agent otpExist = agentRepository.findByRegistrationToken(request.getRegistrationToken());
          if(otpExist ==null){
              throw new BadRequestException(CustomResponseCode.BAD_REQUEST, " Invalid OTP supplied");
          }
          DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          Calendar calobj = Calendar.getInstance();
          String currentDate = df.format(calobj.getTime());
          String regDate = otpExist.getRegistrationTokenExpiration();
          String result = String.valueOf(currentDate.compareTo(regDate));
          if(result.equals("1")){
              throw new BadRequestException(CustomResponseCode.BAD_REQUEST, " OTP invalid/expired");
          }
          request.setUpdatedBy(0l);
          request.setIsActive(true);
          Agent response = agentOTPValidation(otpExist,request);

          User userExist  = userRepository.findById(response.getUserId())
                  .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                          "Requested user does not exist!" + response.getUserId()));
          userExist.setActive(true);
          userExist.setUpdatedBy(0l);
          userRepository.save(userExist);

      }

    public Agent agentOTPValidation(Agent agent, ValidateOTPRequest validateOTPRequest) {
        agent.setUpdatedBy(validateOTPRequest.getUpdatedBy());
        agent.setActive(validateOTPRequest.getIsActive());
        return agentRepository.saveAndFlush(agent);
    }




    /** <summary>
     * Change password for first time login for Agent
     * </summary>
     * <remarks>this method is responsible for changing password</remarks>
     */

    public void agentPasswordActivation(ChangePasswordDto request) {

        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested user id does not exist!"));
        mapper.map(request, user);

        String password = request.getPassword();
        user.setPassword(passwordEncoder.encode(password));
        user.setPasswordChangedOn(LocalDateTime.now());
        user = userRepository.save(user);

        PreviousPasswords previousPasswords = PreviousPasswords.builder()
                .userId(user.getId())
                .password(user.getPassword())
                .build();
        previousPasswordRepository.save(previousPasswords);

    }


    /** <summary>
     * Agent  update
     * </summary>
     * <remarks>this method is responsible for updating already existing Agent c</remarks>
     */

    public AgentUpdateResponseDto updateAgent(AgentUpdateDto request) {
        Agent agent = agentRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested  id does not exist!"));
        mapper.map(request, agent);
        agent.setUpdatedBy(0l);
        agentRepository.save(agent);
        log.debug("Agent record updated - {}"+ new Gson().toJson(agent));
        return mapper.map(agent, AgentUpdateResponseDto.class);
    }




    /** <summary>
     * Find Agent
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */
    public QueryAgentResponseDto findAgent(Long id){
        Agent agent  = agentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent id does not exist!"));
        AgentCategory category = agentCategoryRepository.findById(agent.getAgentCategoryId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "  Id does not exist!"));
        User user = userRepository.findById(agent.getUserId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "  Id does not exist!"));
        Supervisor supervisor = supervisorRepository.findById(agent.getSupervisorId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "  Id does not exist!"));
        CreditLevel creditLevel = creditLevelRepository.findById(agent.getCreditLevelId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "  Id does not exist!"));
        IdType idType = idTypeRepository.findById(agent.getIdTypeId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "  Id does not exist!"));
        State state = stateRepository.findById(agent.getStateId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "  Id does not exist!"));
        Bank bank = bankRepository.findById(agent.getBankId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "  Id does not exist!"));
        Country country = countryRepository.findById(agent.getCountryId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "  Id does not exist!"));
        QueryAgentResponseDto response = QueryAgentResponseDto.builder()
                .id(agent.getId())
                .agentCategory(category.getName())
                .user(user.getFirstName()+ " " + user.getLastName())
                .scope(agent.getScope())
                .referralCode(agent.getReferralCode())
                .address(agent.getAddress())
                .bvn(agent.getBvn())
                .agentType(agent.getAgentType())
                .creditLimit(agent.getCreditLimit())
                .payBackDuration(agent.getPayBackDuration())
                .comment(agent.getComment())
                .cardToken(agent.getCardToken())
                .status(agent.getStatus())
                .walletId(agent.getWalletId())
                .picture(agent.getPicture())
                .hasCustomizedTarget(agent.getHasCustomizedTarget())
                .creditLevel(creditLevel.getLimits())
                .idType(idType.getName())
                .state(state.getName())
                .bank(bank.getName())
                .country(country.getName())
                .createdDate(agent.getCreatedDate())
                .createdBy(agent.getCreatedBy())
                .updatedBy(agent.getUpdatedBy())
                .updatedDate(agent.getUpdatedDate())
                .isActive(agent.isActive())
                .build();
        return response;
    }


    /** <summary>
     * Find all Agent
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */

    public Page<Agent> findAll(Long userId,Boolean isActive, PageRequest pageRequest ) {
        Page<Agent> agents = agentRepository.findAgents(userId,isActive, pageRequest);
        if (agents == null) {
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        return agents;

    }



    /** <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a country</remarks>
     */
    public void enableDisEnableAgent (EnableDisEnableDto request){
        Agent agent  = agentRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent Id does not exist!"));
        agent.setActive(request.isActive());
        agent.setUpdatedBy(0l);
        agentRepository.save(agent);

    }


    public List<Agent> getAll(Boolean isActive){
        List<Agent> agents = agentRepository.findByIsActive(isActive);
        return agents;

    }


    public void agentAddressVerifications (AgentVerificationDto request) {
        Agent agent = agentRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent Id does not exist!"));
            agent.setAddress(request.getAddress());
            agent = agentRepository.save(agent);

            AgentVerification addressVerification = AgentVerification.builder()
                    .agentId(agent.getId())
                    .component(agent.getAddress())
                    .dateSubmitted(agent.getCreatedDate())
                    .status(0)
                    .build();
        log.debug("address verification - {}"+ new Gson().toJson(addressVerification));
            agentVerificationRepository.save(addressVerification);
    }



    public void agentBvnVerifications (AgentBvnVerificationDto request) {
        Agent agent = agentRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent Id does not exist!"));
           User user = userRepository.getOne(agent.getUserId());
          log.info("::: agentUser ::" + user);

        BvnVerificationData data = BvnVerificationData.builder()
                .bvn(request.getBvn())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhone())
                .accountNumber(request.getAccountNumber())
                .bankCode(request.getBankCode())
                .build();

        Map map=new HashMap();
        map.put("Authorization",externalTokenService.getToken());

       AgentBvnVerificationResponse response = api.post(bvnUrl, data,AgentBvnVerificationResponse.class,map);
       if(response.getStatus().equals(false)){
           throw new BadRequestException(CustomResponseCode.BAD_REQUEST, " BVN validation failed !");
       }else {

           agent.setBvn(response.getData().getBvn());
           agent = agentRepository.save(agent);
           log.debug("bvn updated - {}"+ new Gson().toJson(agent));

           AgentVerification bvnVerification = AgentVerification.builder()
                   .agentId(agent.getId())
                   .component(agent.getBvn())
                   .dateSubmitted(agent.getCreatedDate())
                   .status(0)
                   .build();
           log.debug("bvn verification - {}"+ new Gson().toJson(bvnVerification));
           agentVerificationRepository.save(bvnVerification);
       }
    }

    public void agentIdCardVerifications (AgentVerificationDto request) {
        Agent agent = agentRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent Id does not exist!"));
            agent.setIdCard(request.getIdCard());
            agent = agentRepository.save(agent);

            AgentVerification idVerification = AgentVerification.builder()
                    .agentId(agent.getId())
                    .component(agent.getIdCard())
                    .dateSubmitted(agent.getCreatedDate())
                    .status(0)
                    .build();
        log.debug("id verification - {}"+ new Gson().toJson(idVerification));
            agentVerificationRepository.save(idVerification);
        }




    }


