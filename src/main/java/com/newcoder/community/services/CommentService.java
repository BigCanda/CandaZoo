package com.newcoder.community.services;

import com.newcoder.community.dao.CommentMapper;
import com.newcoder.community.entity.Comment;
import com.newcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

import static com.newcoder.community.util.CommunityConstant.ENTITY_TYPE_POST;

@Service
public class CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Autowired
    private DiscussPostService discussPostService;
    public List<Comment> findCommentByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }
    // 设置隔离级别READ_COMMITTED读已提交,兼顾性能和安全性
    // 读数据时只能读到已经提交了的数据 （没有脏读）
    // 写数据时只能覆盖已经提交了的数据 （没有脏写）
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if(comment == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        if (comment.getContent().isEmpty())
        {
            return -1;
        }
        // 添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        // 更新贴子评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountByEntity(comment.getEntityType(),comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }

        return rows;
    }

    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }
}
