package com.springrest.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springrest.PostRepository;
import com.springrest.model.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.bson.Document;
import javax.swing.text.html.parser.Entity;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.*;

@Service
public class RedditService {

    private final MongoTemplate mongoTemplate;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    public RedditService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<String> fetchAllSubreddits() {
        return mongoTemplate.query(Post.class).distinct("subreddit").as(String.class).all();
    }

    public String getAuthToken() {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        RestTemplate restTemplate  = builder
                .basicAuthentication("CHI5wEEbhOMZ0fcw16mnxw","9gCQ4sCxj6Cg4SP006A-TvUN0PUocQ")
                .defaultHeader("Content-Type", "application/x-www-form-urlencoded")
                .defaultHeader("User-Agent", "somerandomuser")
                .build();
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", "vaibhav_3386");
        body.add("password", "Vaibhav@1003");
        String authUrl = "https://www.reddit.com/api/v1/access_token";
        ResponseEntity<String> response = restTemplate.postForEntity(authUrl, body, String.class);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<>();
        try {
            map.putAll(mapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {}));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(response.getBody());
        return String.valueOf(map.get("access_token"));
    }
    public List<Post> sortData() {
        Query query = new Query().with(Sort.by(Sort.Direction.ASC, "created"));
        return mongoTemplate.find(query, Post.class);
    }
    public List<Post> findByWord(String word){
        Query query = new Query();
        query.addCriteria(Criteria.where("title").regex(word, "i"));
        return mongoTemplate.find(query, Post.class);
    }
    public List<Post> deleteBysubreddit(String subreddit){
        Query query = new Query();
        query.addCriteria(Criteria.where("subreddit").is(subreddit));
        List<Post> deletedRepositories = mongoTemplate.findAllAndRemove(query, Post.class);
        mongoTemplate.remove(query, Post.class);
        return deletedRepositories;
    }
    public String  postArticle(String subreddit,String title,String body,String authtoken){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        String authToken = getAuthToken();
        System.out.println(authtoken);
        headers.setBearerAuth(authToken);
        headers.put("User-Agent", Collections.singletonList("tomcat:com.e4developer.e4reddit-test:v1.0 (by /u/vaibhav_3386)"));
        String url = "https://oauth.reddit.com/api/submit";
        MultiValueMap <String,String> myMap = new LinkedMultiValueMap<>();
        myMap.add("kind","self");
        myMap.add("sr",subreddit);
        myMap.add("text",body);
        myMap.add("title",title);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(myMap, headers);
        Object response1 = restTemplate.postForObject(url,entity , Object.class);
        System.out.println(response1);
        return "Post is created with subreddit = " + subreddit + ", title = " + title + " and text = " + body;
    }

    public List<Post> readArticles(String subReddit) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        String authToken = getAuthToken();
        headers.setBearerAuth(authToken);
        headers.put("User-Agent", Collections.singletonList("tomcat:com.e4developer.e4reddit-test:v1.0 (by /u/vaibhav_3386)"));
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        String url = "https://oauth.reddit.com/r/" + subReddit + "/hot";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String responseBody = response.getBody();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(responseBody);
        JsonNode childrenNode = jsonNode.get("data").get("children");
        List<String> titles= new ArrayList<>();
        List<String> subreddits= new ArrayList<>();
        List<Double> createdTimestamps = new ArrayList<>();
        for (JsonNode child : childrenNode) {
            JsonNode dataNode = child.get("data");
            String title= dataNode.get("title").asText();
            String subreddit = dataNode.get("subreddit").asText();
            Double created = dataNode.get("created").asDouble();
            titles.add(title);
            subreddits.add(subreddit);
            createdTimestamps.add(created);
        }
        List<Document> documents = new ArrayList<>();
        List<Post> posts = new ArrayList<>();

        // Print the extracted values
        for (int i = 0; i < titles.size(); i++) {
            System.out.println("title: " + titles.get(i));
            System.out.println("subreddit: " + subreddits.get(i));
            System.out.println("Created: " + createdTimestamps.get(i));
            Post post = new Post(subreddits.get(i), titles.get(i), createdTimestamps.get(i));
            posts.add(post);
        }
        ArrayList<Post> result = new ArrayList<>();
        boolean flag = false;
        for (Post post:posts){
            Query query = new Query();
            query.addCriteria(Criteria.where("subreddit").is(post.getsubreddit())
                    .and("title").is(post.getTitle())
                    .and("created").is(post.getCreated()));
            Post fetchedPost  =  mongoTemplate.findOne(query, Post.class, "postCollection");
            if (fetchedPost == null)  {
                postRepository.insert(post);
                result.add(post);
            }  else  {
                List<Post> rest = postRepository.findAllBySubreddit(subReddit);
                result.addAll(rest);
                break;
            }
        }
        return result;
    }
}