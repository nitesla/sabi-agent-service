package com.sabi.agent.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sabi.agent.core.dto.requestDto.WishListDto;
import com.sabi.agent.core.dto.responseDto.WishListResponseDto;
import com.sabi.agent.core.models.WishList;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.WishListRepository;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.models.User;
import com.sabi.framework.service.TokenService;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@SuppressWarnings("ALL")
@Slf4j
@Service
public class WishListService {

    private WishListRepository wishListRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;
    private final Validations validations;

    public WishListService(WishListRepository wishListRepository, ModelMapper mapper, ObjectMapper objectMapper, Validations validations) {
        this.wishListRepository = wishListRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.validations = validations;
    }

    /** <summary>
     * State creation
     * </summary>
     * <remarks>this method is responsible for creation of new wish list</remarks>
     */

    public WishListResponseDto createWishList(WishListDto request) {
        validations.validateWishList(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        WishList wishList = mapper.map(request,WishList.class);
        WishList wishListExist = wishListRepository.findWishListByProductName(request.getProductName());
        if(wishListExist !=null){
            throw new ConflictException(CustomResponseCode.CONFLICT_EXCEPTION, " Wish lIst already exist");
        }
        wishList.setCreatedBy(userCurrent.getId());
        wishList.setIsActive(true);
        wishList = wishListRepository.save(wishList);
        log.debug("Create new wish list - {}"+ new Gson().toJson(wishList));
        return mapper.map(wishList, WishListResponseDto.class);
    }


    /** <summary>
     * State update
     * </summary>
     * <remarks>this method is responsible for updating already existing wish list</remarks>
     */

    public WishListResponseDto updateWishList(WishListDto request) {
        validations.validateWishList(request);
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        WishList wishList = wishListRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested wish list Id does not exist!"));
        mapper.map(request, wishList);
        wishList.setUpdatedBy(userCurrent.getId());
        wishListRepository.save(wishList);
        log.debug("wish list record updated - {}" + new Gson().toJson(wishList));
        return mapper.map(wishList, WishListResponseDto.class);
    }


    /** <summary>
     * Find State
     * </summary>
     * <remarks>this method is responsible for getting a single record</remarks>
     */
    public WishListResponseDto findWishList(Long id){
        WishList state = wishListRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested wish list Id does not exist!"));
        return mapper.map(state,WishListResponseDto.class);
    }


    /** <summary>
     * Find all State
     * </summary>
     * <remarks>this method is responsible for getting all records in pagination</remarks>
     */
    public Page<WishList> findAll(String agentId,String productId,String productName,String picture, PageRequest pageRequest ){
        Page<WishList> state = wishListRepository.findWishList(agentId,productId,productName,picture,pageRequest);
        if(state == null){
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        return state;

    }

    public WishListResponseDto deleteWishList(Long id){
        WishList state = wishListRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested wish list Id does not exist!"));
        wishListRepository.deleteById(id);
        return mapper.map(state,WishListResponseDto.class);
    }

//    @Param("agentId")String agentId,
//    @Param("productId")String productId,
//    @Param("productName")String productName,
//    @Param("picture")String picture, Pageable pageable


//    /** <summary>
//     * Enable disenable
//     * </summary>
//     * <remarks>this method is responsible for enabling and dis enabling a state</remarks>
//     */
//    public void enableDisEnableState (EnableDisEnableDto request){
//        validations.validateStatus(request.getIsActive());
//        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
//        State state = stateRepository.findById(request.getId())
//                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
//                        "Requested State Id does not exist!"));
//        state.setIsActive(request.getIsActive());
//        state.setUpdatedBy(userCurrent.getId());
//        stateRepository.save(state);
//
//    }


//    public List<State> getAll(Boolean isActive){
//        List<State> states = stateRepository.findByIsActive(isActive);
//        return states;
//
//    }
}
