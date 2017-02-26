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
                        + "status: true,"
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
}
