package ru.viktor141.tms.controller;


import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaskControllerTests {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        postgres.start();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String jwtUser1Token;
    private String jwtUser2Token;

    @BeforeAll
    public void setup() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // Первый юзер
        String user1Json = "{\"email\":\"test@example.com\",\"password\":\"pass123\"}";
        MvcResult result1 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user1Json))
                .andExpect(status().isCreated())
                .andReturn();

        jwtUser1Token = result1.getResponse().getContentAsString();

        // Второй юзер
        String user2Json = "{\"email\":\"test1@example.com\",\"password\":\"pass123\"}";
        MvcResult result2 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user2Json))
                .andExpect(status().isCreated())
                .andReturn();

        jwtUser2Token = result2.getResponse().getContentAsString();

        // Присвоение второму юзеру прав администратора
        jdbcTemplate.update("UPDATE users SET role = ? WHERE id = ?", "ADMIN", 2);

    }

    @Test
    @Order(1)
    public void testGetAllTasks() throws Exception {
        // Проверка работы TaskController
        mockMvc.perform(get("/api/tasks/all")
                        .header("Authorization", "Bearer " + jwtUser1Token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(2)
    public void testCreateTask() throws Exception {
        //Создание задачи
        String taskJson = """
                {
                    "title": "New Task",
                    "description": "Test Description",
                    "status": "PENDING",
                    "priority": "HIGH"
                }""";

        mockMvc.perform(post("/api/tasks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson)
                        .header("Authorization", "Bearer " + jwtUser1Token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }


    @Test
    @Order(3)
    public void testUpdateTask() throws Exception {
        //Простое изменение полей задачи
        String updatedTaskJson = """
                {
                    "title": "Updated Task",
                    "description": "Updated Description",
                    "status": "COMPLETED",
                    "priority": "LOW"
                }""";

        // Предполагается, что ID задачи для обновления равен 1.
        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedTaskJson)
                        .header("Authorization", "Bearer " + jwtUser1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.priority").value("LOW"));
    }


    @Test
    @Order(4)
    public void testFullUpdateTask() throws Exception {
        // Изменение задачи с добавлением поля 'authorId' для присвоения другого пользователя как автора
        String updatedTaskJson = """
                {
                    "title": "Updated Task",
                    "description": "Updated Description",
                    "priority": "HIGH",
                    "author": {
                            "id": 2
                        }
                }""";

        // Предполагается, что ID задачи для обновления равен 1.
        // Так же предполагается что, чтобы изменить автора задачи нужны права администратора
        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedTaskJson)
                        .header("Authorization", "Bearer " + jwtUser2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.author.id").value(2));
    }


    @Test
    @Order(5)
    public void testGetUserTasks() throws Exception {
        // Запрос тасков по автору и исполнителю
        mockMvc.perform(get("/api/tasks?page=0&size=10&sort=title,asc&authorId=1&assigneeId=2")
                        .header("Authorization", "Bearer " + jwtUser2Token))
                .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    public void testAddComment() throws Exception {
        // Добавление комментария
        String newCommentJson = """
                {
                    "text": "This is a test comment."
                }""";

        // Предполагается, что ID задачи, к которой нужно добавить комментарий, равен 1.
        mockMvc.perform(post("/api/tasks/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newCommentJson)
                        .header("Authorization", "Bearer " + jwtUser2Token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("This is a test comment."))
                .andDo(print());
    }


    @Test
    @Order(7)
    public void testDeleteTask() throws Exception {
        // Предполагается, что ID задачи, которую нужно удалить, равен 1.
        mockMvc.perform(delete("/api/tasks/1")
                        .header("Authorization", "Bearer " + jwtUser2Token))
                .andExpect(status().isOk());
    }

}
