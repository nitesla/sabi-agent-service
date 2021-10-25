package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.requestDto.MarketDto;
import com.sabi.agent.core.dto.responseDto.MarketResponseDto;
import com.sabi.agent.core.models.Market;
import com.sabi.agent.service.helper.GenericSpecification;
import com.sabi.agent.service.helper.SearchCriteria;
import com.sabi.agent.service.helper.SearchOperation;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.MarketRepository;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.models.User;
import com.sabi.framework.service.TokenService;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

//ken
@Slf4j
@Service
public class MarketService {

    private MarketRepository marketRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;


    public MarketService(MarketRepository marketRepository, ModelMapper mapper, ObjectMapper objectMapper, Validations validations) {
        this.marketRepository = marketRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
    }

    /**
     * <summary>
     * Market creation
     * </summary>
     * <remarks>this method is responsible for creation of new Market</remarks>
     */

    public MarketResponseDto createMarket(MarketDto request) {
        validations.validateMarket(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        Market market = mapper.map(request, Market.class);
        Market marketExist = marketRepository.findByName(request.getName());
        if (marketExist != null) {
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Market already exist");
        }
        market.setCreatedBy(userCurrent.getId());
        market.setIsActive(false);
        market = marketRepository.save(market);
        log.debug("Create new Market - {}" + new Gson().toJson(market));
        return mapper.map(market, MarketResponseDto.class);
    }

    public MarketDto updateMarket(MarketDto request) {
        validations.validateMarket(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        Market market = marketRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Country Id does not exist!"));
        mapper.map(request, market);
        boolean exists = marketRepository.exists(Example.of(Market.builder().wardId(market.getWardId()).name(market.getName()).build()));
        if (exists) {
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Market already exist");
        }
        market.setUpdatedBy(userCurrent.getId());
        marketRepository.save(market);
        log.debug("Country record updated - {}" + new Gson().toJson(market));
        return mapper.map(market, MarketDto.class);
    }

    /**
     * <summary>
     * Find Market
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */
    public MarketResponseDto findMarket(Long id) {
        Market market = marketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Market id does not exist!"));
        return mapper.map(market, MarketResponseDto.class);
    }

    /**
     * <summary>
     * Find all Markets
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */
    public Page<Market> findAll(String name, Boolean isActive, PageRequest pageRequest) {
        GenericSpecification<Market> genericSpecification = new GenericSpecification<Market>();

        if (name != null && !name.isEmpty()) {
            genericSpecification.add(new SearchCriteria("name", name, SearchOperation.MATCH));
        }
        if (isActive != null) {
            genericSpecification.add(new SearchCriteria("isActive", isActive, SearchOperation.EQUAL));
        }
        Page<Market> market = marketRepository.findAll(genericSpecification, pageRequest);

        return market;
    }

    /**
     * <summary>
     * Enable disenable
     * </summary>
     * <remarks>this method is responsible for enabling and dis enabling a market</remarks>
     */
    public void enableDisEnableState(EnableDisEnableDto request) {
        validations.validateStatus(request.getIsActive());
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        Market market = marketRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested market id does not exist!"));
        market.setIsActive(request.getIsActive());
        market.setUpdatedBy(userCurrent.getId());
        marketRepository.save(market);

    }

    /**
     * <summary>
     * Get all by status
     * </summary>
     * <remarks>this method returns a list of markets based on their active status</remarks>
     */
    public List<MarketResponseDto> getAllByStatus(Boolean isActive) {
        List<Market> markets = marketRepository.findByIsActive(isActive);
        return markets
                .stream()
                .map(user -> mapper.map(user, MarketResponseDto.class))
                .collect(Collectors.toList());
    }
}
