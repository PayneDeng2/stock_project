package com.haydenshui.stock.stock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.haydenshui.stock.lib.annotation.ServiceLog;
import com.haydenshui.stock.lib.dto.stock.StockAlertDTO;
import com.haydenshui.stock.lib.entity.stock.StockAlert;
import com.haydenshui.stock.lib.entity.stock.StockAlertStatus;
import com.haydenshui.stock.lib.entity.stock.StockAlertType;
import com.haydenshui.stock.lib.exception.ResourceNotFoundException;

/**
 * 股票价格提醒服务
 */
@Service
public class StockAlertService {
    
    @Autowired
    private StockAlertRepository alertRepository;
    
    @Autowired
    private StockService stockService;

    /**
     * 创建价格提醒
     * 
     * @param alertDTO 提醒信息
     * @return 创建的提醒
     */
    @Transactional
    @ServiceLog
    public StockAlert createAlert(StockAlertDTO alertDTO) {
        // 验证股票存在
        stockService.getStockByCode(alertDTO.getStockCode());
        
        StockAlert alert = new StockAlert();
        alert.setSecuritiesAccountId(alertDTO.getSecuritiesAccountId());
        alert.setStockCode(alertDTO.getStockCode());
        alert.setTargetPrice(alertDTO.getTargetPrice());
        alert.setAlertType(StockAlertType.fromString(alertDTO.getAlertType()));
        alert.setStatus(StockAlertStatus.ACTIVE);
        alert.setCreatedAt(LocalDateTime.now());
        
        return alertRepository.save(alert);
    }

    /**
     * 获取用户的所有价格提醒
     * 
     * @param securitiesAccountId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 符合条件的提醒列表
     */
    @ServiceLog
    public Page<StockAlert> getUserAlerts(Long securitiesAccountId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return alertRepository.findBySecuritiesAccountId(securitiesAccountId, pageable);
    }

    /**
     * 获取特定股票的所有提醒
     * 
     * @param stockCode 股票代码
     * @param page 页码
     * @param size 每页大小
     * @return 符合条件的提醒列表
     */
    @ServiceLog
    public Page<StockAlert> getStockAlerts(String stockCode, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return alertRepository.findByStockCode(stockCode, pageable);
    }

    /**
     * 更新提醒状态
     * 
     * @param alertId 提醒ID
     * @param status 新状态
     * @return 更新后的提醒
     */
    @Transactional
    @ServiceLog
    public StockAlert updateAlertStatus(String alertId, String status) {
        StockAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("stock alert", "[id: " + alertId + "]"));
        
        alert.setStatus(StockAlertStatus.fromString(status));
        return alertRepository.save(alert);
    }

    /**
     * 删除提醒
     * 
     * @param alertId 提醒ID
     */
    @Transactional
    @ServiceLog
    public void deleteAlert(String alertId) {
        if (!alertRepository.existsById(alertId)) {
            throw new ResourceNotFoundException("stock alert", "[id: " + alertId + "]");
        }
        alertRepository.deleteById(alertId);
    }

    /**
     * 检查并触发提醒
     * 
     * @param stockCode 股票代码
     * @param currentPrice 当前价格
     * @return 触发的提醒ID列表
     */
    @Transactional
    public List<String> checkAndTriggerAlerts(String stockCode, double currentPrice) {
        List<StockAlert> activeAlerts = alertRepository.findByStockCodeAndStatus(
                stockCode, StockAlertStatus.ACTIVE);
        
        List<String> triggeredAlerts = new ArrayList<>();
        
        for (StockAlert alert : activeAlerts) {
            boolean shouldTrigger = false;
            
            switch (alert.getAlertType()) {
                case PRICE_ABOVE:
                    shouldTrigger = currentPrice >= alert.getTargetPrice();
                    break;
                    
                case PRICE_BELOW:
                    shouldTrigger = currentPrice <= alert.getTargetPrice();
                    break;
                    
                case PRICE_CHANGE_PERCENT:
                    // 这里需要获取基准价格，例如前一天收盘价
                    Double basePrice = stockService.getStockByCode(stockCode).getPreviousClosePrice();
                    if (basePrice != null && basePrice > 0) {
                        double changePercent = (currentPrice - basePrice) / basePrice * 100;
                        shouldTrigger = Math.abs(changePercent) >= alert.getTargetPrice();
                    }
                    break;
            }
            
            if (shouldTrigger) {
                alert.setStatus(StockAlertStatus.TRIGGERED);
                alert.setTriggeredAt(LocalDateTime.now());
                alert.setTriggeredPrice(currentPrice);
                alertRepository.save(alert);
                
                triggeredAlerts.add(alert.getId());
            }
        }
        
        return triggeredAlerts;
    }
}