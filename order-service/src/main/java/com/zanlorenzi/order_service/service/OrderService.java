package com.zanlorenzi.order_service.service;

import com.zanlorenzi.order_service.dto.OrderLineItemsDto;
import com.zanlorenzi.order_service.dto.OrderRequest;
import com.zanlorenzi.order_service.model.Order;
import com.zanlorenzi.order_service.model.OrderLineItems;
import com.zanlorenzi.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

  private final OrderRepository orderRepository;

  public void placeOrder(OrderRequest orderRequest) {

    Order order = new Order();

    order.setOrderNumber(UUID.randomUUID().toString());
    List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList().stream()
            .map(this::mapToDto).toList();

    order.setOrderLineItems(orderLineItems);

    orderRepository.save(order);

  }

  private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
    OrderLineItems orderLineItems = new OrderLineItems();
    orderLineItems.setSkuCode(orderLineItems.getSkuCode());
    orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
    orderLineItems.setPrice(orderLineItemsDto.getPrice());
    return orderLineItems;
  }
}
