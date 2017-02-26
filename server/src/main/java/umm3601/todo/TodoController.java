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
import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.orderBy;

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

        FindIterable<Document> matchingTodos;

        if (queryParams.containsKey("orderBy")) {
            String targetOrder = queryParams.get("orderBy")[0];
            matchingTodos = todoCollection.find(and(filterDoc, bodyFilter)).sort(orderBy(ascending(targetOrder)));
        } else {
            matchingTodos = todoCollection.find(and(filterDoc, bodyFilter));
        }

        long limit = 0;
        if (queryParams.containsKey("limit")) {
            String targetLimit = queryParams.get("limit")[0];
            try {
                limit = Long.parseLong(targetLimit);
            } catch (NumberFormatException e) {
                limit = 0;
            }
        }

        return JSON.serialize(matchingTodos.limit((int) limit));
    }

    // Get a single todo
    public String getTodo(String id) {
        FindIterable<Document> jsonTodos
                = todoCollection
                .find(eq("_id", id));

        Iterator<Document> iterator = jsonTodos.iterator();
        String returnval;
        if (iterator.hasNext()) {
            returnval = iterator.next().toJson();
        } else {
            returnval = "";
        }
        return returnval;
    }

    //    // Get the average age of all todos by company
//    public String getAverageAgeByCompany() {
//        AggregateIterable<Document> documents
//                = todoCollection.aggregate(
//                Arrays.asList(
//                        Aggregates.group("$company",
//                                Accumulators.avg("averageAge", "$age")),
//                        Aggregates.sort(Sorts.ascending("_id"))
//                    public String getAverageAgeByCompany() {));
//        System.err.println(JSON.serialize(documents));
//        return JSON.serialize(documents);
//    }

            /// below is a list of counts for the summary of todos to calculate percentages.
    public long returnNumComplete() {
        Document countDoc = new Document();
        countDoc.append("status", true);
        long totalTrueStat = todoCollection.count(countDoc);
        return totalTrueStat;
    }



//    public List<Long> returnNumCompleteCategory(List<String> allCategories) {
//        List<Long> countsOfCompletePerCategory = new ArrayList<Long>();
//        for(String category: allCategories) {
//
//        }
//        Document countDoc = new Document();
//        countDoc.append("status", true);
//        long[] totalTrueStat = todoCollection.count(countDoc);
//        return totalTrueStat;
//    }


    public String todoSummary() {
        long totalCount = todoCollection.count();
        long completeTodos = returnNumComplete();
        float percentComplete = (float)completeTodos/(float)totalCount;
        AggregateIterable<Document> todoSummaryDoc
                = todoCollection.aggregate(
                Arrays.asList(
                        Aggregates.group("$category")
                ));
        List<String> allCategories = new ArrayList<>();
        for (Document doc: todoSummaryDoc) {
            allCategories.add(doc.getString("_id"));
        }
        System.err.println(JSON.serialize(todoSummaryDoc));

        String returnString = "{\"percentToDosComplete\": " + JSON.serialize(percentComplete) + ","
                + "\"categoriesPercentCompelete\": {" + JSON.serialize(todoSummaryDoc);
        return returnString;
    }

}