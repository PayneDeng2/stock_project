package com.haydenshui.stock.stock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.haydenshui.stock.lib.annotation.LogDetails;
import com.haydenshui.stock.lib.dto.stock.StockAlertDTO;
import com.haydenshui.stock.lib.util.ApiResult;
import com.haydenshui.stock.utils.SecurityUtils;

import jakarta.validation.Valid;

/**
 * 股票控制器，提供股票查询和相关功能
 */
@RestController
public class StockController {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockAlertService stockAlertService;

    /**
     * 搜索股票
     * 
     * @param query 搜索关键词
     * @return 符合条件的股票列表
     */
    @GetMapping("/api/stocks/search")
    @LogDetails
    public ResponseEntity<ApiResult<?>> searchStocks(@RequestParam String query) {
        return ResponseEntity.ok(ApiResult.success("搜索成功", stockService.searchStocks(query)));
    }

    /**
     * 获取股票基本信息
     * 
     * @param code 股票代码
     * @return 股票基本信息
     */
    @GetMapping("/api/stocks/{code}")
    @LogDetails
    public ResponseEntity<ApiResult<?>> getStockInfo(@PathVariable String code) {
        return ResponseEntity.ok(ApiResult.success("获取股票信息成功", stockService.getStockByCode(code)));
    }

    /**
     * 获取实时行情
     * 
     * @param code 股票代码
     * @return 实时行情数据
     */
    @GetMapping("/api/stocks/{code}/realtime")
    @LogDetails
    public ResponseEntity<ApiResult<?>> getRealtimeQuote(@PathVariable String code) {
        return ResponseEntity.ok(ApiResult.success("获取实时行情成功", stockService.getRealtimeQuote(code)));
    }

    /**
     * 获取历史行情
     * 
     * @param code 股票代码
     * @param period 时间周期，如"1d", "1w", "1m", "1y"
     * @return 历史行情数据
     */
    @GetMapping("/api/stocks/{code}/history")
    @LogDetails
    public ResponseEntity<ApiResult<?>> getHistoryQuotes(
            @PathVariable String code, 
            @RequestParam String period) {
        return ResponseEntity.ok(ApiResult.success("获取历史行情成功", stockService.getHistoryQuotes(code, period)));
    }

    /**
     * 获取技术指标
     * 
     * @param code 股票代码
     * @param indicatorType 指标类型，如"MACD", "RSI", "KDJ"等
     * @return 技术指标数据
     */
    @GetMapping("/api/stocks/{code}/indicators")
    @LogDetails
    public ResponseEntity<ApiResult<?>> getIndicators(
            @PathVariable String code, 
            @RequestParam String indicatorType) {
        return ResponseEntity.ok(ApiResult.success("获取指标数据成功", stockService.getIndicators(code, indicatorType)));
    }

    /**
     * 创建股票价格提醒
     * 
     * @param alertDTO 提醒设置
     * @return 创建结果
     */
    @PostMapping("/api/alerts")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> createAlert(@Valid @RequestBody StockAlertDTO alertDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        alertDTO.setUserId(userId);
        return ResponseEntity.ok(ApiResult.success("创建提醒成功", stockAlertService.createAlert(alertDTO)));
    }

    /**
     * 获取用户的所有提醒
     * 
     * @return 提醒列表
     */
    @GetMapping("/api/alerts")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> getUserAlerts() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResult.success("获取提醒成功", stockAlertService.getAlertsByUserId(userId)));
    }

    /**
     * 删除提醒
     * 
     * @param id 提醒ID
     * @return 删除结果
     */
    @DeleteMapping("/api/alerts/{id}")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> deleteAlert(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        stockAlertService.deleteAlert(id, userId);
        return ResponseEntity.ok(ApiResult.success("删除提醒成功", null));
    }
}