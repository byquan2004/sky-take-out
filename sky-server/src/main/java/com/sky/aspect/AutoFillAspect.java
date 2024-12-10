package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import io.netty.util.internal.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义填充字段切面
 */
@Slf4j
@Aspect
@Component
public class AutoFillAspect {
    @Pointcut("execution(* com.sky.mapper.*.* (..))  && @annotation(autoFill)")
    public  void customPointCut(AutoFill autoFill) {};

    @Around("customPointCut(autoFill)")
    public Object autoFill(ProceedingJoinPoint joinPoint, AutoFill autoFill) throws Throwable {
        log.info("公共字段开始填充...");
        // 获取原始方法的第一个参数(对象)
        Object[] args = joinPoint.getArgs();
        if(ObjectUtils.isEmpty(args)) return null;
        Object obj = args[0];

        // 反射获取目标方法
        Method createTime = obj.getClass().getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
        Method updateTime = obj.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
        Method createUser = obj.getClass().getMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
        Method updateUser = obj.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

        if(autoFill.value().equals(OperationType.INSERT)) {
            createTime.invoke(obj, LocalDateTime.now());
            createUser.invoke(obj, BaseContext.getCurrentId());
        }
        updateTime.invoke(obj, LocalDateTime.now());
        updateUser.invoke(obj, BaseContext.getCurrentId());
        return joinPoint.proceed();
    }
}
