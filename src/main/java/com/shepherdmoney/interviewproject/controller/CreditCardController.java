package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;


@RestController
public class CreditCardController {

    @Autowired
    @Qualifier("CreditCardRepo")
    private CreditCardRepository creditCardRepository;
    @Autowired
    @Qualifier("UserRepo")
    private UserRepository userRepository;

    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        User user = userRepository.findById(payload.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found with id " + payload.getUserId()));

        CreditCard creditCard = new CreditCard();
        creditCard.setIssuanceBank(payload.getCardIssuanceBank());
        creditCard.setNumber(payload.getCardNumber());
        creditCard.setUser(user);

        CreditCard savedCreditCard = creditCardRepository.save(creditCard);
        User savedUser = userRepository.save(user);
        //TODO: Check if saved?
        return ResponseEntity.ok(savedCreditCard.getId());
    }

    @GetMapping("/credit-card:all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id " + userId));

        List<CreditCardView> creditCardViews = user.getCreditCards().stream()
                .map(CreditCardView::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(creditCardViews);
    }

    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        CreditCard creditCard = creditCardRepository.findByNumber(creditCardNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credit card not found"));

        User user = userRepository.findById(creditCard.getUser().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User does not exist"));

        return ResponseEntity.ok(user.getId());
    }

    @PostMapping("/credit-card:update-balance")
    public ResponseEntity<Integer> postMethodName(@RequestBody UpdateBalancePayload[] payloads) {
        if (payloads.length == 0){
            return ResponseEntity.ok(0);
        }

        CreditCard creditCard = creditCardRepository.findByNumber(payloads[0].getCreditCardNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credit card not found"));

        for (UpdateBalancePayload payload : payloads){
            // get proper card if different
            if (!payload.getCreditCardNumber().equals(creditCard.getNumber())){
                creditCard = creditCardRepository.findByNumber(payload.getCreditCardNumber())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credit card not found"));
            }
            creditCard.update_balance(payload.getTransactionTime().truncatedTo(ChronoUnit.DAYS), payload.getCurrentBalance());
            creditCardRepository.save(creditCard);
        }
        return ResponseEntity.ok(0);
    }
}
