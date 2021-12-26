package com.menzhikov.accounting.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferDto {

    private Long from;
    private Long to;
    @Positive
    private int amount;
}
