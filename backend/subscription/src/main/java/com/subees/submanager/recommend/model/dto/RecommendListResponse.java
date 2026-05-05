package com.subees.submanager.recommend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RecommendListResponse {

    private int count;
    private List<RecommendListItemResponse> reports;
}
