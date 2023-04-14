package com.newcoder.community.util;


// 23.3.8
// 验证状态码

public interface CommunityConstant {

    // 激活状态码
    int ACTIVATION_SUCCESS = 0;
    int ACTIVATION_REPEAT = 1;

    int ACTIVATION_FAILURE = 2;

    // 默认登录凭证超时时间

    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 7;

    // 实体类型:帖子=1,评论=2,人是3
    int ENTITY_TYPE_POST = 1;
    int ENTITY_TYPE_COMMENT = 2;
    int ENTITY_TYPE_USER = 3;
    // 私信方向
    int RECEIVE = 0;
    int SEND = 1;

    // 私信的主题

    String TOPIC_COMMENT = "comment";
    String TOPIC_LIKE = "like";
    String TOPIC_FOLLOW = "follow";
    String TOPIC_PUBLISH = "publish";
    String TOPIC_DELETE = "delete";

    String TOPIC_PUSH = "push";
    String TOPIC_TOP = "top";
    String TOPIC_WONDERFUL = "wonderful";
    // 系统用户ID
    int SYSTEM_USER_ID = 1;
    // 置顶类型
    int TOP_POST_TYPE = 1;
    // 普通类型
    int NORMAL_POST_TYPE = 0;

    // 普通状态
    int NORMAL_POST_STATUS = 0;
    // 加精状态
    int WONDERFUL_POST_STATUS = 1;
    // 删除状态
    int DELETED_POST_STATUS = 2;

    String AUTHORITY_USER = "user";
    String AUTHORITY_ADMIN = "admin";
    String AUTHORITY_MODERATOR = "moderator";
}
