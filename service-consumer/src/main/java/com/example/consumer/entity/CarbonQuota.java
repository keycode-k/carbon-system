package com.example.consumer.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CarbonQuota {

    private Long id;
    private Long userId;
    private Integer year;
    private BigDecimal totalQuota;
    private BigDecimal verifiedEmission;
    private Integer status; // 0-Unfulfilled, 1-Fulfilled
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public BigDecimal getTotalQuota() { return totalQuota; }
    public void setTotalQuota(BigDecimal totalQuota) { this.totalQuota = totalQuota; }

    public BigDecimal getVerifiedEmission() { return verifiedEmission; }
    public void setVerifiedEmission(BigDecimal verifiedEmission) { this.verifiedEmission = verifiedEmission; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
}
