package com.example.consumer.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CarbonQuotaDetail {
    private Long id;
    private Long quotaId;
    private String type;
    private BigDecimal amount;
    private BigDecimal balance;
    private String remark;
    private LocalDateTime changeDate;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuotaId() { return quotaId; }
    public void setQuotaId(Long quotaId) { this.quotaId = quotaId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getChangeDate() { return changeDate; }
    public void setChangeDate(LocalDateTime changeDate) { this.changeDate = changeDate; }
}
