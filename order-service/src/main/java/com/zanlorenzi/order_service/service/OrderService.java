package com.zanlorenzi.order_service.service;

import com.zanlorenzi.order_service.dto.InventoryResponse;
import com.zanlorenzi.order_service.dto.OrderLineItemsDto;
import com.zanlorenzi.order_service.dto.OrderRequest;
import com.zanlorenzi.order_service.model.Order;
import com.zanlorenzi.order_service.model.OrderLineItems;
import com.zanlorenzi.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

  private final OrderRepository orderRepository;
  private final WebClient webClient;

  public void placeOrder(OrderRequest orderRequest) {

    Order order = new Order();

    order.setOrderNumber(UUID.randomUUID().toString());
    List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList().stream()
            .map(this::mapToDto).toList();

    order.setOrderLineItems(orderLineItems);

    var skuCodes = order.getOrderLineItems().stream().map(OrderLineItems::getSkuCode).toList();

    var result = webClient.get()
            .uri("http://localhost:8082/api/inventory",
                    uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
            .retrieve()
            .bodyToMono(InventoryResponse[].class)
            .block();

    boolean allProductsInStock = Arrays.stream(result).allMatch(InventoryResponse::isInStock);

    if(!allProductsInStock) {
      throw new IllegalArgumentException("Product is not in stock, please try again later");
    }
    orderRepository.save(order);
  }

  private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
    OrderLineItems orderLineItems = new OrderLineItems();
    orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
    orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
    orderLineItems.setPrice(orderLineItemsDto.getPrice());
    return orderLineItems;
  }
}
