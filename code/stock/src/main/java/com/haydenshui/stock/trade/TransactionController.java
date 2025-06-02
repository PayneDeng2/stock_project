package com.haydenshui.stock.trade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.haydenshui.stock.lib.annotation.LogDetails;
import com.haydenshui.stock.lib.util.ApiResult;
import com.haydenshui.stock.utils.SecurityUtils;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 交易记录查询控制器，提供交易记录查询、统计和报表功能
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    /**
     * 获取交易记录
     * 
     * @param filters 过滤条件，可包含：stockCode、startDate、endDate、tradeType等
     * @param page 页码
     * @param size 每页大小
     * @param sortBy 排序字段
     * @param sortDirection 排序方向
     * @return 交易记录列表
     */
    @GetMapping
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> getTransactions(
            @RequestParam(required = false) Map<String, String> filters,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "executionTime") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResult.success("获取交易记录成功",
                transactionService.getTransactions(userId, filters, page, size, sortBy, sortDirection)));
    }

    /**
     * 获取交易统计
     * 
     * @param period 统计周期，如"day"、"week"、"month"、"year"
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 交易统计数据
     */
    @GetMapping("/statistics")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> getTransactionStatistics(
            @RequestParam(required = false, defaultValue = "month") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResult.success("获取交易统计成功",
                transactionService.getTransactionStatistics(userId, period, startDate, endDate)));
    }

    /**
     * 获取交易报表
     * 
     * @param reportType 报表类型，如"profit-loss"、"turnover"、"commission"
     * @param period 统计周期
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 交易报表数据
     */
    @GetMapping("/reports")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> getTransactionReports(
            @RequestParam String reportType,
            @RequestParam(required = false, defaultValue = "month") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResult.success("获取交易报表成功",
                transactionService.getTransactionReports(userId, reportType, period, startDate, endDate)));
    }

    /**
     * 导出交易记录
     * 
     * @param formatType 导出格式，如"csv"、"pdf"、"excel"
     * @param filters 过滤条件
     * @param response HTTP响应，用于文件下载
     */
    @GetMapping("/export")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public void exportTransactions(
            @RequestParam String formatType,
            @RequestParam(required = false) Map<String, String> filters,
            HttpServletResponse response) {
        Long userId = SecurityUtils.getCurrentUserId();
        transactionService.exportTransactions(userId, formatType, filters, response);
    }
}