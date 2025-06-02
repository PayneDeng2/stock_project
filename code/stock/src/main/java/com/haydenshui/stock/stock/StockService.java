package com.haydenshui.stock.stock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.haydenshui.stock.constants.RocketMQConstants;
import com.haydenshui.stock.lib.annotation.ServiceLog;
import com.haydenshui.stock.lib.dto.stock.StockDTO;
import com.haydenshui.stock.lib.dto.stock.StockPageDTO;
import com.haydenshui.stock.lib.dto.stock.StockPriceDTO;
import com.haydenshui.stock.lib.entity.stock.Stock;
import com.haydenshui.stock.lib.entity.stock.StockPrice;
import com.haydenshui.stock.lib.exception.ResourceNotFoundException;
import com.haydenshui.stock.utils.DateTimeUtils;
import com.haydenshui.stock.utils.RocketMQUtils;

/**
 * 股票服务，提供股票基本信息和行情相关功能
 */
@Service
public class StockService {
    
    @Autowired
    private StockRepository stockRepository;
    
    @Autowired
    private StockPriceRepository stockPriceRepository;

    /**
     * 获取所有股票（支持分页和排序）
     * 
     * @param page 页码
     * @param size 每页大小
     * @param sortBy 排序字段
     * @param sortDirection 排序方向
     * @return 分页的股票列表
     */
    @ServiceLog
    public StockPageDTO getAllStocks(int page, int size, String sortBy, String sortDirection) {
        Pageable pageable = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        
        Page<Stock> stockPage = stockRepository.findAll(pageable);
        
        return new StockPageDTO(
                stockPage.getContent(),
                stockPage.getTotalElements(),
                stockPage.getTotalPages(),
                stockPage.getNumber(),
                stockPage.getSize()
        );
    }

    /**
     * 根据股票代码获取股票信息
     * 
     * @param stockCode 股票代码
     * @return 股票信息
     */
    @ServiceLog
    public Stock getStockByCode(String stockCode) {
        return stockRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new ResourceNotFoundException("stock", "[code: " + stockCode + "]"));
    }

    /**
     * 按名称搜索股票
     * 
     * @param name 股票名称（模糊搜索）
     * @param page 页码
     * @param size 每页大小
     * @return 符合条件的股票列表
     */
    @ServiceLog
    public StockPageDTO searchStocksByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Stock> stockPage = stockRepository.findByStockNameContaining(name, pageable);
        
        return new StockPageDTO(
                stockPage.getContent(),
                stockPage.getTotalElements(),
                stockPage.getTotalPages(),
                stockPage.getNumber(),
                stockPage.getSize()
        );
    }

    /**
     * 获取股票当前价格
     * 
     * @param stockCode 股票代码
     * @return 当前价格
     */
    @ServiceLog
    public StockPrice getCurrentPrice(String stockCode) {
        return stockPriceRepository.findLatestByStockCode(stockCode)
                .orElseThrow(() -> new ResourceNotFoundException("stock price", "[code: " + stockCode + "]"));
    }

    /**
     * 获取股票历史价格
     * 
     * @param stockCode 股票代码
     * @param from 开始时间
     * @param to 结束时间
     * @return 价格列表
     */
    @ServiceLog
    public List<StockPrice> getHistoricalPrices(String stockCode, LocalDateTime from, LocalDateTime to) {
        return stockPriceRepository.findByStockCodeAndTimeBetween(stockCode, from, to);
    }

    /**
     * 获取热门股票
     * 
     * @param limit 数量限制
     * @return 热门股票列表
     */
    @ServiceLog
    public List<Stock> getPopularStocks(int limit) {
        return stockRepository.findPopularStocks(limit);
    }

    /**
     * 获取指数列表
     * 
     * @return 指数列表
     */
    @ServiceLog
    public List<Stock> getIndices() {
        return stockRepository.findByIsIndex(true);
    }

    /**
     * 更新股票价格
     * 
     * @param stockCode 股票代码
     * @param price 新价格
     * @return 更新后的价格记录
     */
    @Transactional
    public StockPrice updateStockPrice(String stockCode, double price) {
        // 查找股票
        Stock stock = stockRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new ResourceNotFoundException("stock", "[code: " + stockCode + "]"));
        
        // 更新价格
        StockPrice stockPrice = new StockPrice();
        stockPrice.setStockId(stock.getId());
        stockPrice.setStockCode(stockCode);
        stockPrice.setPrice(price);
        stockPrice.setTime(LocalDateTime.now());
        
        StockPrice savedPrice = stockPriceRepository.save(stockPrice);
        
        // 更新股票最新价格
        stock.setCurrentPrice(price);
        if (price > stock.getHighPrice()) {
            stock.setHighPrice(price);
        }
        if (price < stock.getLowPrice()) {
            stock.setLowPrice(price);
        }
        stockRepository.save(stock);
        
        // 发送价格变动消息
        notifyPriceChange(stockCode, price);
        
        return savedPrice;
    }

    /**
     * 更新股票开盘价
     * 
     * @param stockCode 股票代码
     * @param price 开盘价
     * @return 更新后的股票信息
     */
    @Transactional
    public Stock updateOpeningPrice(String stockCode, double price) {
        Stock stock = stockRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new ResourceNotFoundException("stock", "[code: " + stockCode + "]"));
        
        stock.setOpenPrice(price);
        stock.setCurrentPrice(price);
        stock.setHighPrice(price);
        stock.setLowPrice(price);
        
        // 记录开盘价
        StockPrice stockPrice = new StockPrice();
        stockPrice.setStockId(stock.getId());
        stockPrice.setStockCode(stockCode);
        stockPrice.setPrice(price);
        stockPrice.setTime(LocalDateTime.now());
        stockPriceRepository.save(stockPrice);
        
        return stockRepository.save(stock);
    }

    /**
     * 更新股票收盘价
     * 
     * @param stockCode 股票代码
     * @param price 收盘价
     * @return 更新后的股票信息
     */
    @Transactional
    public Stock updateClosingPrice(String stockCode, double price) {
        Stock stock = stockRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new ResourceNotFoundException("stock", "[code: " + stockCode + "]"));
        
        stock.setPreviousClosePrice(stock.getClosePrice());
        stock.setClosePrice(price);
        
        // 记录收盘价
        StockPrice stockPrice = new StockPrice();
        stockPrice.setStockId(stock.getId());
        stockPrice.setStockCode(stockCode);
        stockPrice.setPrice(price);
        stockPrice.setTime(LocalDateTime.now());
        stockPriceRepository.save(stockPrice);
        
        return stockRepository.save(stock);
    }

    /**
     * 添加股票
     * 
     * @param stockDTO 股票信息
     * @return 添加的股票
     */
    @Transactional
    public Stock addStock(StockDTO stockDTO) {
        Stock stock = new Stock();
        stock.setStockCode(stockDTO.getStockCode());
        stock.setStockName(stockDTO.getStockName());
        stock.setCurrentPrice(stockDTO.getCurrentPrice());
        stock.setOpenPrice(stockDTO.getOpenPrice());
        stock.setClosePrice(stockDTO.getClosePrice());
        stock.setHighPrice(stockDTO.getHighPrice());
        stock.setLowPrice(stockDTO.getLowPrice());
        stock.setPreviousClosePrice(stockDTO.getPreviousClosePrice());
        stock.setVolume(stockDTO.getVolume());
        stock.setTurnover(stockDTO.getTurnover());
        stock.setIsIndex(stockDTO.getIsIndex());
        
        return stockRepository.save(stock);
    }

    /**
     * 发送价格变动通知
     */
    private void notifyPriceChange(String stockCode, double price) {
        StockPriceDTO priceDTO = new StockPriceDTO();
        priceDTO.setStockCode(stockCode);
        priceDTO.setCurrentPrice(price);
        priceDTO.setTime(LocalDateTime.now());
        
        RocketMQUtils.sendMessage(
                "stock", 
                RocketMQConstants.TOPIC_STOCK_PRICE,
                RocketMQConstants.TAG_PRICE_UPDATE, 
                priceDTO);
    }
}