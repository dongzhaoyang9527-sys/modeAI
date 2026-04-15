package com.modeai.controller.admin;

import com.modeai.common.dto.Result;
import com.modeai.core.domain.entity.TokenUsage;
import com.modeai.core.domain.repository.TokenUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/monitor")
@RequiredArgsConstructor
public class AdminMonitorController {

    private final TokenUsageRepository tokenUsageRepository;

    @GetMapping("/token-usage")
    public Result<List<TokenUsage>> getTokenUsage(
            @RequestParam(defaultValue = "1") Long userId) {
        return Result.success(tokenUsageRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @GetMapping("/token-usage/summary")
    public Result<Map<String, Object>> getTokenUsageSummary(
            @RequestParam(defaultValue = "7") int days) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(days);

        Long totalTokens = tokenUsageRepository.sumTokensByDateRange(start, end);
        if (totalTokens == null) totalTokens = 0L;

        return Result.success(Map.of(
                "periodDays", days,
                "totalTokens", totalTokens,
                "avgDailyTokens", totalTokens / Math.max(days, 1)
        ));
    }

    @GetMapping("/system-info")
    public Result<Map<String, Object>> getSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> info = Map.of(
                "javaVersion", System.getProperty("java.version"),
                "osName", System.getProperty("os.name"),
                "availableProcessors", runtime.availableProcessors(),
                "maxMemory", runtime.maxMemory() / (1024 * 1024) + "MB",
                "usedMemory", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024) + "MB",
                "freeMemory", runtime.freeMemory() / (1024 * 1024) + "MB"
        );
        return Result.success(info);
    }
}
