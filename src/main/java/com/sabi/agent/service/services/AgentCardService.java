package com.sabi.agent.service.services;

import com.sabi.agent.core.dto.requestDto.CardTokenizationRequest;
import com.sabi.agent.core.models.agentModel.AgentCard;
import com.sabi.agent.service.repositories.agentRepo.AgentCardRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentRepository;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.exceptions.ProcessingException;
import com.sabi.framework.integrations.payment_integration.models.response.TokenisationResponse;
import com.sabi.framework.service.PaymentService;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j

public class AgentCardService {
    private final PaymentService paymentService;
    private final AgentRepository agentRepository;

    @Autowired
    private AgentCardRepository repository;

    public AgentCardService(PaymentService paymentService, AgentRepository agentRepository) {
        this.paymentService = paymentService;
        this.agentRepository = agentRepository;
    }

    public AgentCard saveCard(CardTokenizationRequest cardTokenizationRequest) {
        agentRepository.findById(cardTokenizationRequest.getAgentId()).orElseThrow(() ->
                new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, "Agent not found"));
        log.info("Attempting tokenization request");
        TokenisationResponse tokenise = paymentService.tokenise(cardTokenizationRequest.getTokenisationRequest());
        log.info(tokenise.toString());
        if (!tokenise.getStatus().equals("Success"))
            throw new ProcessingException("Error saving card, Status: " + tokenise.getStatus());

        AgentCard card = new AgentCard();
        card.setAgentId(card.getAgentId());
        card.setCardToken(tokenise.getData().getCard().getToken());
        card.setBin(tokenise.getData().getCard().getBin());
        card.setLast4(tokenise.getData().getCard().getLast4());
        card.setIsActive(true);
        card.setIsDefault(false);
        log.info("Saving agent card: " + card);
        AgentCard savedCard = repository.save(card);
        log.info("Card save successfully: " + savedCard);
        return savedCard;
    }

    public AgentCard chooseDefaultCard(long agentId, long cardId) {
        AgentCard exists = repository.findByIdAndAgentId(cardId, agentId);
        if (exists == null)
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, "Agent with Card Id not found");
        List<AgentCard> agentCards = repository.findByAgentId(agentId);
        AtomicReference<AgentCard> returnValue = new AtomicReference<>();
        agentCards.forEach((agentCard) -> {
            if(agentCard.getId() == cardId) {
                agentCard.setIsDefault(true);
                returnValue.set(agentCard);
            }
            agentCard.setIsDefault(false);
        });
        repository.saveAll(agentCards);
        return returnValue.get();
    }

    public List<AgentCard> getCards(Long agentId){
        List<AgentCard> byAgentId = repository.findByAgentId(agentId);
        return byAgentId;
    }

    public Optional<AgentCard> getCard(Long cardId){
        return repository.findById(cardId);
    }
}
