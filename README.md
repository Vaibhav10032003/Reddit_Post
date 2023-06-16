# Reddit_Post
It is a java application which support various operations through api calling.Here we can also add post in the subreddit which we want with the help of api call.Also it gives you data of new posts which are added into subreddit which data you have already have.

1)The reddit api used to get access token is:
https://www.reddit.com/api/v1/access_token
2)The api used for fetching subreddit data is 
https://oauth.reddit.com/r/ + subReddit + /hot
3)The api used for adding post to some subreddit is
https://oauth.reddit.com/api/submit

Here I kept server to localhost:8099.
The project provide access to following things:
1)To view hot posts for some subreddit also it will add the post into database if it doesn't exist
localhost:8099/{subredditname}
2)To search by word for in the posts is
localhost:8099/search/{word}
3)To delete some subreddit from your database
localhost:8099/delete/{subredditname}
4)To sort all posts in your database by creation time is
localhost:8099/sort
5)To add post into some subreddit from api calling is
localhost:8099/reddit/addpost
Here the method is POST and and additional details needed to provide are (String subreddit,String title,String text)
6)It will fetch new posts into subreddits which are existing in your database when the server is running at some fixed intervals and add them to the datbase. Here I have taken it as 120 seconds.
