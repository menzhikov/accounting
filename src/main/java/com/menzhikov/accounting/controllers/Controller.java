package com.menzhikov.accounting.controllers;

import com.menzhikov.accounting.services.ManagerService;
import com.menzhikov.accounting.controllers.dto.OrderDto;
import com.menzhikov.accounting.controllers.dto.TransferDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("accounts")
public class Controller {

    private final ManagerService managerService;

    @PostMapping("/increment")
    @ResponseBody
    public ResponseEntity<?> incrementAmount(@Validated @RequestBody OrderDto dto) {
        managerService.incrementAmount(dto.getId(), dto.getAmount());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/decrement")
    @ResponseBody
    public ResponseEntity<?> decrementAmount(@Validated @RequestBody OrderDto dto) {
        managerService.decrementAmount(dto.getId(), dto.getAmount());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfer")
    @ResponseBody
    public ResponseEntity<?> transferAmount(@Validated @RequestBody TransferDto dto) {
        managerService.transferAmount(dto.getFrom(), dto.getTo(), dto.getAmount());
        return ResponseEntity.ok().build();
    }
}
