package com.haydenshui.stock.trade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.haydenshui.stock.lib.annotation.LogDetails;
import com.haydenshui.stock.lib.dto.trade.OrderDTO;
import com.haydenshui.stock.lib.util.ApiResult;
import com.haydenshui.stock.trade.order.TradeOrderService;
import com.haydenshui.stock.utils.SecurityUtils;

import jakarta.validation.Valid;

/**
 * 交易控制器，提供订单相关功能
 */
@RestController
@RequestMapping("/api/orders")
public class TradeController {

    @Autowired
    private TradeOrderService tradeOrderService;

    /**
     * 创建订单
     * 
     * @param orderDTO 订单信息
     * @return 订单创建结果
     */
    @PostMapping
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        orderDTO.setSecuritiesAccountId(userId);
        return ResponseEntity.ok(ApiResult.success("订单创建成功", tradeOrderService.createOrder(orderDTO)));
    }

    /**
     * 获取订单详情
     * 
     * @param id 订单ID
     * @return 订单详情
     */
    @GetMapping("/{id}")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> getOrder(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResult.success("获取订单成功", tradeOrderService.getOrderByIdAndUser(id, userId)));
    }

    /**
     * 获取订单列表
     * 
     * @param status 订单状态过滤
     * @param stockCode 股票代码过滤
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param page 页码
     * @param size 每页大小
     * @param sortBy 排序字段
     * @param sortDirection 排序方向
     * @return 订单列表
     */
    @GetMapping
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String stockCode,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createTime") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResult.success("获取订单列表成功", 
                tradeOrderService.getOrders(userId, status, stockCode, startDate, 
                        endDate, page, size, sortBy, sortDirection)));
    }

    /**
     * 取消订单
     * 
     * @param id 订单ID
     * @return 取消结果
     */
    @DeleteMapping("/{id}")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> cancelOrder(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        tradeOrderService.cancelOrder(id, userId);
        return ResponseEntity.ok(ApiResult.success("取消订单成功", null));
    }

    /**
     * 更新订单状态（主要用于内部系统或管理员）
     * 
     * @param id 订单ID
     * @param status 新状态
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    @LogDetails
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResult<?>> updateOrderStatus(
            @PathVariable Long id, 
            @RequestParam String status) {
        return ResponseEntity.ok(ApiResult.success("更新订单状态成功", 
                tradeOrderService.updateOrderStatus(id, status)));
    }
    
    /**
     * 获取支持的订单类型
     * 
     * @return 支持的订单类型列表
     */
    @GetMapping("/order-types")
    @LogDetails
    public ResponseEntity<ApiResult<?>> getOrderTypes() {
        return ResponseEntity.ok(ApiResult.success("获取订单类型成功", 
                tradeOrderService.getOrderTypes()));
    }
}
