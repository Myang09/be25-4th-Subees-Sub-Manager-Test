package com.subees.submanager.subscription.model.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryResponse {
    private Long categoryId;
    private String categoryName;
}