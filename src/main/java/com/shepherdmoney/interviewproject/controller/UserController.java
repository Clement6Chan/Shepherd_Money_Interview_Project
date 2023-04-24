package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class UserController {

    @Autowired
    @Qualifier("UserRepo")
    private UserRepository userRepository;

    @PutMapping("/user")
    public ResponseEntity<Integer> createUser(@RequestBody CreateUserPayload payload) {
        //check if email exists
        Optional<User> user_same_email = userRepository.findByEmail(payload.getEmail());
        if (user_same_email.isPresent()){
            return ResponseEntity.badRequest().body(0);
        }

        User new_user = new User();
        new_user.setName(payload.getName());
        new_user.setEmail(payload.getEmail());
        User user = userRepository.save(new_user);
        return ResponseEntity.ok(user.getId());
    }

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestParam int userId) {
        Optional<User> user_container = userRepository.findById(userId);
        if (user_container.isEmpty()){
            return ResponseEntity.badRequest().body(String.format("User %d not found",userId));
        }
        userRepository.deleteById(userId);
        //TODO: check for deletion?
        return ResponseEntity.ok().body(String.format("User %d deleted",userId));
    }
}
