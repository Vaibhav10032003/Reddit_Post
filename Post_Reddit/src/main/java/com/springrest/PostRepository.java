package com.springrest;

import com.springrest.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PostRepository extends MongoRepository<Post, String> {
    @Query("{'subreddit': ?0}")   // to add first parameter here subreddit in ?0 is used.
    List<Post> findAllBySubreddit(String subreddit);
}