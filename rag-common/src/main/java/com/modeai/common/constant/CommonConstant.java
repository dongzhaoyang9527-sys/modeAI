package com.modeai.common.constant;

public final class CommonConstant {
    private CommonConstant() {}

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String DEFAULT_AVATAR = "/default-avatar.png";

    public static final int ACCESS_LEVEL_PUBLIC = 0;
    public static final int ACCESS_LEVEL_DEPARTMENT = 1;
    public static final int ACCESS_LEVEL_CONFIDENTIAL = 2;
    public static final int ACCESS_LEVEL_SECRET = 3;

    public static final String DOC_STATUS_PENDING = "PENDING";
    public static final String DOC_STATUS_PROCESSING = "PROCESSING";
    public static final String DOC_STATUS_COMPLETED = "COMPLETED";
    public static final String DOC_STATUS_FAILED = "FAILED";
}
