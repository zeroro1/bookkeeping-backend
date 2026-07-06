package com.bookkeeping.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("account")
public class Account {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 类型: 1收入 2支出 3转账 */
    private Integer type;

    private BigDecimal amount;

    private String fromAccount;

    private String toAccount;

    private String category;

    private String remark;

    private LocalDate date;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}