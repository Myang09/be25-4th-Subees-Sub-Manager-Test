package com.subees.submanager.community.model.dto.post;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityPostPageRequestDto {

    private int page;
    private int size;

    public CommunityPostPageRequestDto() {
        this.page = 1;
        this.size = 10;
    }

    public CommunityPostPageRequestDto(int page, int size) {
        this.page = (page < 1) ? 1 : page;
        this.size = size;
    }

    public int getOffset() {
        return (page - 1) * size;
    }
}