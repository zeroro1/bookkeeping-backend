package com.bookkeeping.controller;

import com.bookkeeping.common.Result;
import com.bookkeeping.dto.AccountDTO;
import com.bookkeeping.entity.Account;
import com.bookkeeping.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public Result<Void> addAccount(HttpServletRequest request,
                                   @Valid @RequestBody AccountDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        return accountService.addAccount(userId, dto);
    }

    @PutMapping("/{id}")
    public Result<Void> updateAccount(HttpServletRequest request,
                                      @PathVariable Long id,
                                      @Valid @RequestBody AccountDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        return accountService.updateAccount(userId, id, dto);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAccount(HttpServletRequest request,
                                      @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        return accountService.deleteAccount(userId, id);
    }

    @GetMapping("/list")
    public Result<List<Account>> getAccounts(HttpServletRequest request,
                                             @RequestParam(required = false) Integer type,
                                             @RequestParam(required = false) String month) {
        Long userId = (Long) request.getAttribute("userId");
        return accountService.getAccounts(userId, type, month);
    }

    @GetMapping("/{id}")
    public Result<Account> getAccount(HttpServletRequest request,
                                      @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        return accountService.getAccount(userId, id);
    }

    @GetMapping("/stats")
    public Result<List<Map<String, Object>>> getMonthlyStats(HttpServletRequest request,
                                                              @RequestParam int year) {
        Long userId = (Long) request.getAttribute("userId");
        return accountService.getMonthlyStats(userId, year);
    }
}
