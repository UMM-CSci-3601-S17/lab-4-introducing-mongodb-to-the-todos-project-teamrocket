package umm3601.todo;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.*;
import org.bson.json.JsonReader;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.*;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class TodoControllerSpec {

    private TodoController todoController;

    @Before
    public void clearAndPopulateDB() throws IOException{
        String databaseName = "data-for-testing-only";
        MongoClient mongoClient = new MongoClient();
        MongoDatabase db = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> todoDocuments = db.getCollection("todos");
        todoDocuments.drop();
        List<Document> testTodos = new ArrayList<>();
        testTodos.add(Document.parse("{"
                        + "_id: \"58895985ae3b752b124e7663\","
                        + "owner: \"Fry\","
                        + "status: true,"
                        + "body: \"Ullamco irure laborum magna dolor non. Anim occaecat adipisicing cillum eu magna in.\","
                        + "category: \"homework\""
                        + "}"));
        testTodos.add(Document.parse("{"
                        + "_id: \"588959858f263be0a632afe0\","
                        + "owner: \"Blanche\","
                        + "status: true,"
                        + "body: \"Incididunt Lorem magna velit laborum enim. Eu nisi laboris aliquip magna eu pariatur occaecat occaecat amet consectetur officia ad amet minim.\","
                        + "category: \"video games\""
                        + "}"));
        testTodos.add(Document.parse("{"
                        + "_id: \"5889598593c146ed8d9a8e26\","
                        + "owner: \"Workman\","
                        + "status: true,"
                        + "body: \"Aliqua duis exercitation enim nostrud in ad. Sunt laborum enim laboris dolor nisi enim sunt deserunt mollit.\","
                        + "category: \"groceries\""
                        + "}"));
        testTodos.add(Document.parse("{"
                        + "_id: \"58895985d720b8016900726c\","
                        + "owner: \"Roberta\","
                        + "status: false,"
                        + "body: \"Occaecat sint enim velit aute sit non laboris ipsum cillum aute anim veniam. Dolor reprehenderit aliquip ullamco eiusmod ut sint deserunt aliqua.\","
                        + "category: \"groceries\""
                        + "}"));

        todoDocuments.insertMany(testTodos);

        todoController = new TodoController(databaseName);
    }

    // http://stackoverflow.com/questions/34436952/json-parse-equivalent-in-mongo-driver-3-x-for-java
    private BsonArray parseJsonArray(String json) {
        final CodecRegistry codecRegistry
                = CodecRegistries.fromProviders(Arrays.asList(
                new ValueCodecProvider(),
                new BsonValueCodecProvider(),
                new DocumentCodecProvider()));

        JsonReader reader = new JsonReader(json);
        BsonArrayCodec arrayReader = new BsonArrayCodec(codecRegistry);

        return arrayReader.decode(reader, DecoderContext.builder().build());
    }

    private static String extractFromBson(BsonValue val, String field) {
        BsonDocument doc = val.asDocument();
        return ((BsonString) doc.get(field)).getValue();
    }

    private Set<String> getStringSet(BsonArray docs, String field) {
        return docs
                .stream()
                .map(x -> TodoControllerSpec.extractFromBson(x, field))
                .sorted()
                .collect(Collectors.toSet());
    }

    @Test
    public void getAllTodos(){
        Map<String, String[]> emptyMap = new HashMap<>();
        String jsonResult = todoController.listTodos(emptyMap);
        BsonArray docs = parseJsonArray(jsonResult);

        assertEquals("Should be 4 todos", 4, docs.size());
        Set<String> owners = getStringSet(docs, "owner");
        Set<String> expectedNames = new HashSet<>(Arrays.asList("Fry", "Blanche", "Workman", "Roberta"));
        assertEquals("Names should match", expectedNames, owners);
    }

    @Test
    public void getTodosByID() {
        String id0 = "58895985ae3b752b124e7663";
        String jsonResult = todoController.getTodo(id0);
        Document doc0 = Document.parse(jsonResult);
        assertEquals("Name should be \"Fry\"", "Fry", doc0.getString("owner"));
        assertEquals("Status should be true", true, doc0.getBoolean("status"));
        assertEquals("Category should be homework", "homework", doc0.getString("category"));
    }

    @Test
    public void getTodosByStatus() {
        Map<String, String[]> queryMap = new HashMap<>();
        queryMap.put("status",new String[]{"complete"});
        String jsonResult = todoController.listTodos(queryMap);
        BsonArray docs = parseJsonArray(jsonResult);

        assertEquals("Should be 3 todos", 3, docs.size());

        Set<String> owners = getStringSet(docs, "owner");
        Set<String> expectedNames = new HashSet<>(Arrays.asList("Fry", "Blanche", "Workman"));
        assertEquals(expectedNames, owners);

        Set<String> categories = getStringSet(docs, "category");
        Set<String> expectedCategories = new HashSet<>(Arrays.asList("homework", "video games", "groceries"));
        assertEquals(expectedCategories, categories);


    }

    @Test
    public void getTodosByOwner() {
        Map<String, String[]> queryMap = new HashMap<>();
        queryMap.put("owner",new String[]{"Blanche"});
        String jsonResult = todoController.listTodos(queryMap);
        BsonArray docs = parseJsonArray(jsonResult);
        BsonDocument doc = docs.get(0).asDocument();

        assertEquals("We should only get one result", 1, docs.size());
        assertEquals("588959858f263be0a632afe0", ((BsonString) doc.get("_id")).getValue());
    }

    @Test
    public void getTodosByCategory() {
        Map<String, String[]> queryMap = new HashMap<>();
        queryMap.put("category",new String[]{"groceries"});
        String jsonResult = todoController.listTodos(queryMap);
        BsonArray docs = parseJsonArray(jsonResult);

        assertEquals("We should get 2 results", 2, docs.size());

        Set<String> owners = getStringSet(docs, "owner");
        Set<String> expectedNames = new HashSet<>(Arrays.asList("Roberta", "Workman"));
        assertEquals(expectedNames, owners);
    }

    @Test
    public void getTodosByBody() {
        Map<String, String[]> queryMap = new HashMap<>();
        queryMap.put("body",new String[]{"Lorem"});
        String jsonResult = todoController.listTodos(queryMap);
        BsonArray docs = parseJsonArray(jsonResult);
        BsonDocument doc = docs.get(0).asDocument();

        assertEquals("We should only get one result", 1, docs.size());
        assertEquals("588959858f263be0a632afe0", ((BsonString) doc.get("_id")).getValue());
    }

    @Test
    public void limitTodos() {
        Map<String, String[]> queryMap = new HashMap<>();
        queryMap.put("limit",new String[]{"2"});
        String jsonResult = todoController.listTodos(queryMap);
        BsonArray docs = parseJsonArray(jsonResult);

        assertEquals("We should get exactly 2 results", 2, docs.size());

    }

    @Test
    public void orderByOwner() {
        Map<String, String[]> queryMap = new HashMap<>();
        queryMap.put("orderBy",new String[]{"owner"});
        String jsonResult = todoController.listTodos(queryMap);
        BsonArray docs = parseJsonArray(jsonResult);

        // We are using a List instead of a set because ORDER MATTERS HERE
        List<String> owners = docs
                .stream()
                .map(x -> TodoControllerSpec.extractFromBson(x, "owner"))
                .sorted()
                .collect(Collectors.toList());
        assertEquals(Arrays.asList("Blanche", "Fry", "Roberta", "Workman"), owners);
    }

    @Test
    public void eachUniqueInFieldT() {
        Set<String> actualResults1 = new HashSet<>(todoController.eachUniqueInField("$owner"));
        Set<String> expectedResults1 = new HashSet<>(Arrays.asList("Fry", "Blanche", "Workman", "Roberta"));
        assertEquals("Should return all 4 owners in this test file",
                expectedResults1, actualResults1);
    }

    @Test
    public void returnNumCompleteT() {
        assertEquals("Should return all 3, for 3 true statuses",
                3 , todoController.returnNumComplete());
    }

    @Test
    public void categoryPercentCompleteT() {
        String actualResults1 = todoController.categoriesPercentComplete(Arrays.asList("homework", "video games", "groceries"));
        String expectedResults1 = "\"homework\" : 1.0,\"video games\" : 1.0,\"groceries\" : 0.5";
        assertEquals("Should return percent of each category complete",
                expectedResults1, actualResults1);
    }

    @Test
    public void fieldTotalMatchingT() {
        assertEquals("Should return total number of Frys in this class",
                1, todoController.fieldTotalMatching("owner", "Fry"));
        assertEquals("Should return total number of Blanches in this class",
                1, todoController.fieldTotalMatching("owner", "Blanche"));
        assertEquals("Should return total number of Workmen in this class",
                1, todoController.fieldTotalMatching("owner", "Workman"));
        assertEquals("Should return total number of Robertas in this class",
                1, todoController.fieldTotalMatching("owner", "Roberta"));
    }

    @Test
    public void fieldCompleteT() {
        assertEquals("Should return number of complete todos from Fry",
                1, todoController.fieldComplete("owner", "Fry"));
        assertEquals("Should return number of complete todos from Blanche",
                1, todoController.fieldComplete("owner", "Blanche"));
        assertEquals("Should return number of complete todos from Workman",
                1, todoController.fieldComplete("owner", "Workman"));
        assertEquals("Should return number of complete todos from Roberta",
                0, todoController.fieldComplete("owner", "Roberta"));
    }

    @Test
    public void ownersPercentCompleteT() {
        String actualResults1 = todoController.ownersPercentComplete(Arrays.asList("Fry", "Blanche", "Workman", "Roberta"));
        String expectedResults1 = "\"Fry\" : 1.0,\"Blanche\" : 1.0,\"Workman\" : 1.0,\"Roberta\" : 0.0";
        assertEquals("Should return percent complete for each owner",
                expectedResults1, actualResults1);
    }

    @Test
    public void todoSummaryT() {
        String actualResults1 = todoController.todoSummary();
        String expectedResults1 = "{\"percentToDosComplete\": 0.75,\"categoriesPercentComplete\": " +
                "{\"groceries\" : 0.5,\"video games\" : 1.0,\"homework\" : 1.0},\"ownersPercentComplete\": " +
                "{\"Roberta\" : 0.0,\"Workman\" : 1.0,\"Blanche\" : 1.0,\"Fry\" : 1.0}}";
        assertEquals("Should return The summary for this set of To-dos",
                expectedResults1, actualResults1);
    }






}
