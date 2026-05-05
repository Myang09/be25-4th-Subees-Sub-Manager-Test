package com.subees.submanager.recommend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecommendDeleteResponse {

    private Long reportId;
    private boolean deleted;
}
