package com.example.order.service;

import com.example.order.dto.OrderCreatedMessage;
import com.example.order.dto.OrderDto;
import com.example.order.entity.OrderEntity;
import com.example.order.entity.OrderStatus;
import com.example.order.kafka.KafkaProducerService;
import com.example.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final KafkaProducerService kafkaProducerService;

    public OrderEntity process(OrderDto dto) {
        OrderEntity dbEntity = addNew(dto);

        OrderCreatedMessage orderCreatedMessage = new OrderCreatedMessage();
        orderCreatedMessage.setOrderId(dbEntity.getId());
        orderCreatedMessage.setOrderName(dbEntity.getName());
        kafkaProducerService.sendOrder(orderCreatedMessage);
        return dbEntity;
    }

    public OrderEntity addNew(OrderDto dto) {
        OrderEntity entity = new OrderEntity();
        entity.setName(dto.getName());
        entity.setStatus(OrderStatus.PENDING);
        OrderEntity dbEntity = repository.save(entity);
        return dbEntity;
    }
}
