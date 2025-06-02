package com.haydenshui.stock.capital.strategy;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.haydenshui.stock.capital.CapitalAccountRepository;
<<<<<<< HEAD
import com.haydenshui.stock.lib.dto.CheckDTO;
import com.haydenshui.stock.lib.dto.capital.CapitalAccountDTO;
=======
import com.haydenshui.stock.lib.dto.capital.CapitalAccountDTO;
import com.haydenshui.stock.lib.dto.capital.CapitalCheckDTO;
>>>>>>> 13c6d9d36c826dd91c3f04d952de90f7b349efbe
import com.haydenshui.stock.lib.dto.capital.CapitalMapper;
import com.haydenshui.stock.lib.entity.account.CapitalAccount;
import com.haydenshui.stock.lib.exception.ResourceAlreadyExistsException;
import com.haydenshui.stock.lib.exception.ResourceNotFoundException;
import com.haydenshui.stock.utils.BeanCopyUtils;

public abstract class AbstractCapitalAccountStrategy implements CapitalAccountStrategy {

    protected final CapitalAccountRepository capitalAccountRepository;

    protected final PasswordEncoder passwordEncoder;

    public AbstractCapitalAccountStrategy(CapitalAccountRepository capitalAccountRepository, PasswordEncoder passwordEncoder) {
        this.capitalAccountRepository = capitalAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public CapitalAccount createAccount(CapitalAccountDTO dto) {
        Optional<CapitalAccount> existingAccount = capitalAccountRepository.findByCapitalAccountNumber(dto.getCapitalAccountNumber());
        existingAccount.ifPresent(acc -> {
<<<<<<< HEAD
            throw new ResourceAlreadyExistsException("Capital", "[AccountNumber: " + acc.getCapitalAccountNumber() + "]");
=======
            throw new ResourceAlreadyExistsException("Capital", acc.getCapitalAccountNumber());
>>>>>>> 13c6d9d36c826dd91c3f04d952de90f7b349efbe
        });
        CapitalAccount account  = CapitalMapper.toEntity(dto);
        return capitalAccountRepository.save(account);
    }

    @Override
    public CapitalAccount getAccountById(Long id) {
        return capitalAccountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Capital", "[id: " + id.toString() + "]"));
    }

    @Override
    public CapitalAccount getAccountByAccountNumber(String accountNumber) {
        return capitalAccountRepository.findByCapitalAccountNumber(accountNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Capital", "[accountNumber: " + accountNumber + "]"));
    }

    @Override
    @Transactional
    public CapitalAccount updateAccount(CapitalAccountDTO dto) {
        Optional<CapitalAccount> existingAccount = capitalAccountRepository.findById(dto.getId());
        if (existingAccount.isEmpty()) {
            throw new ResourceNotFoundException("Capital", "[id: " + dto.getId().toString() + "]");
        }
        CapitalAccount existing = existingAccount.get();
        CapitalAccount patch = CapitalMapper.toEntity(dto);
        BeanCopyUtils.copyNonNullProperties(patch, existing);
        existing.setPassword(passwordEncoder.encode(existing.getPassword()));
        return capitalAccountRepository.save(existing);
    }

    @Override
<<<<<<< HEAD
    public boolean disableValidity(CheckDTO dto) {
=======
    public boolean disableValidity(CapitalCheckDTO dto) {
>>>>>>> 13c6d9d36c826dd91c3f04d952de90f7b349efbe
        Optional<CapitalAccount> existingAccount = capitalAccountRepository.findById(dto.getCapitalAccountId());
        if (existingAccount.isEmpty()) {
            throw new ResourceNotFoundException("Capital", "[id: " + dto.getCapitalAccountId().toString() + "]");
        }
        CapitalAccount existing = existingAccount.get();
        return existing.getFrozenBalance().compareTo(BigDecimal.ZERO) == 0;
    }

<<<<<<< HEAD
    @Override
    public CapitalAccount disableAccount(CapitalAccountDTO dto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'disableAccount'");
    }

    @Override
    public CapitalAccount restoreAccount(CapitalAccountDTO dto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'restoreAccount'");
    }

    @Override
    public CapitalAccount reportAccountLoss(CapitalAccountDTO dto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'reportAccountLoss'");
    }

=======
>>>>>>> 13c6d9d36c826dd91c3f04d952de90f7b349efbe
    
}
