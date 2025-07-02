package com.lfx.demo.entity;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ArticleStatus {
    PENDING(0),    // 未审核
    REVIEWING(1),  // 审核中
    PUBLISHED(2),  // 已发布
    REJECTED(3);   // 已驳回

    @EnumValue
    private final int code;

    ArticleStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @JsonValue
    public String toJson() {
        return this.name();
    }
} 