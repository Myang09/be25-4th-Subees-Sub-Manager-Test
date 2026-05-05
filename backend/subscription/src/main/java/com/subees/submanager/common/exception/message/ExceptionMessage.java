package com.subees.submanager.common.exception.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionMessage {
    USER_NOT_FOUND("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_EMAIL("이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    DUPLICATE_NICKNAME("이미 사용 중인 닉네임입니다.", HttpStatus.CONFLICT),
    INVALID_EMAIL("가입되지 않은 이메일입니다.", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_CURRENT_PASSWORD("현재 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    SAME_AS_OLD_PASSWORD("새 비밀번호는 현재 비밀번호와 달라야 합니다.", HttpStatus.BAD_REQUEST),
    WITHDRAWN_USER("탈퇴한 계정입니다.", HttpStatus.BAD_REQUEST),
    INACTIVE_USER("비활성화된 계정입니다.", HttpStatus.BAD_REQUEST),
    ALREADY_WITHDRAWN_USER("이미 탈퇴 처리된 계정입니다.", HttpStatus.BAD_REQUEST),
    EMPTY_UPLOAD_FILE("업로드할 파일이 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_IMAGE_FILE("이미지 파일만 업로드할 수 있습니다.", HttpStatus.BAD_REQUEST),
    INVALID_FILE_EXTENSION("허용되지 않는 파일 확장자입니다.", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED("파일 저장 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    SUBSCRIPTION_NOT_FOUND("구독 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CARD_NOT_FOUND("등록된 카드 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CARD_TYPE_CHANGE_NOT_ALLOWED("카드 등록 방식은 수정할 수 없습니다.", HttpStatus.BAD_REQUEST),
    POST_NOT_FOUND("해당 게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_PAGE("유효하지 않은 페이지입니다.", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST("유효하지 않은 요청입니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    ALREADY_SCRAPPED("이미 스크랩한 게시글입니다.", HttpStatus.CONFLICT),
    INVALID_SCRAP_REQUEST("자신이 작성한 게시글은 스크랩할 수 없습니다.", HttpStatus.BAD_REQUEST),
    SCRAP_NOT_FOUND("스크랩 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CARD_INPUT_INVALID("카드 선택 또는 직접 입력 중 하나만 입력해야 합니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_CARD("이미 등록된 카드입니다.", HttpStatus.CONFLICT),
    CARD_ALREADY_DELETED("이미 삭제된 카드입니다.", HttpStatus.CONFLICT),
    PAYMENT_METHOD_NOT_FOUND("해당 결제수단이 존재하지 않거나 수정 권한이 없습니다.", HttpStatus.NOT_FOUND),
    CARD_IN_USE("현재 사용 중인 카드는 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST),
    CARD_ACCESS_DENIED("해당 카드에 대한 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    CARD_NAME_REQUIRED("카드 별칭은 필수입니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_ID_REQUIRED("결제수단 아이디는 필수입니다.", HttpStatus.BAD_REQUEST),
    CARD_CREATE_FAILED("카드 등록에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    CARD_UPDATE_FAILED("카드 수정에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    CARD_DELETE_FAILED("카드 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    CARD_DELETE_NOT_FOUND("삭제할 카드가 없거나 이미 비활성화된 카드입니다.", HttpStatus.NOT_FOUND),
    NOTIFICATION_NOT_FOUND("존재하지 않거나 처리할 수 없는 알림입니다.", HttpStatus.NOT_FOUND),
    NOTIFICATION_UPDATE_FAILED("알림 상태 변경에 실패했습니다.", HttpStatus.CONFLICT),
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DUPLICATE_CARD_NAME("이미 수정된 카드입니다.", HttpStatus.CONFLICT),
    RECOMMEND_ACCESS_DENIED("해당 추천 리포트에 대한 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NO_PROFILE_CHANGES("변경된 프로필 정보가 없습니다.", HttpStatus.BAD_REQUEST),

    // 캘린더
    INVALID_MONTH("월은 1부터 12 사이여야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_YEAR("유효한 년도를 입력해주세요.", HttpStatus.BAD_REQUEST),
    INVALID_DATE("해당 월에 존재하는 날짜를 입력해주세요.", HttpStatus.BAD_REQUEST),
    RANGE_REQUIRED("조회 범위는 필수입니다.",HttpStatus.BAD_REQUEST ),
    INVALID_RANGE_TYPE("조회 범위는 MONTH 또는 YEAR만 가능합니다.",HttpStatus.BAD_REQUEST ),
    MONTH_REQUIRED("월간 조회 시 month는 필수입니다.", HttpStatus.BAD_REQUEST),




    //구독
    SUBSCRIPTION_DUPLICATE("이미 등록된 구독 항목입니다.", HttpStatus.CONFLICT),
    CATEGORY_NOT_FOUND("존재하지 않는 카테고리입니다.", HttpStatus.NOT_FOUND),
    ITEM_NOT_FOUND("존재하지 않는 구독 항목입니다.", HttpStatus.NOT_FOUND),
    ITEM_CATEGORY_MISMATCH("선택한 카테고리에 해당하지 않는 구독 항목입니다.", HttpStatus.BAD_REQUEST),
    INVALID_BILLING_CYCLE("올바르지 않은 결제 주기입니다.", HttpStatus.BAD_REQUEST),
    INVALID_START_DATE("시작일은 오늘 이후일 수 없습니다.", HttpStatus.BAD_REQUEST),
    NO_CHANGES_DETECTED("수정된 내용이 없습니다.", HttpStatus.BAD_REQUEST),
    SUBSCRIPTION_ACCESS_DENIED("해당 구독에 대한 접근 권한이 없습니다.", HttpStatus.FORBIDDEN);
    private final String message;
    private final HttpStatus httpStatus;
}
