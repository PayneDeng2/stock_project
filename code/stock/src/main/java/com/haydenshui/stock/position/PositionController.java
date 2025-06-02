package com.haydenshui.stock.position;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.haydenshui.stock.lib.annotation.LogDetails;
import com.haydenshui.stock.lib.dto.position.StopLossTakeProfitRuleDTO;
import com.haydenshui.stock.lib.entity.position.StopLossTakeProfitRule;
import com.haydenshui.stock.lib.util.ApiResult;
import com.haydenshui.stock.utils.SecurityUtils;

import jakarta.validation.Valid;

/**
 * 持仓管理控制器，提供持仓查询、分析和规则设置等功能
 */
@RestController
@RequestMapping("/api/positions")
public class PositionController {
    
    private final PositionService positionService;
    private final PositionChangeLogService positionChangeLogService;

    public PositionController(PositionService positionService, PositionChangeLogService positionChangeLogService) {
        this.positionService = positionService;
        this.positionChangeLogService = positionChangeLogService;
    }

    /**
     * 获取用户持仓列表
     * @return 用户持仓列表
     */
    @GetMapping
    @LogDetails
<<<<<<< HEAD
    public ResponseEntity<?> getPositionById(@RequestParam(required = false) String id) {
        if (id != null) {
            return ResponseEntity.ok(positionService.getPositionById(Long.parseLong(id)));
        }
=======
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> getUserPositions() {
>>>>>>> 13c6d9d36c826dd91c3f04d952de90f7b349efbe
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResult.success("获取持仓列表成功", 
                positionService.getPositionBySecuritiesAccountId(userId)));
    }
<<<<<<< HEAD
=======
    
    /**
     * 获取单个持仓详情
     * @param id 持仓ID
     * @return 持仓详情
     */
    @GetMapping("/{id}")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> getPositionById(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        // 验证持仓是否属于当前用户
        positionService.validatePositionOwnership(id, userId);
        return ResponseEntity.ok(ApiResult.success("获取持仓详情成功", 
                positionService.getPositionById(id)));
    }
>>>>>>> 13c6d9d36c826dd91c3f04d952de90f7b349efbe

    /**
     * 获取持仓分析
     * @return 持仓分析数据
     */
    @GetMapping("/analysis")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> getPositionAnalysis() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResult.success("获取持仓分析成功", 
                positionService.getPositionAnalysis(userId)));
    }
    
    /**
     * 获取历史持仓变动
     * @return 历史持仓变动记录
     */
    @GetMapping("/history")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> getPositionHistory() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResult.success("获取持仓历史成功", 
                positionChangeLogService.getLogsBySecuritiesAccountId(userId)));
    }

    /**
     * 设置止损规则
     * @param id 持仓ID
     * @param ruleDTO 止损规则
     * @return 创建结果
     */
    @PostMapping("/{id}/stop-loss")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> createStopLossRule(
            @PathVariable Long id, 
            @Valid @RequestBody StopLossTakeProfitRuleDTO ruleDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        // 验证持仓是否属于当前用户
        positionService.validatePositionOwnership(id, userId);
        
        ruleDTO.setPositionId(id);
        ruleDTO.setType("STOP_LOSS");
        return ResponseEntity.ok(ApiResult.success("设置止损成功", 
                positionService.createStopLossTakeProfitRule(ruleDTO)));
    }

    /**
     * 设置止盈规则
     * @param id 持仓ID
     * @param ruleDTO 止盈规则
     * @return 创建结果
     */
    @PostMapping("/{id}/take-profit")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> createTakeProfitRule(
            @PathVariable Long id, 
            @Valid @RequestBody StopLossTakeProfitRuleDTO ruleDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        // 验证持仓是否属于当前用户
        positionService.validatePositionOwnership(id, userId);
        
        ruleDTO.setPositionId(id);
        ruleDTO.setType("TAKE_PROFIT");
        return ResponseEntity.ok(ApiResult.success("设置止盈成功", 
                positionService.createStopLossTakeProfitRule(ruleDTO)));
    }

    /**
     * 更新规则
     * @param id 规则ID
     * @param ruleDTO 规则更新信息
     * @return 更新结果
     */
    @PutMapping("/rules/{id}")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> updateRule(
            @PathVariable Long id, 
            @Valid @RequestBody StopLossTakeProfitRuleDTO ruleDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        ruleDTO.setId(id);
        
        return ResponseEntity.ok(ApiResult.success("更新规则成功", 
                positionService.updateStopLossTakeProfitRule(ruleDTO, userId)));
    }

    /**
     * 删除规则
     * @param id 规则ID
     * @return 删除结果
     */
    @DeleteMapping("/rules/{id}")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> deleteRule(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        positionService.deleteStopLossTakeProfitRule(id, userId);
        return ResponseEntity.ok(ApiResult.success("删除规则成功", null));
    }
}
