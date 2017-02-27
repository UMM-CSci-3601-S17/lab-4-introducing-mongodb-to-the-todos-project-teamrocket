package umm3601.todo;

import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.orderBy;

public class TodoController {

    private final MongoCollection<Document> todoCollection;

    public TodoController(String databaseName) throws IOException {
        // Set up our server address
        // (Default host: 'localhost', default port: 27017)
        // ServerAddress testAddress = new ServerAddress();

        // Try connecting to the server
        //MongoClient mongoClient = new MongoClient(testAddress, credentials);
        MongoClient mongoClient = new MongoClient(); // Defaults!

        // Try connecting to a database
        MongoDatabase db = mongoClient.getDatabase(databaseName);

        todoCollection = db.getCollection("todos");
    }

    // List todos
    public String listTodos(Map<String, String[]> queryParams) {
        Document filterDoc = new Document();
        Bson bodyFilter;


        if (queryParams.containsKey("owner") && !queryParams.get("owner")[0].equals("")) {
            String targetOwner = queryParams.get("owner")[0];
                filterDoc = filterDoc.append("owner", targetOwner);
        }

        if (queryParams.containsKey("category") && !queryParams.get("category")[0].equals("")) {
            String targetCategory = queryParams.get("category")[0];
            filterDoc = filterDoc.append("category", targetCategory);
        }

        if (queryParams.containsKey("status") && !queryParams.get("status")[0].equals("")) {
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

        if (queryParams.containsKey("orderBy") && !queryParams.get("orderBy")[0].equals("")) {
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


    public List<String> eachUniqueInField(String field) {
        AggregateIterable<Document> todoSummaryDoc
                = todoCollection.aggregate(
                Arrays.asList(
                        Aggregates.group(field)
                ));
        List<String> allCategories = new ArrayList<>();
        for (Document doc: todoSummaryDoc) {
            allCategories.add(doc.getString("_id"));
        }
        return allCategories;
    }

    public long returnNumComplete() {
        Document countDoc = new Document();
        countDoc.append("status", true);
        long totalTrueStat = todoCollection.count(countDoc);
        return totalTrueStat;
    }


    public String categoriesPercentComplete(List<String> categories) {
        String returnString = "";
        for(int i = 0; i < categories.size(); i++) {
            String category = categories.get(i);
            returnString = returnString + "\"" + category + "\"" + " : " +
                    (float)fieldComplete("category",category)/(float)fieldTotalMatching("category",category);
            if(i != categories.size() - 1) {
                returnString = returnString + ",";
            }
        }
        return returnString;
    }

    public long fieldTotalMatching(String field, String val) {
        Document countDoc = new Document();
        countDoc.append(field, val);
        return todoCollection.count(countDoc);
    }

    public long fieldComplete(String field, String val) {
        Document countDoc = new Document();
        countDoc.append(field, val);
        countDoc.append("status", true);
        return todoCollection.count(countDoc);
    }


    public String ownersPercentComplete(List<String> owners) {
        String returnString = "";
        for(int i = 0; i < owners.size(); i++) {
            String owner = owners.get(i);
            returnString = returnString + "\"" + owner + "\"" + " : " +
                    (float)fieldComplete("owner", owner)/(float)fieldTotalMatching("owner", owner);
            if(i != owners.size() - 1) {
                returnString = returnString + ",";
            }
        }
        return returnString;
    }



    public String todoSummary() {
        long totalCount = todoCollection.count();
        long completeTodos = returnNumComplete();
        float percentComplete = (float)completeTodos/(float)totalCount;
        List<String> allTheCategories = eachUniqueInField("$category");
        List<String> allTheOwners = eachUniqueInField("$owner");

        String returnString = "{\"percentToDosComplete\": " + JSON.serialize(percentComplete) + ","
                + "\"categoriesPercentComplete\": {" + categoriesPercentComplete(allTheCategories) + "}," +
                "\"ownersPercentComplete\": {" + ownersPercentComplete(allTheOwners) +"}" + "}";
        return returnString;
    }

}