package com.newcoder.community.services;
import com.newcoder.community.dao.DiscussPostMapper;
import com.newcoder.community.dao.UserMapper;
import com.newcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.newcoder.community.dao.elasticsearch.UserRepository;
import com.newcoder.community.entity.DiscussPost;
import com.newcoder.community.entity.SearchPostResult;
import com.newcoder.community.entity.SearchUserResult;
import com.newcoder.community.entity.User;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SearchService {
    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /*
    每次项目重新部署就把mysql里的帖子全部导入elasticsearch
     */
    @PostConstruct
    public void init() {
        discussPostRepository.deleteAll();
        userRepository.deleteAll();
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(0,0,discussPostMapper.selectDiscussPostRows(0)));
        userRepository.saveAll(userMapper.selectUsers(0, userMapper.selectUserCount()));
    }

    public SearchPostResult searchDiscussPost(String keyword, Pageable pageable) {
        List<DiscussPost> posts = new ArrayList<>();

        // 构建一个NativeSearchQuery并添加分页条件、排序条件以及高光区域
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withPageable(pageable)
                .withSorts(
                        SortBuilders.fieldSort("type").order(SortOrder.DESC),
                        SortBuilders.fieldSort("score").order(SortOrder.DESC),
                        SortBuilders.fieldSort("createTime").order(SortOrder.DESC)
                )
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                )
                .build();
        // 调用ElasticsearchRestTemplate的search()方法进行查询
        // 使用SearchHits存储搜索结果
        SearchHits<DiscussPost> searchHits = elasticsearchRestTemplate.search(query, DiscussPost.class);
        long rows = searchHits.getTotalHits();

        // 遍历搜索结果设置帖子的各个参数
        if (searchHits.getTotalHits() != 0) {
            for (SearchHit<DiscussPost> searchHit : searchHits) {
                DiscussPost post = new DiscussPost();

                int id = searchHit.getContent().getId();
                post.setId(id);

                int userId = searchHit.getContent().getUserId();
                post.setUserId(userId);

                String title = searchHit.getContent().getTitle();
                post.setTitle(title);

                String content = searchHit.getContent().getContent();
                post.setContent(content);

                int status = searchHit.getContent().getStatus();
                post.setStatus(status);

                int type = searchHit.getContent().getType();
                post.setType(type);

                Date createTime = searchHit.getContent().getCreateTime();
                post.setCreateTime(createTime);

                int commentCount = searchHit.getContent().getCommentCount();
                post.setCommentCount(commentCount);

                // 获得刚刚构建的高光区域，填到帖子的内容和标题上
                List<String> contentField = searchHit.getHighlightFields().get("content");
                if (contentField != null) {
                    post.setContent(contentField.get(0));
                }

                List<String> titleField = searchHit.getHighlightFields().get("title");
                if (titleField != null) {
                    post.setTitle(titleField.get(0));
                }
                posts.add(post);
            }
        }

        return new SearchPostResult(rows, posts);
    }

    public SearchUserResult searchUser(String keyword, Pageable pageable) {
        List<User> users = new ArrayList<>();

        // 构建一个NativeSearchQuery并添加分页条件、排序条件以及高光区域
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "username"))
                .withPageable(pageable)
                .withSort(
                        SortBuilders.fieldSort("createTime").order(SortOrder.DESC)
                )
                .withHighlightFields(
                        new HighlightBuilder.Field("username").preTags("<em>").postTags("</em>")
                )
                .build();
        // 调用ElasticsearchRestTemplate的search()方法进行查询
        // 使用SearchHits存储搜索结果
        SearchHits<User> searchHits = elasticsearchRestTemplate.search(query, User.class);
        long rows = searchHits.getTotalHits();

        // 遍历搜索结果设置帖子的各个参数
        if (searchHits.getTotalHits() != 0) {
            for (SearchHit<User> searchHit : searchHits) {
                User user = new User();

                int id = searchHit.getContent().getId();
                user.setId(id);

                String username = searchHit.getContent().getUsername();
                user.setUsername(username);

                String headerUrl = searchHit.getContent().getHeaderUrl();
                user.setHeaderUrl(headerUrl);

                Date createTime = searchHit.getContent().getCreateTime();
                user.setCreateTime(createTime);

                // 获得刚刚构建的高光区域，填到用户名上
                List<String> contentField = searchHit.getHighlightFields().get("username");
                if (contentField != null) {
                    user.setUsername(contentField.get(0));
                }

                users.add(user);
            }
        }

        return new SearchUserResult(rows, users);
    }

    public void saveDiscussPost(DiscussPost discussPost) {
         discussPostRepository.save(discussPost);
    }

    public void deleteDiscussPost(DiscussPost discussPost) {
        discussPostRepository.delete(discussPost);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }

}
