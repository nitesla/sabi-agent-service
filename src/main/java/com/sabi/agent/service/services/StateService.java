package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.requestDto.StateDto;
import com.sabi.agent.core.dto.responseDto.StateResponseDto;
import com.sabi.agent.core.helpers.Validations;
import com.sabi.agent.core.models.State;
import com.sabi.agent.service.repositories.StateRepository;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Date;


/**
 *
 * This class is responsible for all business logic for state
 */


@Slf4j
@Service
public class StateService {



    private StateRepository stateRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;

    public StateService(StateRepository stateRepository, ModelMapper mapper, ObjectMapper objectMapper,Validations validations) {
        this.stateRepository = stateRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
    }

    /** <summary>
      * State creation
      * </summary>
      * <remarks>this method is responsible for creation of new states</remarks>
      */

    public StateResponseDto createState(StateDto request) {
        validations.validateState(request);
        State state = mapper.map(request,State.class);
        State stateExist = stateRepository.findByName(request.getName());
        if(stateExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " State already exist");
        }
        state.setCreatedBy(0l);
        state.setIsactive(true);
        state = stateRepository.save(state);
        log.debug("Create new State - {}", new Gson().toJson(state));
        return mapper.map(state, StateResponseDto.class);
    }


    /** <summary>
     * State update
     * </summary>
     * <remarks>this method is responsible for updating already existing states</remarks>
     */

    public StateResponseDto updateState(StateDto request) {
        validations.validateState(request);
        State state = stateRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested State Id does not exist!"));
        mapper.map(request, state);
        state.setUpdatedBy(0l);
        state.setUpdatedDate(new Date());
        stateRepository.save(state);
        log.debug("State record updated - {}", new Gson().toJson(state));
        return mapper.map(state, StateResponseDto.class);
    }


}
