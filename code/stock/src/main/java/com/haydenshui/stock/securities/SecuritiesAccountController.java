package com.haydenshui.stock.securities;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.haydenshui.stock.lib.annotation.LogDetails;
import com.haydenshui.stock.lib.dto.risk.RiskAssessmentDTO;
import com.haydenshui.stock.lib.dto.securities.SecuritiesAccountDTO;
import com.haydenshui.stock.lib.util.ApiResult;
import com.haydenshui.stock.utils.SecurityUtils;

import jakarta.servlet.http.HttpServletRequest;
<<<<<<< HEAD
@RestController
@RequestMapping("/api/account")
public class SecuritiesAccountController {

    private final SecuritiesAccountService securitiesAccountService;

    public SecuritiesAccountController(SecuritiesAccountService securitiesAccountService) {
        this.securitiesAccountService = securitiesAccountService;
    }

    @LogDetails 
    @PutMapping("/new")
    public ResponseEntity<?> createAccount(@RequestBody SecuritiesAccountDTO dto) {
        return ResponseEntity.ok(securitiesAccountService.createAccount(dto));
    } 

    @LogDetails
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestParam String type) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(securitiesAccountService.getAccountById(userId, type));
    }

    @LogDetails
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody SecuritiesAccountDTO updateDTO) {
        return ResponseEntity.ok(securitiesAccountService.updateAccount(updateDTO));
    }

    @LogDetails
    @PutMapping("/security")
    public ResponseEntity<?> updateSecuritySettings(@RequestBody SecuritiesAccountDTO securityDTO) {
        return ResponseEntity.ok(securitiesAccountService.updateAccount(securityDTO));
    }

=======
import jakarta.validation.Valid;

/**
 * 证券账户控制器，提供账户管理相关功能
 */
@RestController
@RequestMapping("/api/accounts")
public class SecuritiesAccountController {

    private final IndividualSecuritiesAccountService individualSecuritiesAccountService;
    private final CorporateSecuritiesAccountService corporateSecuritiesAccountService;

    public SecuritiesAccountController(
            IndividualSecuritiesAccountService individualSecuritiesAccountService,
            CorporateSecuritiesAccountService corporateSecuritiesAccountService) {
        this.individualSecuritiesAccountService = individualSecuritiesAccountService;
        this.corporateSecuritiesAccountService = corporateSecuritiesAccountService;
    }

    /**
     * 获取账户信息
     * 
     * @param id 账户ID
     * @return 账户信息
     */
    @GetMapping("/{id}")
    @LogDetails
    @PreAuthorize("#id == authentication.principal")
    public ResponseEntity<ApiResult<?>> getAccountById(@PathVariable Long id) {
        // 通过ID判断是个人账户还是企业账户
        if (individualSecuritiesAccountService.existsById(id)) {
            return ResponseEntity.ok(ApiResult.success("获取个人账户成功", 
                    individualSecuritiesAccountService.getAccountById(id)));
        } else if (corporateSecuritiesAccountService.existsById(id)) {
            return ResponseEntity.ok(ApiResult.success("获取企业账户成功", 
                    corporateSecuritiesAccountService.getAccountById(id)));
        } else {
            return ResponseEntity.ok(ApiResult.failure("账户不存在", null));
        }
    }

    /**
     * 更新账户信息
     * 
     * @param id 账户ID
     * @param updateDTO 更新信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @LogDetails
    @PreAuthorize("#id == authentication.principal")
    public ResponseEntity<ApiResult<?>> updateAccount(
            @PathVariable Long id, 
            @Valid @RequestBody SecuritiesAccountDTO updateDTO) {
        updateDTO.setId(id);
        
        if (updateDTO instanceof IndividualSecuritiesAccountDTO) {
            return ResponseEntity.ok(ApiResult.success("更新个人账户成功", 
                    individualSecuritiesAccountService.updateAccount((IndividualSecuritiesAccountDTO) updateDTO)));
        } else if (updateDTO instanceof CorporateSecuritiesAccountDTO) {
            return ResponseEntity.ok(ApiResult.success("更新企业账户成功", 
                    corporateSecuritiesAccountService.updateAccount((CorporateSecuritiesAccountDTO) updateDTO)));
        } else {
            return ResponseEntity.ok(ApiResult.failure("未知账户类型", null));
        }
    }

    /**
     * 获取账户状态
     * 
     * @param id 账户ID
     * @return 账户状态
     */
    @GetMapping("/{id}/status")
    @LogDetails
    @PreAuthorize("#id == authentication.principal")
    public ResponseEntity<ApiResult<?>> getAccountStatus(@PathVariable Long id) {
        // 通过ID判断是个人账户还是企业账户
        if (individualSecuritiesAccountService.existsById(id)) {
            return ResponseEntity.ok(ApiResult.success("获取账户状态成功", 
                    individualSecuritiesAccountService.getAccountStatus(id)));
        } else if (corporateSecuritiesAccountService.existsById(id)) {
            return ResponseEntity.ok(ApiResult.success("获取账户状态成功", 
                    corporateSecuritiesAccountService.getAccountStatus(id)));
        } else {
            return ResponseEntity.ok(ApiResult.failure("账户不存在", null));
        }
    }

    /**
     * 更新安全设置
     * 
     * @param id 账户ID
     * @param securityDTO 安全设置
     * @return 更新结果
     */
    @PutMapping("/{id}/security")
    @LogDetails
    @PreAuthorize("#id == authentication.principal")
    public ResponseEntity<ApiResult<?>> updateSecuritySettings(
            @PathVariable Long id, 
            @Valid @RequestBody SecuritiesAccountDTO securityDTO) {
        securityDTO.setId(id);
        
        if (securityDTO instanceof IndividualSecuritiesAccountDTO) {
            return ResponseEntity.ok(ApiResult.success("更新安全设置成功", 
                    individualSecuritiesAccountService.updateSecuritySettings((IndividualSecuritiesAccountDTO) securityDTO)));
        } else if (securityDTO instanceof CorporateSecuritiesAccountDTO) {
            return ResponseEntity.ok(ApiResult.success("更新安全设置成功", 
                    corporateSecuritiesAccountService.updateSecuritySettings((CorporateSecuritiesAccountDTO) securityDTO)));
        } else {
            return ResponseEntity.ok(ApiResult.failure("未知账户类型", null));
        }
    }
    
    /**
     * 获取风险评估
     * 
     * @param id 账户ID
     * @return 风险评估信息
     */
    @GetMapping("/{id}/risk-assessment")
    @LogDetails
    @PreAuthorize("#id == authentication.principal")
    public ResponseEntity<ApiResult<?>> getRiskAssessment(@PathVariable Long id) {
        // 通过ID判断是个人账户还是企业账户
        if (individualSecuritiesAccountService.existsById(id)) {
            return ResponseEntity.ok(ApiResult.success("获取风险评估成功", 
                    individualSecuritiesAccountService.getRiskAssessment(id)));
        } else if (corporateSecuritiesAccountService.existsById(id)) {
            return ResponseEntity.ok(ApiResult.success("获取风险评估成功", 
                    corporateSecuritiesAccountService.getRiskAssessment(id)));
        } else {
            return ResponseEntity.ok(ApiResult.failure("账户不存在", null));
        }
    }

    /**
     * 创建个人账户
     * 
     * @param accountDTO 账户信息
     * @return 创建结果
     */
    @PostMapping("/individual")
>>>>>>> 13c6d9d36c826dd91c3f04d952de90f7b349efbe
    @LogDetails
    public ResponseEntity<ApiResult<?>> createIndividualAccount(
            @Valid @RequestBody IndividualSecuritiesAccountDTO accountDTO) {
        return ResponseEntity.ok(ApiResult.success("创建个人账户成功", 
                individualSecuritiesAccountService.createAccount(accountDTO)));
    }

<<<<<<< HEAD
=======
    /**
     * 创建企业账户
     * 
     * @param accountDTO 账户信息
     * @return 创建结果
     */
    @PostMapping("/corporate")
>>>>>>> 13c6d9d36c826dd91c3f04d952de90f7b349efbe
    @LogDetails
    public ResponseEntity<ApiResult<?>> createCorporateAccount(
            @Valid @RequestBody CorporateSecuritiesAccountDTO accountDTO) {
        return ResponseEntity.ok(ApiResult.success("创建企业账户成功", 
                corporateSecuritiesAccountService.createAccount(accountDTO)));
    }
<<<<<<< HEAD
}
=======

    /**
     * 更新账户状态
     * 
     * @param id 账户ID
     * @param status 新状态
     * @param reason 原因（可选）
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    @LogDetails
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
    public ResponseEntity<ApiResult<?>> updateAccountStatus(
            @PathVariable Long id, 
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        // 判断账户类型
        if (individualSecuritiesAccountService.existsById(id)) {
            return ResponseEntity.ok(ApiResult.success("更新账户状态成功", 
                    individualSecuritiesAccountService.updateAccountStatus(id, status, reason)));
        } else if (corporateSecuritiesAccountService.existsById(id)) {
            return ResponseEntity.ok(ApiResult.success("更新账户状态成功", 
                    corporateSecuritiesAccountService.updateAccountStatus(id, status, reason)));
        } else {
            return ResponseEntity.ok(ApiResult.failure("账户不存在", null));
        }
    }
}
>>>>>>> 13c6d9d36c826dd91c3f04d952de90f7b349efbe
