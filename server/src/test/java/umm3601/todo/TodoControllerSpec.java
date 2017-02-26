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

    private static String getName(BsonValue val) {
        BsonDocument doc = val.asDocument();
        return ((BsonString) doc.get("owner")).getValue();
    }

    private static String getCategory(BsonValue val) {
        BsonDocument doc = val.asDocument();
        return ((BsonString) doc.get("category")).getValue();
    }

    @Test
    public void getAllTodos(){
        Map<String, String[]> emptyMap = new HashMap<>();
        String jsonResult = todoController.listTodos(emptyMap);
        BsonArray docs = parseJsonArray(jsonResult);

        assertEquals("Should be 4 todos", 4, docs.size());
        Set<String> names = docs
                .stream()
                .map(TodoControllerSpec::getName)
                .sorted()
                .collect(Collectors.toSet());
        Set<String> expectedNames = new HashSet<>(Arrays.asList("Fry", "Blanche", "Workman", "Roberta"));
        assertEquals("Names should match", expectedNames, names);
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

        Set<String> names = docs
                .stream()
                .map(TodoControllerSpec::getName)
                .sorted()
                .collect(Collectors.toSet());
        Set<String> expectedNames = new HashSet<>(Arrays.asList("Fry", "Blanche", "Workman"));
        assertEquals(expectedNames, names);

        Set<String> categories = docs
                .stream()
                .map(TodoControllerSpec::getCategory)
                .sorted()
                .collect(Collectors.toSet());
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

        Set<String> names = docs
                .stream()
                .map(TodoControllerSpec::getName)
                .sorted()
                .collect(Collectors.toSet());
        Set<String> expectedNames = new HashSet<>(Arrays.asList("Roberta", "Workman"));
        assertEquals(expectedNames, names);
    }

    @Test
    public void limitTodos() {
        Map<String, String[]> queryMap = new HashMap<>();
        queryMap.put("limit",new String[]{"2"});
        String jsonResult = todoController.listTodos(queryMap);
        BsonArray docs = parseJsonArray(jsonResult);

        assertEquals("We should get exactly 2 results", 2, docs.size());

    }

}
