package umm3601.todo;

import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Sorts;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.where;
import static com.mongodb.client.model.Filters.and;

public class TodoController {

    private final MongoCollection<Document> todoCollection;

    public TodoController() throws IOException {
        // Set up our server address
        // (Default host: 'localhost', default port: 27017)
        // ServerAddress testAddress = new ServerAddress();

        // Try connecting to the server
        //MongoClient mongoClient = new MongoClient(testAddress, credentials);
        MongoClient mongoClient = new MongoClient(); // Defaults!

        // Try connecting to a database
        MongoDatabase db = mongoClient.getDatabase("test");

        todoCollection = db.getCollection("todos");
    }

    // List todos
    public String listTodos(Map<String, String[]> queryParams) {
        Document filterDoc = new Document();
        Bson bodyFilter;


        if (queryParams.containsKey("owner")) {
            String targetOwner = queryParams.get("owner")[0];
            filterDoc = filterDoc.append("owner", targetOwner);
        }

        if (queryParams.containsKey("category")) {
            String targetCategory = queryParams.get("category")[0];
            filterDoc = filterDoc.append("category", targetCategory);
        }

        if (queryParams.containsKey("status")) {
            boolean targetStatus = queryParams.get("status")[0].equals("complete");
            filterDoc = filterDoc.append("status", targetStatus);
        }


        if (queryParams.containsKey("body")) {
            String targetBody = queryParams.get("body")[0];

            bodyFilter = where("this.body.indexOf(\"" + targetBody + "\") !== -1;");
            //             ^^ this is sin
        } else {
            bodyFilter = where("return true;");
        }

        FindIterable<Document> matchingTodos = todoCollection.find(and(filterDoc, bodyFilter));

        return JSON.serialize(matchingTodos);
    }

    // Get a single todo
    public String getTodo(String id) {
        FindIterable<Document> jsonTodos
                = todoCollection
                .find(eq("_id", new ObjectId(id)));

        Iterator<Document> iterator = jsonTodos.iterator();

        Document todo = iterator.next();

        return todo.toJson();
    }

//    // Get the average age of all todos by company
//    public String getAverageAgeByCompany() {
//        AggregateIterable<Document> documents
//                = todoCollection.aggregate(
//                Arrays.asList(
//                        Aggregates.group("$company",
//                                Accumulators.avg("averageAge", "$age")),
//                        Aggregates.sort(Sorts.ascending("_id"))
//                ));
//        System.err.println(JSON.serialize(documents));
//        return JSON.serialize(documents);
//    }

}
