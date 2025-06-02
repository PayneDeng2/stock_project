package com.haydenshui.stock.trade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.haydenshui.stock.lib.annotation.ServiceLog;
import com.haydenshui.stock.lib.dto.trade.TradeExecutionDTO;
import com.haydenshui.stock.lib.entity.trade.TradeExecution;
import com.haydenshui.stock.securities.SecuritiesAccountService;
import com.haydenshui.stock.trade.execution.TradeExecutionRepository;
import com.haydenshui.stock.utils.CsvExportUtils;
import com.haydenshui.stock.utils.ExcelExportUtils;
import com.haydenshui.stock.utils.PdfExportUtils;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 交易记录查询服务，提供交易记录查询、统计和报表功能
 */
@Service
public class TransactionService {
    
    @Autowired
    private TradeExecutionRepository tradeExecutionRepository;
    
    /**
     * 获取交易记录列表（支持条件查询和分页）
     * 
     * @param userId 当前用户ID
     * @param filters 过滤条件，如stockCode、startDate、endDate、tradeType等
     * @param page 页码
     * @param size 每页大小
     * @param sortBy 排序字段
     * @param sortDirection 排序方向
     * @return 分页的交易记录列表
     */
    @ServiceLog
    public Page<TradeExecution> getTransactions(
            Long userId, 
            Map<String, String> filters, 
            int page, 
            int size, 
            String sortBy, 
            String sortDirection) {
        
        // 构建查询条件
        Pageable pageable = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        
        // 从过滤条件中提取值
        String stockCode = filters.getOrDefault("stockCode", null);
        String startDateStr = filters.getOrDefault("startDate", null);
        String endDateStr = filters.getOrDefault("endDate", null);
        String tradeType = filters.getOrDefault("tradeType", null);
        
        // 解析日期
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        
        if (startDateStr != null && !startDateStr.isEmpty()) {
            startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ISO_DATE).atStartOfDay();
        }
        
        if (endDateStr != null && !endDateStr.isEmpty()) {
            endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ISO_DATE).plusDays(1).atStartOfDay();
        }
        
        // 执行查询
        return tradeExecutionRepository.findByUserIdAndFilters(
                userId, stockCode, startDate, endDate, tradeType, pageable);
    }
    
    /**
     * 获取交易统计数据
     * 
     * @param userId 用户ID
     * @param period 统计周期，如"day", "week", "month", "year"
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计结果
     */
    @ServiceLog
    public Map<String, Object> getTransactionStatistics(
            Long userId, 
            String period, 
            String startDateStr, 
            String endDateStr) {
        
        // 解析日期
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        
        if (startDateStr != null && !startDateStr.isEmpty()) {
            startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ISO_DATE).atStartOfDay();
        } else {
            // 默认统计最近一个月
            startDate = LocalDateTime.now().minusMonths(1);
        }
        
        if (endDateStr != null && !endDateStr.isEmpty()) {
            endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ISO_DATE).plusDays(1).atStartOfDay();
        } else {
            endDate = LocalDateTime.now();
        }
        
        // 获取指定时间范围内的所有交易
        List<TradeExecution> executions = tradeExecutionRepository.findBySecuritiesAccountIdAndExecutionTimeBetween(
                userId, startDate, endDate);
        
        // 计算统计数据
        double totalTurnover = executions.stream()
                .mapToDouble(e -> e.getPrice() * e.getQuantity())
                .sum();
                
        double totalCommission = executions.stream()
                .mapToDouble(TradeExecution::getCommission)
                .sum();
                
        long totalTransactions = executions.size();
        
        // 按股票代码统计交易量
        Map<String, Long> volumeByStock = executions.stream()
                .collect(Collectors.groupingBy(
                        TradeExecution::getStockCode, 
                        Collectors.summingLong(TradeExecution::getQuantity)));
        
        // 组合统计结果
        Map<String, Object> result = new HashMap<>();
        result.put("totalTurnover", totalTurnover);
        result.put("totalCommission", totalCommission);
        result.put("totalTransactions", totalTransactions);
        result.put("volumeByStock", volumeByStock);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        
        return result;
    }
    
    /**
     * 获取交易报表
     * 
     * @param userId 用户ID
     * @param reportType 报表类型，如"profit-loss", "turnover", "commission"
     * @param period 统计周期，如"day", "week", "month", "year"
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报表数据
     */
    @ServiceLog
    public Map<String, Object> getTransactionReports(
            Long userId, 
            String reportType, 
            String period, 
            String startDateStr, 
            String endDateStr) {
        
        // 解析日期
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        
        if (startDateStr != null && !startDateStr.isEmpty()) {
            startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ISO_DATE).atStartOfDay();
        } else {
            // 默认统计最近一个月
            startDate = LocalDateTime.now().minusMonths(1);
        }
        
        if (endDateStr != null && !endDateStr.isEmpty()) {
            endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ISO_DATE).plusDays(1).atStartOfDay();
        } else {
            endDate = LocalDateTime.now();
        }
        
        // 获取指定时间范围内的所有交易
        List<TradeExecution> executions = tradeExecutionRepository.findBySecuritiesAccountIdAndExecutionTimeBetween(
                userId, startDate, endDate);
        
        // 根据报表类型生成不同的报表
        Map<String, Object> report = new HashMap<>();
        switch (reportType) {
            case "profit-loss":
                report = generateProfitLossReport(executions, period);
                break;
            case "turnover":
                report = generateTurnoverReport(executions, period);
                break;
            case "commission":
                report = generateCommissionReport(executions, period);
                break;
            default:
                throw new IllegalArgumentException("Unsupported report type: " + reportType);
        }
        
        return report;
    }
    
    /**
     * 导出交易记录
     * 
     * @param userId 用户ID
     * @param formatType 导出格式，如"csv", "excel", "pdf"
     * @param filters 过滤条件
     * @param response HTTP响应，用于文件下载
     */
    @ServiceLog
    public void exportTransactions(
            Long userId, 
            String formatType, 
            Map<String, String> filters, 
            HttpServletResponse response) {
        
        // 获取所有符合条件的交易记录（不分页）
        List<TradeExecution> executions = getFilteredExecutions(userId, filters);
        
        // 根据导出格式导出数据
        String filename = "transactions_" + LocalDate.now().toString();
        
        switch (formatType.toLowerCase()) {
            case "csv":
                CsvExportUtils.exportTransactionsToCsv(executions, response, filename);
                break;
            case "excel":
                ExcelExportUtils.exportTransactionsToExcel(executions, response, filename);
                break;
            case "pdf":
                PdfExportUtils.exportTransactionsToPdf(executions, response, filename);
                break;
            default:
                throw new IllegalArgumentException("Unsupported export format: " + formatType);
        }
    }
    
    /**
     * 根据过滤条件获取交易记录
     */
    private List<TradeExecution> getFilteredExecutions(Long userId, Map<String, String> filters) {
        // 从过滤条件中提取值
        String stockCode = filters.getOrDefault("stockCode", null);
        String startDateStr = filters.getOrDefault("startDate", null);
        String endDateStr = filters.getOrDefault("endDate", null);
        String tradeType = filters.getOrDefault("tradeType", null);
        
        // 解析日期
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        
        if (startDateStr != null && !startDateStr.isEmpty()) {
            startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ISO_DATE).atStartOfDay();
        }
        
        if (endDateStr != null && !endDateStr.isEmpty()) {
            endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ISO_DATE).plusDays(1).atStartOfDay();
        }
        
        // 执行查询并返回结果
        return tradeExecutionRepository.findAllByUserIdAndFilters(
                userId, stockCode, startDate, endDate, tradeType);
    }
    
    /**
     * 生成盈亏报表
     */
    private Map<String, Object> generateProfitLossReport(List<TradeExecution> executions, String period) {
        // 按照时间周期和股票代码分组计算盈亏
        Map<String, Object> report = new HashMap<>();
        // 实现盈亏计算逻辑...
        report.put("totalProfit", 0); // 替换为实际计算逻辑
        report.put("profitByPeriod", new HashMap<String, Double>());
        report.put("profitByStock", new HashMap<String, Double>());
        return report;
    }
    
    /**
     * 生成成交量报表
     */
    private Map<String, Object> generateTurnoverReport(List<TradeExecution> executions, String period) {
        // 按照时间周期和股票代码分组计算成交量
        Map<String, Object> report = new HashMap<>();
        // 实现成交量计算逻辑...
        report.put("totalTurnover", 0); // 替换为实际计算逻辑
        report.put("turnoverByPeriod", new HashMap<String, Double>());
        report.put("turnoverByStock", new HashMap<String, Double>());
        return report;
    }
    
    /**
     * 生成佣金报表
     */
    private Map<String, Object> generateCommissionReport(List<TradeExecution> executions, String period) {
        // 按照时间周期和股票代码分组计算佣金
        Map<String, Object> report = new HashMap<>();
        // 实现佣金计算逻辑...
        report.put("totalCommission", 0); // 替换为实际计算逻辑
        report.put("commissionByPeriod", new HashMap<String, Double>());
        report.put("commissionByStock", new HashMap<String, Double>());
        return report;
    }
}