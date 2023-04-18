package com.newcoder.community.dao.elasticsearch;

import com.newcoder.community.entity.User;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ElasticsearchRepository<User, Integer> {
}
