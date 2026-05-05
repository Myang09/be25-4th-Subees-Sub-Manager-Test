package com.subees.submanager.recommend.model.service;

import com.subees.submanager.recommend.model.dto.RecommendDeleteResponse;
import com.subees.submanager.recommend.model.dto.RecommendDetailResponse;
import com.subees.submanager.recommend.model.dto.RecommendGenerateRequest;
import com.subees.submanager.recommend.model.dto.RecommendGenerateResponse;
import com.subees.submanager.recommend.model.dto.RecommendListResponse;
import com.subees.submanager.recommend.model.dto.RecommendSaveRequest;
import com.subees.submanager.recommend.model.dto.RecommendSaveResponse;
import com.subees.submanager.recommend.model.dto.RecommendSubmitRequest;
import com.subees.submanager.recommend.model.dto.RecommendSubmitResponse;

public interface RecommendService {

    RecommendSubmitResponse submit(Long userId, RecommendSubmitRequest request);

    RecommendGenerateResponse generate(Long userId, RecommendGenerateRequest request);

    RecommendSaveResponse save(Long userId, RecommendSaveRequest request);

    RecommendListResponse getSavedReports(Long userId);

    RecommendDetailResponse getReportDetail(Long userId, Long reportId);

    RecommendDeleteResponse delete(Long userId, Long reportId);
}
