package com.subees.submanager.recommend.model.mapper;

import com.subees.submanager.recommend.model.dto.PopularSubscriptionStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecommendStatusMapper {

    List<PopularSubscriptionStat> findTopPopularSubscriptions(@Param("limit") int limit);
}
