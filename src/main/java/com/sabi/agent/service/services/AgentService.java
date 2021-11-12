package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.ValidateEmailOtpRequest;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentUpdateDto;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentVerificationDto;
import com.sabi.agent.core.dto.agentDto.requestDto.CreateAgentRequestDto;
import com.sabi.agent.core.dto.requestDto.EmailVerificationDto;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.requestDto.ResendOTP;
import com.sabi.agent.core.dto.requestDto.ValidateOTPRequest;
import com.sabi.agent.core.dto.responseDto.AgentActivationResponse;
import com.sabi.agent.core.dto.responseDto.AgentUpdateResponseDto;
import com.sabi.agent.core.dto.responseDto.CreateAgentResponseDto;
import com.sabi.agent.core.dto.responseDto.EmailVerificationResponseDto;
import com.sabi.agent.core.models.Country;
import com.sabi.agent.core.models.agentModel.Agent;
import com.sabi.agent.core.models.agentModel.AgentCategory;
import com.sabi.agent.core.models.agentModel.AgentVerification;
import com.sabi.agent.core.wallet_integration.response.WalletBvnResponse;
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
import com.sabi.framework.notification.requestDto.NotificationRequestDto;
import com.sabi.framework.notification.requestDto.RecipientRequest;
import com.sabi.framework.notification.requestDto.SmsRequest;
import com.sabi.framework.repositories.PreviousPasswordRepository;
import com.sabi.framework.repositories.UserRepository;
import com.sabi.framework.service.ExternalTokenService;
import com.sabi.framework.service.NotificationService;
import com.sabi.framework.service.TokenService;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


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

        User exist = userRepository.findByEmailOrPhone(request.getEmail(),request.getPhone());
        if(exist !=null && exist.getPasswordChangedOn()== null){
          Agent existAgent = agentRepository.findByUserId(exist.getId());
            existAgent.setRegistrationToken(Utility.registrationCode("HHmmss"));
            existAgent.setRegistrationTokenExpiration(Utility.expiredTime());
            Agent agentExist =agentRepository.save(existAgent);

            NotificationRequestDto notificationRequestDto = new NotificationRequestDto();
            User emailRecipient = userRepository.getOne(exist.getId());
            notificationRequestDto.setMessage("Activation Otp " + " " + agentExist.getRegistrationToken());
            List<RecipientRequest> recipient = new ArrayList<>();
            recipient.add(RecipientRequest.builder()
                    .email(emailRecipient.getEmail())
                    .build());
            notificationRequestDto.setRecipient(recipient);
            notificationService.emailNotificationRequest(notificationRequestDto);

            SmsRequest smsRequest = SmsRequest.builder()
                    .message("Activation Otp " + " " + agentExist.getRegistrationToken())
                    .phoneNumber(emailRecipient.getPhone())
                    .build();
            notificationService.smsNotificationRequest(smsRequest);
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent user already exist, a new OTP sent to your email");

        }else if(exist !=null && exist.getPasswordChangedOn() !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent user already exist");
        }

        String password = Utility.getSaltString();
        user.setPassword(passwordEncoder.encode(password));
        user.setUserCategory(Constants.OTHER_USER);
        user.setUsername(request.getPhone());
        user.setLoginAttempts(0l);
        user.setCreatedBy(0l);
        user.setIsActive(false);
        user = userRepository.save(user);
        log.debug("Create new agent user - {}"+ new Gson().toJson(user));

        PreviousPasswords previousPasswords = PreviousPasswords.builder()
                .userId(user.getId())
                .password(user.getPassword())
                .build();
        previousPasswordRepository.save(previousPasswords);

        Agent saveAgent = new Agent();
                saveAgent.setUserId(user.getId());
                saveAgent.setCountryCode(request.getCountryCode());
                saveAgent.setReferrer(request.getReferrer());
                saveAgent.setReferralCode(Utility.guidID());
                saveAgent.setRegistrationToken(Utility.registrationCode("HHmmss"));
                saveAgent.setRegistrationTokenExpiration(Utility.expiredTime());
                saveAgent.setIsActive(false);
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
        notificationService.emailNotificationRequest(notificationRequestDto);


        SmsRequest smsRequest = SmsRequest.builder()
                .message("Activation Otp " + " " + agentResponse.getRegistrationToken())
                .phoneNumber(emailRecipient.getPhone())
                .build();
        notificationService.smsNotificationRequest(smsRequest);

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
        agent.setRegistrationToken(Utility.registrationCode("HHmmss"));
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
        notificationService.emailNotificationRequest(notificationRequestDto);

        SmsRequest smsRequest = SmsRequest.builder()
                .message("Activation Otp " + " " + agentResponse.getRegistrationToken())
                .phoneNumber(emailRecipient.getPhone())
                .build();
        notificationService.smsNotificationRequest(smsRequest);


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
          userExist.setIsActive(true);
          userExist.setUpdatedBy(0l);
          userRepository.save(userExist);

      }

    public Agent agentOTPValidation(Agent agent, ValidateOTPRequest validateOTPRequest) {
        agent.setUpdatedBy(validateOTPRequest.getUpdatedBy());
        agent.setIsActive(validateOTPRequest.getIsActive());
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
                        "Requested Agent id does not exist!"));
        if (request.getCountryId() != null) {
            Country savedCountry = countryRepository.findById(request.getCountryId())
                    .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                            "Requested country id does not exist!"));
        }
        AgentCategory savedCategory = agentCategoryRepository.findAgentCategoriesByIsDefault(true);
        if (request.getAgentCategoryId() == null || request.getAgentCategoryId() < 0) {
            agent.setAgentCategoryId(savedCategory.getId());
        }
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
        if(agent.getAgentCategoryId() != null) {
            AgentCategory agentCategory  = agentCategoryRepository.getOne(agent.getAgentCategoryId());
            agent.setAgentCategoryName(agentCategory.getName());
        }
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

    public Page<Agent> findAllAgents(Long userId,Boolean isActive,String referrer, PageRequest pageRequest ) throws Exception {
        Page<Agent> agents = agentRepository.findAgents(userId, isActive, referrer, pageRequest);
        if (agents == null) {
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        agents.getContent().forEach(agent -> {
            User user = userRepository.getOne(agent.getUserId());
           if(agent.getAgentCategoryId() != null) {
               AgentCategory agentCategory  = agentCategoryRepository.getOne(agent.getAgentCategoryId());
                    agent.setAgentCategoryName(agentCategory.getName());
        }
            agent.setLastName(user.getLastName());
            agent.setFirstName(user.getFirstName());
            agent.setEmail(user.getEmail());
            agent.setPhone(user.getPhone());
//            agent.setAgentCategoryName(agentCategory.getName());

        });
        return agents;

    }



    public Page<User> findAgentUser(String firstName,String lastName, PageRequest pageRequest ){
        Page<User> agentUser = userRepository.findAgentUser(firstName,lastName,pageRequest);
        if(agentUser == null){
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        agentUser.getContent().forEach(users -> {
            Agent agent = agentRepository.findByUserId(users.getId());
            users.setAgentId(agent.getId());
            users.setAgentCategoryId(agent.getAgentCategoryId());
            users.setScope(agent.getScope());
            users.setReferralCode(agent.getReferralCode());
            users.setReferrer(agent.getReferrer());
            users.setAddress(agent.getAddress());
            users.setBvn(agent.getBvn());
            users.setAgentType(agent.getAgentType());
            users.setCreditLimit(agent.getCreditLimit());
            users.setPayBackDuration(agent.getPayBackDuration());
            users.setBalance(agent.getBalance());
            users.setVerificationDate(agent.getVerificationDate());
            users.setSupervisorId(agent.getSupervisorId());
            users.setVerificationStatus(agent.getVerificationStatus());
            users.setComment(agent.getComment());
            users.setCardToken(agent.getCardToken());
            users.setStatus(agent.getStatus());
            users.setWalletId(agent.getWalletId());
            users.setPicture(agent.getPicture());
            users.setHasCustomizedTarget(agent.getHasCustomizedTarget());
            users.setCreditLevelId(agent.getCreditLevelId());
            users.setIdTypeId(agent.getIdTypeId());
            users.setIdCard(agent.getIdCard());
            users.setStateId(agent.getStateId());
            users.setBankId(agent.getBankId());
            users.setCountryId(agent.getCountryId());
            users.setAccountNonLocked(agent.isAccountNonLocked());
            users.setRegistrationTokenExpiration(agent.getRegistrationTokenExpiration());
            users.setRegistrationToken(agent.getRegistrationToken());
            users.setIsEmailVerified(agent.getIsEmailVerified());

        });
        return agentUser;

    }







    /** <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a country</remarks>
     */
    public void enableDisEnableAgent (EnableDisEnableDto request){
        validations.validateStatus(request.getIsActive());
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        Agent agent  = agentRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent Id does not exist!"));
        agent.setIsActive(request.getIsActive());
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
        validations.validateComponentVerification(addressVerification);
        log.debug("address verification - {}"+ new Gson().toJson(addressVerification));
            agentVerificationRepository.save(addressVerification);
    }



    public void agentBvnVerifications (WalletBvnResponse bvnResponse, long agentId) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent Id does not exist!"));
           User user = userRepository.getOne(agent.getUserId());
          log.info("::: agentUser ::" + user);


           AgentVerification bvnVerification = AgentVerification.builder()
                   .name("BVN")
                   .agentId(agent.getId())
                   .component(bvnResponse.getData().getData().getBvn())
                   .dateSubmitted(agent.getCreatedDate())
                   .status(0)
                   .build();
           validations.validateComponentVerification(bvnVerification);
           log.debug("bvn verification - {}"+ new Gson().toJson(bvnVerification));
           agentVerificationRepository.save(bvnVerification);
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
        validations.validateComponentVerification(idVerification);
        log.debug("id verification - {}"+ new Gson().toJson(idVerification));
            agentVerificationRepository.save(idVerification);
        }



    public EmailVerificationResponseDto agentEmailVerifications (EmailVerificationDto request) {
        User user = userRepository.findByEmail(request.getEmail());
        if(user == null){
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " Email does not exist !");
        }

        Agent agent = agentRepository.findByUserId(user.getId());
        agent.setRegistrationToken(Utility.registrationCode("HHmmss"));
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
        agent.setRegistrationToken(Utility.registrationCode("HHmmss"));
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

        SmsRequest smsRequest = SmsRequest.builder()
                .message("Activation Otp " + " " + agentResponse.getRegistrationToken())
                .phoneNumber(emailRecipient.getPhone())
                .build();
        notificationService.smsNotificationRequest(smsRequest);

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


