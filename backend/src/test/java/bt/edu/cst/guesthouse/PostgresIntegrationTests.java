package bt.edu.cst.guesthouse;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Opt in only against a disposable database whose name ends in _test. */
@EnabledIfEnvironmentVariable(named="RUN_POSTGRES_TESTS",matches="true")
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={
    "app.registration-enabled=true","app.jwt-secret=integration-only-jwt-secret-with-at-least-32-bytes",
    "app.frontend-url=http://127.0.0.1:5173","ADMIN_EMAIL=","ADMIN_PASSWORD="})
class PostgresIntegrationTests {
    @DynamicPropertySource static void database(DynamicPropertyRegistry properties) {
        String url=System.getenv("TEST_DATABASE_URL");
        if (url==null || !url.matches("^jdbc:postgresql://[^/]+/[a-zA-Z0-9_]+_test(?:\\?.*)?$"))
            throw new IllegalArgumentException("TEST_DATABASE_URL must name a disposable database ending in _test");
        properties.add("spring.datasource.url",() -> url);
        properties.add("spring.datasource.username",() -> Objects.requireNonNull(System.getenv("TEST_DATABASE_USERNAME")));
        properties.add("spring.datasource.password",() -> Objects.requireNonNull(System.getenv("TEST_DATABASE_PASSWORD")));
    }
    @Value("${local.server.port}") int port;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder passwords;
    private final JsonMapper json=new JsonMapper();
    private final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private static final String PASSWORD="Integration-only-password!";
    private static final String ROOM="00000000-0000-0000-0000-000000000101";
    private String user,admin;
    private final LocalDate today=LocalDate.now(ZoneId.of("Asia/Thimphu"));
    record Result(int status,JsonNode body) {}
    private Result call(String method,String path,Object body,String token) throws Exception {
        return call(method,path,body,token,null);
    }
    private Result call(String method,String path,Object body,String token,String origin) throws Exception {
        var request=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/api"+path)).timeout(Duration.ofSeconds(20));
        if (body!=null) request.header("Content-Type","application/json");
        if (token!=null) request.header("Authorization","Bearer "+token);
        if (origin!=null) request.header("Origin",origin);
        request.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
        var response=client.send(request.build(),HttpResponse.BodyHandlers.ofString());
        JsonNode data=response.body().isBlank() || !response.headers().firstValue("content-type").orElse("").contains("json")
            ?json.createObjectNode():json.readTree(response.body());
        return new Result(response.statusCode(),data);
    }
    private String login(String email) throws Exception {
        Result response=call("POST","/auth/login",Map.of("email",email,"password",PASSWORD),null);
        assertEquals(200,response.status,response.body.toString()); return response.body.path("token").asText();
    }
    @BeforeEach void fixtures() throws Exception {
        jdbc.execute("TRUNCATE facility_request_history,facility_requests,booking_status_history,bookings,auth_sessions,app_users CASCADE");
        jdbc.update("UPDATE rooms SET service_status='ACTIVE'");
        jdbc.update("UPDATE rooms SET room_number=right(id::text,3)");
        jdbc.update("INSERT INTO app_users(id,full_name,email,password_hash,role,active) VALUES (?,?,?,?,?,true)",UUID.randomUUID(),"Integration Admin","admin@example.test",passwords.encode(PASSWORD),"ADMIN");
        assertEquals(201,call("POST","/auth/register",Map.of("fullName","Integration Guest","email","guest@example.test","password",PASSWORD,"role","ADMIN"),null).status);
        user=login("guest@example.test"); admin=login("admin@example.test");
    }
    private Map<String,Object> booking(int guests,boolean bed,LocalDate arrival) {
        return Map.of("fullName","Integration Guest","email","guest@example.test","guestCategory","GENERAL_GUEST","purpose","PostgreSQL integration test",
            "guests",guests,"checkIn",arrival.toString(),"checkOut",arrival.plusDays(1).toString(),"preferredCategory","STANDARD","extraBedRequested",bed);
    }
    private String createBooking(int guests,boolean bed,LocalDate arrival) throws Exception {
        var result=call("POST","/bookings",booking(guests,bed,arrival),user); assertEquals(201,result.status,result.body.toString()); return result.body.path("id").asText();
    }
    private Result approve(String id,boolean bed) throws Exception { return call("PATCH","/admin/bookings/"+id+"/approve",Map.of("roomId",ROOM,"approveExtraBed",bed,"remark","Test allocation"),admin); }
    @Test void simultaneousRequestsCannotHoldTheSameRoom() throws Exception {
        var request=new HashMap<String,Object>(booking(2,false,today.plusDays(75))); request.put("roomId",ROOM);
        try (var executor=Executors.newFixedThreadPool(2)) {
            var first=executor.submit(() -> call("POST","/bookings",request,user));
            var second=executor.submit(() -> call("POST","/bookings",request,user));
            var codes=new ArrayList<>(List.of(first.get(30,TimeUnit.SECONDS).status,second.get(30,TimeUnit.SECONDS).status));
            Collections.sort(codes); assertEquals(List.of(201,409),codes);
        }
        var reference=call("GET","/public/config",null,null);
        assertEquals(8,reference.body.path("standardRooms").asInt()); assertEquals(2,reference.body.path("specialRooms").asInt());
    }
    @Test void authenticationOwnershipRevocationAndCors() throws Exception {
        assertEquals("USER",call("GET","/auth/me",null,user).body.path("role").asText());
        assertEquals(401,call("GET","/auth/me",null,null).status);
        assertEquals(403,call("GET","/admin/dashboard/summary",null,user).status);
        assertEquals(200,call("GET","/public/config",null,null,"http://127.0.0.1:5173").status);
        assertEquals(403,call("GET","/public/config",null,null,"https://untrusted.example").status);
        String id=createBooking(2,false,today.plusDays(10));
        assertEquals(201,call("POST","/auth/register",Map.of("fullName","Other Guest","email","other@example.test","password",PASSWORD),null).status);
        assertEquals(404,call("GET","/bookings/"+id,null,login("other@example.test")).status);
        assertEquals(204,call("POST","/auth/logout",null,user).status);
        assertEquals(401,call("GET","/auth/me",null,user).status);
    }
    private Map<String,Object> convention(LocalDate day,String start,String end,String booking) {
        Map<String,Object> body=new HashMap<>(Map.of("title","Workshop","purpose","Integration event","eventDate",day.toString(),"startTime",start,"endTime",end,"expectedParticipants",10));
        if (booking!=null) body.put("bookingId",booking); return body;
    }
    private String convention(LocalDate day,String start,String end) throws Exception {
        var result=call("POST","/convention-hall/requests",convention(day,start,end,null),user); assertEquals(201,result.status); return result.body.path("id").asText();
    }
    @Test void completeThreeGuestLifecycleRequiresBedAndReleasesInventory() throws Exception {
        assertEquals(400,call("POST","/bookings",booking(3,false,today),user).status);
        String id=createBooking(3,true,today);
        assertEquals(409,approve(id,false).status); assertEquals(200,approve(id,true).status);
        assertEquals(409,call("PATCH","/admin/bookings/"+id+"/check-in",null,admin).status);
        assertEquals(200,call("PATCH","/admin/bookings/"+id+"/extra-bed/setup",Map.of("added",true),admin).status);
        assertEquals(200,call("PATCH","/admin/bookings/"+id+"/check-in",null,admin).status);
        assertEquals(200,call("PATCH","/admin/bookings/"+id+"/check-out",null,admin).status);
        var detail=call("GET","/bookings/"+id,null,user).body;
        assertEquals("COMPLETED",detail.path("booking").path("status").asText());
        assertTrue(detail.path("timeline").toString().contains("CHECKED_OUT"));
        assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM bookings WHERE assigned_room_id=? AND status IN ('APPROVED','CHECKED_IN')",Integer.class,UUID.fromString(ROOM)));
    }
}
