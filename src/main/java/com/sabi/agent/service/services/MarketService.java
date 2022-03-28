package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.dto.requestDto.MarketDto;
import com.sabi.agent.core.dto.responseDto.MarketResponseDto;
import com.sabi.agent.core.models.*;
import com.sabi.agent.service.helper.GenericSpecification;
import com.sabi.agent.service.helper.SearchCriteria;
import com.sabi.agent.service.helper.SearchOperation;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.*;
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
import java.util.Optional;
import java.util.stream.Collectors;

//ken
@Slf4j
@Service
public class MarketService {

    private MarketRepository marketRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;
    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final LGARepository lgaRepository;
    private final WardRepository wardRepository;


    public MarketService(MarketRepository marketRepository, ModelMapper mapper, ObjectMapper objectMapper, Validations validations, CountryRepository countryRepository, StateRepository stateRepository, LGARepository lgaRepository, WardRepository wardRepository) {
        this.marketRepository = marketRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
        this.countryRepository = countryRepository;
        this.stateRepository = stateRepository;
        this.lgaRepository = lgaRepository;
        this.wardRepository = wardRepository;
    }

    /**
     * <summary>
     * Market creation
     * </summary>
     * <remarks>this method is responsible for creation of new Market</remarks>
     */

    public MarketResponseDto createMarket(MarketDto request) {
        List<Long> locationIds = validations.validateMarket(request);
        Market market = null;
        if (locationIds.size() == 4)
        {
            market = mapper.map(request, Market.class);
            market.setWardId(locationIds.get(0));
            market.setWard(wardRepository.findById(locationIds.get(0)).get().getName());
            market.setLgaId(locationIds.get(1));
            market.setStateId(locationIds.get(2));
            market.setCountryId(locationIds.get(3));
        }
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        Market marketExist = marketRepository.findByName(request.getName());
        if (marketExist != null) {
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Market already exist");
        }
        market.setCreatedBy(userCurrent.getId());
        market.setIsActive(true);
        market = marketRepository.save(market);
        log.debug("Create new Market - {}",market);
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
        MarketResponseDto marketResponseDto = mapper.map(market, MarketResponseDto.class);
        Market marketWithLgaInfo = marketRepository.findMarketAndLocationInfo(market.getId(),market.getWardId());
        log.info("marketWithLgaInfo=={}",marketWithLgaInfo);
        //setAndGetWardsLocationDetails(marketResponseDto);
        return marketResponseDto;

    }

    private MarketResponseDto setAndGetWardsLocationDetails(MarketResponseDto marketResponseDto) {
        Optional<Ward> ward = wardRepository.findById(marketResponseDto.getWardId());
        marketResponseDto.setWard(ward.isPresent()?ward.get().getName():null);
        Optional<LGA> lga = lgaRepository.findById(ward.get().getLgaId());
        if (lga.isPresent()){
            marketResponseDto.setLgaId(lga.get().getId());
            marketResponseDto.setLga(lga.get().getName());
        }
        Optional<State> state = stateRepository.findById(lga.get().getStateId());
        if (state.isPresent()){
            marketResponseDto.setStateId(state.get().getId());
            marketResponseDto.setState(state.get().getName());
        }
        Optional<Country> country = countryRepository.findById(state.get().getCountryId());
        if (country.isPresent()){
            marketResponseDto.setCountryId(country.get().getId());
            marketResponseDto.setCountry(country.get().getName());
        }
        return marketResponseDto;
    }

    /**
     * <summary>
     * Find all Markets
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */
    public Page<Market> findAll(String name,String wardName, String lgaName, String stateName,String countryName, Boolean isActive, PageRequest pageRequest) {
        GenericSpecification<Market> genericSpecification = new GenericSpecification<Market>();
        Ward ward = null;

        if (name != null && !name.isEmpty()) {
            genericSpecification.add(new SearchCriteria("name", name, SearchOperation.MATCH));
        }
        if (isActive != null) {
            genericSpecification.add(new SearchCriteria("isActive", isActive, SearchOperation.EQUAL));
        }
        if (wardName!=null && !wardName.isEmpty()){
            genericSpecification.add(new SearchCriteria("ward",wardName,SearchOperation.EQUAL));
        }
        if (lgaName!=null && !lgaName.isEmpty())
            genericSpecification.add(new SearchCriteria("lga", lgaName,SearchOperation.EQUAL));
        if (stateName!=null && !stateName.isEmpty())
            genericSpecification.add(new SearchCriteria("state",stateName,SearchOperation.EQUAL));
        if (countryName!=null && !countryName.isEmpty())
            genericSpecification.add(new SearchCriteria("country",countryName,SearchOperation.EQUAL));

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
        List<MarketResponseDto> marketResponseDtos= markets
                .stream()
                .map(user -> mapper.map(user, MarketResponseDto.class))
                .collect(Collectors.toList());

        //marketResponseDtos.forEach(this::setAndGetWardsLocationDetails);
        return marketResponseDtos;
    }
}
