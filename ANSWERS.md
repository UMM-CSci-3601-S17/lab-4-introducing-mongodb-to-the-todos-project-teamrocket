# Answers

#### Exploring the server

We use gradle to populate the Mongo database with data from JSON files. The UserController object maintains a connection
to the database. When the server gets a request, it calls methods on the UserController object which queries the 
database.