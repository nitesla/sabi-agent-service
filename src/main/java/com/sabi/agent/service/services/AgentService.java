package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.agentDto.requestDto.AgentUpdateDto;
import com.sabi.agent.core.dto.agentDto.requestDto.CreateAgentRequestDto;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.requestDto.ValidateOTPRequest;
import com.sabi.agent.core.dto.responseDto.AgentUpdateResponseDto;
import com.sabi.agent.core.dto.responseDto.CreateAgentResponseDto;
import com.sabi.agent.core.models.agentModel.Agent;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.agentRepo.AgentCategoryRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentRepository;
import com.sabi.framework.exceptions.BadRequestException;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.models.User;
import com.sabi.framework.repositories.UserRepository;
import com.sabi.framework.utils.CustomResponseCode;
import com.sabi.framework.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Slf4j
@Service
public class AgentService {

    BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserRepository userRepository;
    private AgentRepository agentRepository;
    private AgentCategoryRepository agentCategoryRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;

    public AgentService(UserRepository userRepository,AgentRepository agentRepository,
                        AgentCategoryRepository agentCategoryRepository, ModelMapper mapper, ObjectMapper objectMapper,
                        Validations validations) {
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

        User user = mapper.map(request,User.class);
        User userExist = userRepository.findByEmailAndPhone(request.getEmail(),request.getPhone());
        if(userExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Agent user already exist");
        }
        String password = user.getPassword();
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setCreatedBy(0l);
        user.setIsActive(false);
        user = userRepository.save(user);
        log.debug("Create new agent user - {}"+ new Gson().toJson(user));
        Agent saveAgent = new Agent();
                saveAgent.setUserId(user.getId());
                saveAgent.setReferrer(Utility.guidID());
                saveAgent.setRegistrationToken(Utility.registrationCode());
                saveAgent.setRegistrationTokenExpiration(Utility.expiredTime());
                saveAgent.setIsActive(false);
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
     * Agent  update
     * </summary>
     * <remarks>this method is responsible for updating already existing Agent c</remarks>
     */

    public AgentUpdateResponseDto updateAgent(AgentUpdateDto request) {
        Agent agent = agentRepository.findByUserId(request.getUserId());
        if(agent ==null){
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, " Agent does not exist");
        }
        mapper.map(request, agent);
        agent.setUpdatedBy(0l);
        agentRepository.save(agent);
        log.debug("Agent record updated - {}"+ new Gson().toJson(agent));
        return mapper.map(agent, AgentUpdateResponseDto.class);
    }






    /** <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a country</remarks>
     */
    public void enableDisEnableState (EnableDisEnableDto request){
        Agent agent  = agentRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested agent Id does not exist!"));
        agent.setIsActive(request.getIsActive());
        agent.setUpdatedBy(0l);
        agentRepository.save(agent);

    }

}
