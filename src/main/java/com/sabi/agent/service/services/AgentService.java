package com.sabi.agent.service.services;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.ValidateEmailOtpRequest;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentBvnVerificationDto;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentUpdateDto;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentVerificationDto;
import com.sabi.agent.core.dto.agentDto.requestDto.CreateAgentRequestDto;
import com.sabi.agent.core.dto.requestDto.*;
import com.sabi.agent.core.dto.responseDto.*;
import com.sabi.agent.core.models.agentModel.Agent;
import com.sabi.agent.core.models.agentModel.AgentVerification;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.*;
import com.sabi.agent.service.repositories.agentRepo.AgentCategoryRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentVerificationRepository;
import com.sabi.framework.dto.requestDto.ChangePasswordDto;
import com.sabi.framework.exceptions.BadRequestException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.helpers.API;
import com.sabi.framework.models.PreviousPasswords;
import com.sabi.framework.models.User;
import com.sabi.framework.notification.requestDto.NotificationRequestDto;
import com.sabi.framework.notification.requestDto.RecipientRequest;
import com.sabi.framework.repositories.PreviousPasswordRepository;
import com.sabi.framework.repositories.UserRepository;
import com.sabi.framework.service.ExternalTokenService;
import com.sabi.framework.service.NotificationService;
import com.sabi.framework.service.TokenService;
import com.sabi.framework.utils.Constants;
import com.sabi.framework.utils.CustomResponseCode;
import com.sabi.framework.utils.Utility;
import lombok.extern.slf4j.Slf4j;

import org.apache.http.protocol.HTTP;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

//1
@SuppressWarnings("ALL")
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
    private NotificationService notificationService;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;

    public AgentService(AgentVerificationRepository agentVerificationRepository,ExternalTokenService externalTokenService,CountryRepository countryRepository,
                        BankRepository bankRepository,StateRepository stateRepository,IdTypeRepository idTypeRepository,
                        CreditLevelRepository creditLevelRepository,SupervisorRepository supervisorRepository,
                        PreviousPasswordRepository previousPasswordRepository,UserRepository userRepository,AgentRepository agentRepository,
                        AgentCategoryRepository agentCategoryRepository,NotificationService notificationService, ModelMapper mapper, ObjectMapper objectMapper,
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
        this.notificationService = notificationService;
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
        String password = Utility.getSaltString();
        user.setPassword(passwordEncoder.encode(password));
        user.setUserCategory(Constants.AGENT_USER);
        user.setUsername(request.getPhone());
        user.setLoginAttempts(0l);
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
                saveAgent.setReferralCode(Utility.guidID());
                saveAgent.setRegistrationToken(Utility.registrationCode());
                saveAgent.setRegistrationTokenExpiration(Utility.expiredTime());
                saveAgent.setActive(false);
                saveAgent.setIsEmailVerified(false);
                saveAgent.setCreatedBy(0l);
        Agent agentResponse= agentRepository.save(saveAgent);
        log.debug("Create new agent  - {}"+ new Gson().toJson(saveAgent));

// --------  sending token to agent -----------

        NotificationRequestDto notificationRequestDto = new NotificationRequestDto();
        User emailRecipient = userRepository.getOne(user.getId());
        notificationRequestDto.setMessage("Activation Otp " + " " + agentResponse.getRegistrationToken());
        List<RecipientRequest> recipient = new ArrayList<>();
        recipient.add(RecipientRequest.builder()
                .email(emailRecipient.getEmail())
                .build());
        notificationRequestDto.setRecipient(recipient);
        System.out.println(":::::: AGENT NOTIFICATION ::::" + notificationRequestDto);
        notificationService.emailNotificationRequest(notificationRequestDto);


        return mapper.map(user, CreateAgentResponseDto.class);
    }





    /** <summary>
     * Resend OTP
     * </summary>
     * <remarks>this method is responsible for resending OTP</remarks>
     */
    public  void resendAgentOTP (ResendOTP request) {
        User user = userRepository.findByPhone(request.getPhone());
        if(user == null){
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, "Invalid phone number");
        }
       Agent agent = agentRepository.findByUserId(user.getId());
        agent.setRegistrationToken(Utility.registrationCode());
        agent.setRegistrationTokenExpiration(Utility.expiredTime());
        Agent agentResponse = agentRepository.save(agent);

        NotificationRequestDto notificationRequestDto = new NotificationRequestDto();
        User emailRecipient = userRepository.getOne(user.getId());
        notificationRequestDto.setMessage("Activation Otp " + " " + agentResponse.getRegistrationToken());
        List<RecipientRequest> recipient = new ArrayList<>();
        recipient.add(RecipientRequest.builder()
                .email(emailRecipient.getEmail())
                .build());
        notificationRequestDto.setRecipient(recipient);
        System.out.println(":::::: AGENT NOTIFICATION ::::" + notificationRequestDto);
        notificationService.emailNotificationRequest(notificationRequestDto);

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

    public AgentActivationResponse agentPasswordActivation(ChangePasswordDto request) {

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

        Agent agent = agentRepository.findByUserId(user.getId());

        AgentActivationResponse response = AgentActivationResponse.builder()
                .userId(user.getId())
                .agentId(agent.getId())
                .phone(user.getPhone())
                .build();

        return response;
    }


    /** <summary>
     * Agent  update
     * </summary>
     * <remarks>this method is responsible for updating already existing Agent c</remarks>
     */

    public AgentUpdateResponseDto updateAgent(AgentUpdateDto request) {
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        Agent agent = agentRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested  id does not exist!"));
        mapper.map(request, agent);
        agent.setUpdatedBy(userCurrent.getId());
        agentRepository.save(agent);
        log.debug("Agent record updated - {}"+ new Gson().toJson(agent));
        return mapper.map(agent, AgentUpdateResponseDto.class);
    }




    /** <summary>
     * Find Agent
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */
    public Agent findAgent(Long id){
        Agent agent  = agentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent id does not exist!"));
            User user = userRepository.getOne(agent.getUserId());
            agent.setFirstName(user.getFirstName());
            agent.setLastName(user.getLastName());
            agent.setEmail(user.getEmail());
            agent.setPhone(user.getPhone());
        return agent;
    }




    /** <summary>
     * Find all Agent
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */

    public Page<Agent> findAll(Long userId,Boolean isActive,String referrer, PageRequest pageRequest ) throws Exception {
//        log.info("the lastName value is {} " + lastName );
        Page<Agent> agents = agentRepository.findAgents(userId, isActive, referrer, pageRequest);
        if (agents == null) {
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        log.info("agets fetched " + objectMapper.writeValueAsString(agents));

        agents.getContent().forEach(agent -> {
            User user = userRepository.getOne(agent.getId());
            agent.setLastName(user.getLastName());
            agent.setFirstName(user.getFirstName());
            agent.setEmail(user.getEmail());
            agent.setPhone(user.getPhone());
        });
        log.info("agets fetched 222222: " + this.objectMapper.writeValueAsString(agents));
        return agents;

    }


//    *****

    public Agent findByFirstNameOrLastName(Boolean isActive,String referrer,String firstName,
                                           String lastName) {
        Agent savedAgent;
        User savedUser;
        if (firstName != null){
            savedUser =  userRepository.findByFirstName(firstName);
        } else if(lastName != null) {
            savedUser = userRepository.findByLastName(lastName);
        } else {
            throw new BadRequestException(String.valueOf(HttpStatus.BAD_REQUEST.value()),
                    "lastname or firstname can not be null!");
        }
        if (savedUser != null){
           savedAgent = findAgent(savedUser.getId());
           if (savedAgent != null){
               return savedAgent;
           } else {
               throw new NotFoundException(String.valueOf(HttpStatus.NOT_FOUND.value()),"Agent Not Found!");
           }
        }
        else {
            throw new NotFoundException(String.valueOf(HttpStatus.NOT_FOUND.value()),"User Not Found!");
        }
    }

    public Page<Agent> findAllAgentsBySort(Long userId,Boolean isActive,String referrer,String firstName,String lastName,PageRequest pageRequest) throws Exception {
        Page<Agent> savedAgent;
        if (firstName != null){
            Agent agent =  findByFirstNameOrLastName(isActive,referrer,firstName,lastName);
            List<Agent> listOfAgent = new ArrayList<>();
            listOfAgent.add(agent);
             savedAgent = new PageImpl<>(listOfAgent);

//            savedAgent = (Page<Agent>) findByFirstNameOrLastName(isActive,referrer,firstName,lastName);
        } else if (lastName != null){
            Agent agent =  findByFirstNameOrLastName(isActive,referrer,firstName,lastName);
            List<Agent> listOfAgent = new ArrayList<>();
            listOfAgent.add(agent);
            savedAgent = new PageImpl<>(listOfAgent);
            savedAgent = (Page<Agent>) findByFirstNameOrLastName(isActive,referrer,lastName,firstName);
        } else
            savedAgent = findAll(userId,isActive,referrer, pageRequest);
        return savedAgent;
    }



    /** <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a country</remarks>
     */
    public void enableDisEnableAgent (EnableDisEnableDto request){
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        Agent agent  = agentRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent Id does not exist!"));
        agent.setActive(request.isActive());
        agent.setUpdatedBy(userCurrent.getId());
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
                    .name("ADDRESS")
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

       AgentBvnVerificationResponse response = api.post(bvnUrl, data, AgentBvnVerificationResponse.class,map);
       if(response.getStatus().equals(false)){
           throw new BadRequestException(CustomResponseCode.BAD_REQUEST, " BVN validation failed !");
       }else {

           agent.setBvn(response.getData().getBvn());
           agent = agentRepository.save(agent);
           log.debug("bvn updated - {}"+ new Gson().toJson(agent));

           AgentVerification bvnVerification = AgentVerification.builder()
                   .name("BVN")
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
                    .name("IDCARD")
                    .agentId(agent.getId())
                    .component(agent.getIdCard())
                    .dateSubmitted(agent.getCreatedDate())
                    .status(0)
                    .build();
        log.debug("id verification - {}"+ new Gson().toJson(idVerification));
            agentVerificationRepository.save(idVerification);
        }



    public EmailVerificationResponseDto agentEmailVerifications (EmailVerificationDto request) {
        User user = userRepository.findByEmail(request.getEmail());
        if(user == null){
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " Email does not exist !");
        }

        Agent agent = agentRepository.findByUserId(user.getId());
        agent.setRegistrationToken(Utility.registrationCode());
        agent.setRegistrationTokenExpiration(Utility.expiredTime());
        agent.setIsEmailVerified(false);
        Agent agentResponse = agentRepository.save(agent);


        NotificationRequestDto notificationRequestDto = new NotificationRequestDto();
        User emailRecipient = userRepository.getOne(user.getId());
        notificationRequestDto.setMessage("Activation Otp " + " " + agentResponse.getRegistrationToken());
        List<RecipientRequest> recipient = new ArrayList<>();
        recipient.add(RecipientRequest.builder()
                .email(emailRecipient.getEmail())
                .build());
        notificationRequestDto.setRecipient(recipient);
        notificationService.emailNotificationRequest(notificationRequestDto);

        EmailVerificationResponseDto responseDto = EmailVerificationResponseDto.builder()
                .email(user.getEmail())
                .isEmailVerified(agentResponse.getIsEmailVerified())
                .build();
        return responseDto;
    }

    public EmailVerificationResponseDto agentPhoneVerifications (EmailVerificationDto request) {
        User user = userRepository.findByPhone(request.getPhone());
        if(user == null){
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " Phone number does not exist !");
        }

        Agent agent = agentRepository.findByUserId(user.getId());
        agent.setRegistrationToken(Utility.registrationCode());
        agent.setRegistrationTokenExpiration(Utility.expiredTime());
        agent.setIsEmailVerified(false);
        Agent agentResponse = agentRepository.save(agent);


        NotificationRequestDto notificationRequestDto = new NotificationRequestDto();
        User emailRecipient = userRepository.getOne(user.getId());
        notificationRequestDto.setMessage("Activation Otp " + " " + agentResponse.getRegistrationToken());
        List<RecipientRequest> recipient = new ArrayList<>();
        recipient.add(RecipientRequest.builder()
                .email(emailRecipient.getEmail())
                .build());
        notificationRequestDto.setRecipient(recipient);
        notificationService.emailNotificationRequest(notificationRequestDto);

        EmailVerificationResponseDto responseDto = EmailVerificationResponseDto.builder()
                .phone(user.getPhone())
                .isEmailVerified(agentResponse.getIsEmailVerified())
                .build();
        return responseDto;
    }


    public void validateOTPForEmailVerification (ValidateEmailOtpRequest request){
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
        request.setIsEmailVerified(true);
        Agent response = emailOTPValidation(otpExist,request);


    }


    public Agent emailOTPValidation(Agent agent, ValidateEmailOtpRequest validateOTPRequest) {
        agent.setIsEmailVerified(validateOTPRequest.getIsEmailVerified());
        return agentRepository.saveAndFlush(agent);
    }


    }


