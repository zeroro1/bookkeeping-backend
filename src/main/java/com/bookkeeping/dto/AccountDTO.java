package com.bookkeeping.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AccountDTO {
    private Long id;

    @NotNull(message = "类型不能为空")
    private Integer type;

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    private String fromAccount;

    private String toAccount;

    private String category;

    private String remark;

    private LocalDate date;
}