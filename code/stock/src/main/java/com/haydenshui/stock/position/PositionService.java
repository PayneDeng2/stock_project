package com.haydenshui.stock.position;

import org.springframework.stereotype.Service;

import com.haydenshui.stock.lib.annotation.Lock;
import com.haydenshui.stock.lib.annotation.LocalTcc;
import com.haydenshui.stock.lib.annotation.ServiceLog;
import com.haydenshui.stock.lib.dto.position.PositionTransactionalDTO;
import com.haydenshui.stock.lib.entity.position.Position;
import com.haydenshui.stock.lib.entity.position.PositionChangeLog;
import com.haydenshui.stock.lib.entity.tcc.TccContext;
import com.haydenshui.stock.lib.exception.ResourceInsufficientException;
import com.haydenshui.stock.lib.exception.ResourceNotFoundException;
import com.haydenshui.stock.utils.RedisUtils;
import java.util.List;

@Service
public class PositionService {

    private final PositionRepository repository;

    private final PositionChangeLogRepository positionChangeLogRepository;

    public PositionService(PositionRepository positionRepository, PositionChangeLogRepository positionChangeLogRepository) {
        this.repository = positionRepository;
        this.positionChangeLogRepository = positionChangeLogRepository;
    }

    @ServiceLog
    public Position getPositionById(Long id) {
        return repository.findById(id).orElseThrow(() -> new 
            ResourceNotFoundException("position", "[id: " + id.toString() + "]"));
    }

    @ServiceLog
    public List<Position> getPositionBySecuritiesAccountId(Long securitiesAccountId) {
        return repository.findBySecuritiesAccountId(securitiesAccountId);
    }

    @ServiceLog
    public Position getPositionBySecuritiesAccountIdAndStockCode(Long securitiesAccountId, String stockCode) {
        return repository.findBySecuritiesAccountIdAndStockCode(securitiesAccountId, stockCode).orElseThrow(() -> new 
            ResourceNotFoundException("position", "[securitiesAccountId: " + securitiesAccountId.toString() + ", stockCode: " + stockCode + "]"));
    }
    
    @Lock(lockKey = "LOCK:TCC:POSITION:{positionDTO.id}")
    @LocalTcc
    public void tradeUpdatePosition(TccContext context, PositionTransactionalDTO positionDTO) {
        String xid = context.getCtxXid();
        String freezeKey = "TCC:" + xid + ":position:" + positionDTO.getId() + ":freeze";
    
        if (RedisUtils.hasKey(freezeKey)) {
            return;
        }
    
        Position position = repository.findById(positionDTO.getId())
            .orElseThrow(() -> new ResourceNotFoundException("position", "[id: " + positionDTO.getId() + "]"));
    
        int freezeAmount = positionDTO.getQuantity();
        if (freezeAmount < 0 && position.getQuantity() + freezeAmount < 0) {
            throw new ResourceInsufficientException("position", positionDTO.getId().toString(), "Sell");
        }
    
        position.setFrozenQuantity(
            position.getFrozenQuantity() + 
            (positionDTO.getQuantity() < 0 ? freezeAmount : 0)
        );
        repository.save(position);
    
        context.put("positionId", position.getId());
        context.put("freezeAmount", freezeAmount);
        RedisUtils.set(freezeKey, String.valueOf(freezeAmount));
    }
    

    @Lock(lockKey = "LOCK:TCC:POSITION:{positionDTO.id}")
    @LocalTcc
    public boolean commitTradeUpdatePosition(TccContext context, PositionTransactionalDTO positionDTO) {
        String xid = context.getCtxXid();
        String freezeKey = "TCC:" + xid + ":position:" + positionDTO.getId() + ":freeze";
        String commitKey = "TCC:" + xid + ":position:" + positionDTO.getId() + ":commit";
    
        String commitStatus = RedisUtils.get(commitKey);
        if ("COMMITTED".equals(commitStatus)) {
            return true;
        }
    
        Position position = repository.findById(positionDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("position", "[id: " + positionDTO.getId() + "]"));
    
        position.setQuantity(position.getQuantity() + positionDTO.getQuantity());
        position.setFrozenQuantity(
            position.getFrozenQuantity() + 
            (positionDTO.getQuantity() < 0 ? positionDTO.getQuantity() : 0)
        );
        repository.save(position);

        PositionChangeLog positionChangeLog = PositionChangeLog.builder()
                .positionId(position.getId())
                .securitiesAccountId(positionDTO.getSecuritiesAccountId())
                .stockCode(positionDTO.getStockCode())
                .changeQuantity(positionDTO.getQuantity())
                .price(positionDTO.getTransactionalPrice())
                .reason("COMMIT")
                .build();
        positionChangeLogRepository.save(positionChangeLog);
    
        int frozenAmount = (int) context.get("freezeAmount") + positionDTO.getQuantity();
        context.put("freezeAmount", frozenAmount);
    
        if (frozenAmount == 0) {
            RedisUtils.delete(freezeKey);
            RedisUtils.set(commitKey, "COMMITTED");
            return true;
        } else {
            RedisUtils.set(commitKey, "PENDING");
            return false;
        }
    }
    

    
    @Lock(lockKey = "LOCK:TCC:POSITION:{positionDTO.id}")
    @LocalTcc
    public void rollbackTradeUpdatePosition(TccContext context, PositionTransactionalDTO positionDTO) {
        String xid = context.getCtxXid();
        String freezeKey = "TCC:" + xid + ":position:" + positionDTO.getId() + ":freeze";
        String cancelKey = "TCC:" + xid + ":position:" + positionDTO.getId() + ":cancel";
    
        if (RedisUtils.hasKey(cancelKey)) {
            return;
        }
    
        Position position = repository.findById(positionDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("position", "[id: " + positionDTO.getId() + "]"));
    
        int freezeAmount = context.get("freezeAmount", Integer.class);
        position.setFrozenQuantity(position.getFrozenQuantity() - freezeAmount);
        repository.save(position);

        PositionChangeLog positionChangeLog = PositionChangeLog.builder()
                .positionId(position.getId())
                .securitiesAccountId(positionDTO.getSecuritiesAccountId())
                .stockCode(positionDTO.getStockCode())
                .changeQuantity(freezeAmount)
                .price(positionDTO.getTransactionalPrice())
                .reason("CANCEL")
                .build();
        positionChangeLogRepository.save(positionChangeLog);
    
        RedisUtils.delete(freezeKey);
        RedisUtils.set(cancelKey, "CANCELED");
    }
    

    @ServiceLog
    public StopLossTakeProfitRule createStopLossTakeProfitRule(StopLossTakeProfitRule rule) {
        // TODO: 实现创建止盈止损规则的逻辑
        return stopLossTakeProfitRuleRepository.save(rule);
    }

    @ServiceLog
    public StopLossTakeProfitRule updateStopLossTakeProfitRule(Long id, StopLossTakeProfitRule updatedRule) {
        StopLossTakeProfitRule existingRule = stopLossTakeProfitRuleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("StopLossTakeProfitRule", "[id: " + id + "]"));
        existingRule.setThreshold(updatedRule.getThreshold());
        existingRule.setStatus(updatedRule.getStatus());
        return stopLossTakeProfitRuleRepository.save(existingRule);
    }

    @ServiceLog
    public void deleteStopLossTakeProfitRule(Long id) {
        if (!stopLossTakeProfitRuleRepository.existsById(id)) {
            throw new ResourceNotFoundException("StopLossTakeProfitRule", "[id: " + id + "]");
        }
        stopLossTakeProfitRuleRepository.deleteById(id);
    }

}
