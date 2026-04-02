package redlib.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import redlib.backend.annotation.BackendModule;
import redlib.backend.annotation.NeedNoPrivilege;
import redlib.backend.annotation.Privilege;
import redlib.backend.model.Token;
import redlib.backend.service.utils.AdminUtils;
import redlib.backend.utils.ThreadContextHolder;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * 描述：对API接口的请求，除了login外，都做token检查，没有token则报错
 *
 * @author lihongwen
 * @date 2020/3/9
 */
@Component
@Aspect
@Slf4j
public class TokenCheckAspect {

    /**
     * 业务模块（这些模块对所有登录用户自动开放所有权限，无需在admin_priv中配置）
     * 包括：note（笔记功能）、category（分类功能）
     * 注意：admin模块需要权限检查，只有root能管理用户
     */
    private static final Set<String> BUSINESS_MODULES =
            Set.of("note", "category", "notification");

    @Before("execution(* redlib.backend.controller..*Controller.*(..))")
    public void processLog(JoinPoint joinPoint) throws Exception {
        Class clazz = joinPoint.getTarget().getClass();
        String methodName = joinPoint.getSignature().getName();
        Class[] parameterTypes = ((MethodSignature) joinPoint.getSignature()).getMethod().getParameterTypes();

        Method method = clazz.getMethod(methodName, parameterTypes);
        if (method.getAnnotation(NeedNoPrivilege.class) != null) {
            return;
        }

        if ("login".equals(joinPoint.getSignature().getName())) {
            return;
        }


        Privilege privilege = method.getAnnotation(Privilege.class);
        if (privilege != null && privilege.value().length == 0) {
            // @Privilege表示只要登录了就能访问，不需要特别权限
            ThreadContextHolder.getToken();
            return;
        }

        Token token = ThreadContextHolder.getToken();
        Assert.notNull(token, "您未登录，请重新登录");
        if ("ping".equals(joinPoint.getSignature().getName())) {
            return;
        }

        if ("root".equalsIgnoreCase(token.getUserCode())) {
            return;
        }

        BackendModule moduleAnnotation = (BackendModule) clazz.getAnnotation(BackendModule.class);
        Assert.notNull(moduleAnnotation, "访问的类没有Module注解");

        String className = AdminUtils.getModuleName(clazz);

        // 业务模块（note、category）对所有登录用户自动开放，无需检查admin_priv
        if (BUSINESS_MODULES.contains(className)) {
            return;
        }

        // 非业务模块，检查admin_priv权限
        Assert.notNull(privilege, "方法没有privilege注解，不能访问：" + className + '.' + methodName);

        // 兼容历史管理员行为：
        // 拥有模块 page 权限即视为可执行该模块管理操作（如 admin.add/update/delete、onlineUser.kick）。
        if ("admin".equals(className) && token.getPrivSet().contains("admin.page")) {
            return;
        }
        if ("onlineUser".equals(className) && token.getPrivSet().contains("onlineUser.page")) {
            return;
        }

        String[] privs = privilege.value();
        for (String s : privs) {
            if (token.getPrivSet().contains(className + '.' + s)) {
                return;
            }
        }

        throw new RuntimeException("您没有权限执行此操作");
    }
}
