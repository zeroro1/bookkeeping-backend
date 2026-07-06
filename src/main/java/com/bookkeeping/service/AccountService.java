package com.bookkeeping.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bookkeeping.common.Result;
import com.bookkeeping.dto.AccountDTO;
import com.bookkeeping.entity.Account;
import com.bookkeeping.entity.User;
import com.bookkeeping.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountService extends ServiceImpl<AccountMapper, Account> {

    /** 新增账目 */
    public Result<Void> addAccount(Long userId, AccountDTO dto) {
        Account account = new Account();
        account.setUserId(userId);
        account.setType(dto.getType());
        account.setAmount(dto.getAmount());
        account.setFromAccount(StringUtils.hasText(dto.getFromAccount()) ? dto.getFromAccount() : "");
        account.setToAccount(StringUtils.hasText(dto.getToAccount()) ? dto.getToAccount() : "");
        account.setCategory(StringUtils.hasText(dto.getCategory()) ? dto.getCategory() : "");
        account.setRemark(StringUtils.hasText(dto.getRemark()) ? dto.getRemark() : "");
        account.setDate(dto.getDate() != null ? dto.getDate() : LocalDate.now());
        save(account);
        return Result.success();
    }

    /** 更新账目 */
    public Result<Void> updateAccount(Long userId, Long id, AccountDTO dto) {
        Account account = getById(id);
        if (account == null || !account.getUserId().equals(userId)) {
            return Result.error(403, "无权操作");
        }
        account.setType(dto.getType());
        account.setAmount(dto.getAmount());
        account.setFromAccount(StringUtils.hasText(dto.getFromAccount()) ? dto.getFromAccount() : "");
        account.setToAccount(StringUtils.hasText(dto.getToAccount()) ? dto.getToAccount() : "");
        account.setCategory(StringUtils.hasText(dto.getCategory()) ? dto.getCategory() : "");
        account.setRemark(StringUtils.hasText(dto.getRemark()) ? dto.getRemark() : "");
        account.setDate(dto.getDate() != null ? dto.getDate() : account.getDate());
        updateById(account);
        return Result.success();
    }

    /** 删除账目 */
    public Result<Void> deleteAccount(Long userId, Long id) {
        Account account = getById(id);
        if (account == null || !account.getUserId().equals(userId)) {
            return Result.error(403, "无权操作");
        }
        removeById(id);
        return Result.success();
    }

    /** 查询账目列表 */
    public Result<List<Account>> getAccounts(Long userId, Integer type, String month) {
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Account::getUserId, userId)
               .orderByDesc(Account::getDate)
               .orderByDesc(Account::getCreateTime);

        if (type != null) {
            wrapper.eq(Account::getType, type);
        }
        if (StringUtils.hasText(month)) {
            try {
                YearMonth ym = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
                wrapper.ge(Account::getDate, ym.atDay(1))
                       .le(Account::getDate, ym.atEndOfMonth());
            } catch (Exception e) {
                // ignore invalid month format
            }
        }

        List<Account> list = list(wrapper);
        return Result.success(list);
    }

    /** 获取单条账目 */
    public Result<Account> getAccount(Long userId, Long id) {
        Account account = getById(id);
        if (account == null || !account.getUserId().equals(userId)) {
            return Result.error(403, "无权查看");
        }
        return Result.success(account);
    }

    /** 月度统计 */
    public Result<List<Map<String, Object>>> getMonthlyStats(Long userId, int year) {
        List<Map<String, Object>> stats = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            Map<String, Object> monthStat = new HashMap<>();
            monthStat.put("month", String.format("%02d", m));

            LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Account::getUserId, userId)
                   .apply("YEAR(date) = {0}", year)
                   .apply("MONTH(date) = {0}", m);

            List<Account> expenses = wrapper.clone();
            expenses.eq(Account::getType, 2); // 支出
            BigDecimal expenseTotal = expenses.list().stream()
                    .map(Account::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthStat.put("expense", expenseTotal);

            List<Account> incomes = wrapper.clone();
            incomes.eq(Account::getType, 1); // 收入
            BigDecimal incomeTotal = incomes.list().stream()
                    .map(Account::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthStat.put("income", incomeTotal);

            stats.add(monthStat);
        }
        return Result.success(stats);
    }
}
