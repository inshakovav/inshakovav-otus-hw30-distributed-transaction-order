package com.example.order.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DeliveryRejectedMessage {
    private Long orderId;
    private Long deliveryId;
    private String errorCode;
}
