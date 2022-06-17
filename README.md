# To Run

From root directory:

```
sbt compile
sbt run
```

This starts the sever on localhost:8080.

Then, you can query like so:

`curl 'http://localhost:8080/news?num_articles=10&keywords=blockchain' | jq`