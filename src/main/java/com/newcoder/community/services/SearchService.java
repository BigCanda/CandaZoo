package com.newcoder.community.services;
import com.newcoder.community.dao.DiscussPostMapper;
import com.newcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.newcoder.community.entity.DiscussPost;
import com.newcoder.community.entity.SearchResult;
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
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /*
    每次项目重新部署就把mysql里的帖子全部导入elasticsearch
     */
    @PostConstruct
    public void init() {
        discussPostRepository.deleteAll();
        System.out.println("自动删除所有帖子");
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(0,0,discussPostMapper.selectDiscussPostRows(0)));
        System.out.println("自动注入所有帖子");
    }

    public SearchResult search(String keyword,  Pageable pageable) {
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

        return new SearchResult(rows, posts);
    }

    public void saveDiscussPost(DiscussPost discussPost) {
         discussPostRepository.save(discussPost);
    }

    public void deleteDiscussPost(DiscussPost discussPost) {
        discussPostRepository.delete(discussPost);
    }

}
