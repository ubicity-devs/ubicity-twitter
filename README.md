This is and was the very first plugin ever written for ubicity. 

It takes a user-provided set of OAuth credentials for signing into the Twitter API, 
defines a user-parameterized geographical bounding box, and then filters the Twitter
stream to retain only geo-referenced tweets from within that bounding box. 
Retained tweets are then offered, in JSON format, to a Queue handled by ubicity core,
which is polled regularly. Each JSON-formatted tweet is offered to ubicity core's 
elasticsearch instance for indexing. 
  


[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/ubicity-principal/ubicity-twitterplugin/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

