#!/bin/bash

echo "Deploying Goodla DS to all nodes"

echo "Deploying node 1"

echo "include \"node_1.conf\"" >> src/main/resources/application.conf

heroku git:remote -a goodla-ds-1

git add .

git commit -m "Deploying node-1"

git push heroku master

echo "Deploying node 2"

echo "include \"node_2.conf\"" >> src/main/resources/application.conf

heroku git:remote -a goodla-ds-2

git add .

git commit -m "Deploying node-2"

git push heroku master


echo "Deploying node 3"

echo "include \"node_1.conf\"" >> src/main/resources/application.conf

heroku git:remote -a goodla-ds-3

git add .

git commit -m "Deploying node-3"

git push heroku master

git push origin master

