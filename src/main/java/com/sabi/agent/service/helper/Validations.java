package com.sabi.agent.service.helper;


import com.sabi.agent.core.dto.agentDto.requestDto.AgentCategoryDto;
import com.sabi.agent.core.dto.requestDto.*;
import com.sabi.agent.core.models.LGA;
import com.sabi.agent.core.models.State;
import com.sabi.agent.core.models.Ward;
import com.sabi.agent.service.repositories.LGARepository;
import com.sabi.agent.service.repositories.StateRepository;
import com.sabi.agent.service.repositories.WardRepository;
import com.sabi.framework.exceptions.BadRequestException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.models.User;
import com.sabi.framework.repositories.UserRepository;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@SuppressWarnings("All")
@Slf4j
@Service
public class Validations {

    private StateRepository stateRepository;
    private LGARepository lgaRepository;
    private UserRepository userRepository;
    private WardRepository wardRepository;



    public Validations(LGARepository lgaRepository,StateRepository stateRepository,WardRepository wardRepository) {
        this.lgaRepository = lgaRepository;
        this.stateRepository = stateRepository;
        this.wardRepository = wardRepository;
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
}
