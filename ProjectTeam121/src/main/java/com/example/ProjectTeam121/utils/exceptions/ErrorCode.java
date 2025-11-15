package com.example.ProjectTeam121.utils.exceptions;


import org.springframework.http.HttpStatus;

public enum ErrorCode {
    RESOURCE_NOT_FOUND(1001, "error.resource_not_found", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED(1002, "error.access_denied", HttpStatus.FORBIDDEN),
    ID_NOT_FOUND(1003, "error.id_not_found", HttpStatus.NOT_FOUND),
    GROUP_CODE_EXISTS(1004, "error.group_code_exists", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE(1005, "error.invalid_date_range", HttpStatus.BAD_REQUEST),
    ACTIVITY_ASSIGNMENT_DUPLICATE(1006, "error.activity_assignment_duplicate", HttpStatus.BAD_REQUEST),
    INVALID_INPUT(1007, "error.invalid_input", HttpStatus.BAD_REQUEST),
    ACTION_NAME_EXIST(1008, "error.action_name_exist", HttpStatus.BAD_REQUEST),
    INVALID_EFFECTIVE_DATE(1009, "error.invalid_effective_date", HttpStatus.BAD_REQUEST),
    INVALID_EXPIRE_DATE(1010, "error.invalid_expire_date", HttpStatus.BAD_REQUEST),
    INVALID_ACTIVITY_ASSIGNMENT(1011, "error.invalid_activity_assignment", HttpStatus.BAD_REQUEST),
    RISK_ASSESSMENT_PENDING_APPROVAL(1012, "error.risk_assessment_pending_approval", HttpStatus.BAD_REQUEST),
    INVALID_START_DATE(1013, "error.invalid_start_date", HttpStatus.BAD_REQUEST),
    ASSIGNMENT_NOT_OPEN(1014, "error.assignment_not_open", HttpStatus.BAD_REQUEST),
    SYNC_DATA_PROCESSING(1015, "error.sync_data_processing", HttpStatus.BAD_REQUEST),
    OPINION_ROUND_NOT_ALLOWED(1016, "error.opinion_round_not_allowed", HttpStatus.BAD_REQUEST),
    UNIT_ALREADY_APPROVED(1017, "error.unit_already_approved", HttpStatus.BAD_REQUEST),
    DESCRIPTION_REQUIRED(1018, "error.description_required", HttpStatus.BAD_REQUEST),
    ACTIVITY_MISMATCH(1019, "error.activity_mismatch", HttpStatus.BAD_REQUEST),
    FEEDBACK_ALREADY_APPROVED(1020, "error.feedback_already_approved", HttpStatus.BAD_REQUEST),
    CONCURRENT_UPDATE(1021, "error.concurrent_update", HttpStatus.CONFLICT),
    EVALUATION_CRITERIA_VALUE_NOT_FOUND(1022, "error.evaluation_criteria_value_not_found", HttpStatus.BAD_REQUEST),
    RISK_MATRIX_NOT_FOUND(1023, "error.risk_matrix_not_found", HttpStatus.BAD_REQUEST),
    CONTROL_MEASURE_NOT_FOUND(1024, "error.control_measure_not_found", HttpStatus.BAD_REQUEST),
    INVALID_STATE(1025, "error.invalid_state", HttpStatus.BAD_REQUEST),
    CONTROL_MATRIX_NOT_FOUND(1026, "error.control_matrix_not_found", HttpStatus.BAD_REQUEST),
    DUPLICATE_VALUE(1027, "error.duplicate_value", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1028, "error.role_not_found", HttpStatus.NOT_FOUND),
    // Lỗi chung cho IoT
    OWNERSHIP_VIOLATION(1029, "error.ownership_violation", HttpStatus.FORBIDDEN),
    // Lỗi cho Location
    LOCATION_NOT_FOUND(1030, "error.location_not_found", HttpStatus.NOT_FOUND),
    // Lỗi cho DeviceType
    DEVICE_TYPE_NOT_FOUND(1031, "error.device_type_not_found", HttpStatus.NOT_FOUND),
    DEVICE_TYPE_NAME_EXISTS(1032, "error.device_type_name_exists", HttpStatus.BAD_REQUEST),
    // Lỗi cho Property
    PROPERTY_NOT_FOUND(1033, "error.property_not_found", HttpStatus.NOT_FOUND),
    PROPERTY_NAME_EXISTS(1034, "error.property_name_exists", HttpStatus.BAD_REQUEST),
    // Lỗi cho Device
    DEVICE_NOT_FOUND(1035, "error.device_not_found", HttpStatus.NOT_FOUND),
    DEVICE_IDENTIFIER_EXISTS(1036, "error.device_identifier_exists", HttpStatus.BAD_REQUEST),
    // Lỗi cho Sensor
    SENSOR_NOT_FOUND(1037, "error.sensor_not_found", HttpStatus.NOT_FOUND),

    COMMENTING_BLOCKED(1038, "error.commenting_blocked", HttpStatus.FORBIDDEN),

    COMMENT_NOT_FOUND(1039, "error.comment_not_found", HttpStatus.NOT_FOUND);


    private final int code;
    private final String messageKey;
    private final HttpStatus httpStatus;
    private final String domain;

    ErrorCode(int code, String messageKey, HttpStatus httpStatus) {
        this.code = code;
        this.messageKey = messageKey;
        this.httpStatus = httpStatus;
        this.domain = "project-team121"; // đổi domain theo dự án bạn
    }

    public int getCode() { return code; }
    public String getMessageKey() { return messageKey; }
    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getDomain() { return domain; }
    public String getFullCode() { return domain + "." + code; }
}