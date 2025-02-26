package org.buaa.project.common.biz.user;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.HyperLogLogOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.buaa.project.common.consts.RedisCacheConstants.TODAY_PV_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.TODAY_UV_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.TOTAL_PV_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.TOTAL_UV_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.USER_SIGN;

/**
 * 系统计数过滤器
 */
@RequiredArgsConstructor
public class CountStatisticsFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        try {
            String ipAddress = servletRequest.getRemoteAddr();
            Long userID = UserContext.getUserId();

            LocalDate now = LocalDate.now();

            ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
            valueOps.increment(TOTAL_PV_KEY);
            String todayPvKey = TODAY_PV_KEY + now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            valueOps.increment(todayPvKey);

            HyperLogLogOperations<String, String> hyperLogLogOps = stringRedisTemplate.opsForHyperLogLog();
            hyperLogLogOps.add(TOTAL_UV_KEY, ipAddress);
            hyperLogLogOps.add(TODAY_UV_KEY + now.format(DateTimeFormatter.ofPattern("yyyyMMdd")), ipAddress);

            if (userID != null) {
                int dayOfMonth = now.getDayOfMonth();
                stringRedisTemplate.opsForValue().setBit(USER_SIGN + now.getYear() + "-" + now.getMonthValue(), (dayOfMonth - 1L) *  userID, true);
            }

            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }



}
