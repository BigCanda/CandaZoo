package com.newcoder.community;

import com.newcoder.community.dao.DiscussPostMapper;
import com.newcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.newcoder.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.lang.Nullable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = SpringbootApplication.class)
public class ElasticsearchTest {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Test
    public void testInsert() {
        try {
            discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
            discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
            discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
        } catch (Exception e) {

        }
    }

    @Test
    public void testInsertList() {
        try {
//            discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101,0,100));
//            discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102,0,100));
//            discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103,0,100));
//            discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111,0,100));
//            discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112,0,100));
            discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(0,0,discussPostMapper.selectDiscussPostRows(0)));
        } catch (Exception e) {

        }
    }

    @Test
    public void testDelete() {
        try {
            discussPostRepository.deleteAll();
        } catch (Exception e) {

        }
    }


    @Test
    public void testSearchByRepository() throws Exception{
//            NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
//                    // 内容标题同时匹配
//                    .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
//                    // 分别按照三个字段排序
//                    .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC))
//                    .withSorts(SortBuilders.fieldSort("score").order(SortOrder.DESC))
//                    .withSorts(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
//                    // 分页
//                    .withPageable(PageRequest.of(0, 10))
//                    //高亮显示
//                    .withHighlightFields(
//                            new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
//                            new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
//                    ).build();
//            Page<DiscussPost> page = elasticsearchTemplate.search(nativeSearchQuery, DiscussPost, "discusspost");
//            Sort sort = Sort.by(Sort.Direction.DESC,"type");
//            Pageable pageable = PageRequest.of(0,10, sort);
//            Page<DiscussPost> page = discussPostRepository.findDiscussPostByTitleOrContent("互联网寒冬", "互联网寒冬", pageable);
//            List<DiscussPost> list = page.getContent();
//            if (list != null) {
//                for (DiscussPost discussPost : list) {
//                    System.out.println(discussPost.toString());
//                }
//            }
    }

}
