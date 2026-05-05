package com.subees.submanager.common.model.dto;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@ToString
public class ItemsResponseDto<T> extends BaseResponseDto<List<T>> {
    private final int page;
    private final int numOfRows;
    private final int totalCount;

    public ItemsResponseDto(HttpStatus httpStatus, List<T> items, int page, int totalCount) {
        super(httpStatus, items);
        this.page = page;
        this.numOfRows = items.size();
        this.totalCount = totalCount;
    }

    public ItemsResponseDto(HttpStatus httpStatus, String message, List<T> items, int page, int totalCount) {
        super(httpStatus, message, items);
        this.page = page;
        this.numOfRows = items.size();
        this.totalCount = totalCount;
    }
}