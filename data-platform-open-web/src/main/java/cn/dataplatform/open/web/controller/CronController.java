package cn.dataplatform.open.web.controller;


import cn.dataplatform.open.common.util.CronUtils;
import cn.dataplatform.open.common.vo.base.Param;
import cn.dataplatform.open.common.vo.base.PlainResult;
import cn.dataplatform.open.web.util.DateUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2025/5/21
 * @since 1.0.0
 */
@RestController
@RequestMapping(path = "cron")
public class CronController {

    /**
     * cron表达式校验
     *
     * @param param cron表达式
     * @return 是否有效
     */
    @PostMapping("/valid")
    public PlainResult<Boolean> valid(@RequestBody @Valid Param<String> param) {
        return new PlainResult<>(CronUtils.isValid(param.getParam()));
    }

    /**
     * 下几次执行时间
     *
     * @param param cron表达式
     * @return 下次执行时间
     */
    @PostMapping("/nexts")
    public PlainResult<List<String>> nexts(@RequestBody @Valid Param<String> param) {
        return new PlainResult<>(CronUtils.nextExecutionTime(param.getParam(), ZonedDateTime.now(), 5)
                .stream().map(m -> m.format(DateUtil.FULL_DATE_TIME_FORMATTER)).toList());
    }

}
