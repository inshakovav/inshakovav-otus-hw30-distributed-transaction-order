package com.example.order;

import com.example.order.dto.OrderCreatedMessage;
import com.example.order.entity.OrderEntity;
import com.example.order.entity.OrderStatus;
import com.example.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
@ActiveProfiles("local")
class OrderApplicationTests {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private KafkaConsumer consumer;

    // To disable second @KafkaListener
//    @MockBean
//    KafkaConsumerService kafkaConsumerService;

    MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .build();
    }

//    @Test
    void contextLoads() {
        List<OrderEntity> userEntities = orderRepository.findAll();
        log.info("Read users={}", userEntities);

        OrderEntity order = new OrderEntity();
        order.setName("fistname");
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);
        log.info("New order saved");

        List<OrderEntity> userEntities1 = orderRepository.findAll();
        log.info("Read users={}", userEntities1);
    }

    @Test
    void createOrder() throws Exception {
        // before

        // act
        mockMvc.perform(
                post("http://localhost:8000/order")
                        .content("{ \"name\":\"foo\"}")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // verify
        OrderEntity lastOrder = orderRepository.findFirstByOrderByIdDesc();
        log.info("Last record={}", lastOrder);

        boolean messageConsumed = consumer.getLatch().await(10, TimeUnit.SECONDS);
        assertTrue(messageConsumed);

        OrderCreatedMessage receivedMessage = consumer.getReceivedMessage();
        assertEquals(receivedMessage.getOrderId(), lastOrder.getId());
        assertEquals(receivedMessage.getOrderName(),lastOrder.getName());
    }
}
