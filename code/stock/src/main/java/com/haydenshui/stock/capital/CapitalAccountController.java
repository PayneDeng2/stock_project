package com.haydenshui.stock.capital;

<<<<<<< HEAD
import org.springframework.http.ResponseEntity;
=======
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
>>>>>>> 13c6d9d36c826dd91c3f04d952de90f7b349efbe
import org.springframework.web.bind.annotation.*;

import com.haydenshui.stock.lib.annotation.LogDetails;
import com.haydenshui.stock.lib.dto.capital.CapitalAccountDTO;
<<<<<<< HEAD

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("api/capital")
public class CapitalAccountController{

    private final CapitalAccountService capitalAccountService;

    public CapitalAccountController(CapitalAccountService capitalAccountService) {
        this.capitalAccountService = capitalAccountService;
    }

    @LogDetails
    @PutMapping("/new")
    public ResponseEntity<?> createAccount(@RequestBody CapitalAccountDTO dto) {
        return ResponseEntity.ok(capitalAccountService.createAccount(dto));
    }

    @LogDetails
    @GetMapping("/profile")
    public ResponseEntity<?> getAccountById(@RequestParam Long id, @RequestParam String type) {
        return ResponseEntity.ok(capitalAccountService.getAccountById(id, type));
    }

    @LogDetails
    @PutMapping("/update")
    public ResponseEntity<?> updateAccount(@RequestBody CapitalAccountDTO dto) {
        return ResponseEntity.ok(capitalAccountService.updateAccount(dto));
    }
    
=======
import com.haydenshui.stock.lib.dto.capital.TransactionDTO;
import com.haydenshui.stock.lib.util.ApiResult;
import com.haydenshui.stock.utils.SecurityUtils;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 资金账户控制器，提供资金账户相关的API接口
 */
@RestController
@RequestMapping("/api/capital-accounts")
public class CapitalAccountController {

    @Autowired
    private CapitalAccountService capitalAccountService;

    /**
     * 创建资金账户
     * 
     * @param accountDTO 账户信息
     * @return 创建结果
     */
    @PostMapping
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> createCapitalAccount(
            @Valid @RequestBody CapitalAccountDTO accountDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        accountDTO.setSecuritiesAccountId(userId);
        return ResponseEntity.ok(
                ApiResult.success("创建资金账户成功", capitalAccountService.createCapitalAccount(accountDTO)));
    }

    /**
     * 获取资金账户信息
     * 
     * @param id 资金账户ID
     * @return 账户信息
     */
    @GetMapping("/{id}")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> getCapitalAccount(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity
                .ok(ApiResult.success("获取资金账户成功", capitalAccountService.getCapitalAccountByIdAndUser(id, userId)));
    }

    /**
     * 更新资金账户信息
     * 
     * @param id 资金账户ID
     * @param accountDTO 账户更新信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> updateCapitalAccount(
            @PathVariable Long id, 
            @Valid @RequestBody CapitalAccountDTO accountDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        accountDTO.setId(id);
        accountDTO.setSecuritiesAccountId(userId);
        return ResponseEntity
                .ok(ApiResult.success("更新资金账户成功", capitalAccountService.updateCapitalAccount(accountDTO)));
    }

    /**
     * 获取资金账户余额
     * 
     * @param id 资金账户ID
     * @return 账户余额
     */
    @GetMapping("/{id}/balance")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> getBalance(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity
                .ok(ApiResult.success("获取余额成功", capitalAccountService.getBalanceByIdAndUser(id, userId)));
    }

    /**
     * 充值
     * 
     * @param id 资金账户ID
     * @param amount 充值金额
     * @return 充值结果
     */
    @PostMapping("/{id}/deposit")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> deposit(
            @PathVariable Long id, 
            @RequestParam @NotNull @Positive Double amount) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity
                .ok(ApiResult.success("充值成功", capitalAccountService.deposit(id, userId, amount)));
    }

    /**
     * 提现
     * 
     * @param id 资金账户ID
     * @param amount 提现金额
     * @return 提现结果
     */
    @PostMapping("/{id}/withdraw")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> withdraw(
            @PathVariable Long id, 
            @RequestParam @NotNull @Positive Double amount) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity
                .ok(ApiResult.success("提现成功", capitalAccountService.withdraw(id, userId, amount)));
    }

    /**
     * 更新资金账户状态
     * 
     * @param id 资金账户ID
     * @param status 状态
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> updateStatus(
            @PathVariable Long id, 
            @RequestParam String status) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity
                .ok(ApiResult.success("更新状态成功", capitalAccountService.updateStatus(id, userId, status)));
    }

    /**
     * 获取资金流水
     * 
     * @param id 资金账户ID
     * @param page 页码
     * @param size 每页大小
     * @return 资金流水列表
     */
    @GetMapping("/{id}/transactions")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> getTransactions(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity
                .ok(ApiResult.success("获取资金流水成功", capitalAccountService.getTransactions(id, userId, page, size)));
    }

    /**
     * 获取资产统计分析
     * 
     * @param id 资金账户ID
     * @return 资产统计分析
     */
    @GetMapping("/{id}/statistics")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> getStatistics(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity
                .ok(ApiResult.success("获取资产统计成功", capitalAccountService.getStatistics(id, userId)));
    }

    /**
     * 冻结资金（分布式事务）
     * 
     * @param id 资金账户ID
     * @param amount 冻结金额
     * @param orderId 关联订单ID
     * @return 冻结结果
     */
    @PostMapping("/{id}/freeze")
    @LogDetails
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResult<?>> freezeFunds(
            @PathVariable Long id,
            @RequestParam @NotNull @Positive Double amount,
            @RequestParam Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity
                .ok(ApiResult.success("冻结资金成功", capitalAccountService.freezeFunds(id, userId, amount, orderId)));
    }
>>>>>>> 13c6d9d36c826dd91c3f04d952de90f7b349efbe
}