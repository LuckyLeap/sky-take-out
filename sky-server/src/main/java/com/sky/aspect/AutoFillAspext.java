package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自定义切面类，用于实现自动填充公共字段的处理逻辑
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspext {
    /**
     * 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {}

    /**
     * 前置通知,在目标方法执行前执行,用于实现自动填充公共字段的赋值
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段的自动填充...");
        // 获取到目标方法的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();// 获取到目标方法的签名信息
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);// 获取到目标方法上的注解对象
        OperationType operationType = autoFill.value();// 获取到目标方法上注解的赋值 - 数据库操作类型

        // 获取到目标方法上注解的参数 - 实体对象
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            Object entity = args[0];

            // 准备赋值数据
            LocalDateTime now = LocalDateTime.now();
            Long currentId = BaseContext.getCurrentId();

            // 根据当前目标方法的数据库操作类型，动态的为实体对象赋值
            if (operationType == OperationType.INSERT) {
                // 插入操作，为实体对象中的以下属性赋值
                try {
                    // 赋值操作时间
                    entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class).invoke(entity, now);
                    entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class).invoke(entity, now);
                    // 赋值操作人
                    entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class).invoke(entity, currentId);
                    entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class).invoke(entity, currentId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (operationType == OperationType.UPDATE) {
                // 更新操作，为实体对象中的以下属性赋值
                try {
                    // 赋值操作时间
                    entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class).invoke(entity, now);
                    // 赋值操作人
                    entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class).invoke(entity, currentId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}