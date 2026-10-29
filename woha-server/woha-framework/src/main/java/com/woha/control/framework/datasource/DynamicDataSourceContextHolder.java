package com.woha.control.framework.datasource;

import java.util.ArrayDeque;
import java.util.Deque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据源切换处理
 * 
 * @author ruoyi
 */
public class DynamicDataSourceContextHolder
{
    public static final Logger log = LoggerFactory.getLogger(DynamicDataSourceContextHolder.class);

    /**
     * 使用ThreadLocal维护变量，ThreadLocal为每个使用该变量的线程提供独立的变量副本，
     * 所以每一个线程都可以独立地改变自己的副本，而不会影响其它线程所对应的副本。
     */
    /** 按调用层级保存数据源，确保嵌套调用结束后恢复外层数据源。 */
    private static final ThreadLocal<Deque<String>> CONTEXT_HOLDER = ThreadLocal.withInitial(ArrayDeque::new);

    /**
     * 设置数据源的变量
     */
    public static void setDataSourceType(String dsType)
    {
        log.info("切换到{}数据源", dsType);
        CONTEXT_HOLDER.get().push(dsType);
    }

    /**
     * 获得数据源的变量
     */
    public static String getDataSourceType()
    {
        Deque<String> dataSourceStack = CONTEXT_HOLDER.get();
        return dataSourceStack.peek();
    }

    /**
     * 清空数据源变量
     */
    public static void clearDataSourceType()
    {
        Deque<String> dataSourceStack = CONTEXT_HOLDER.get();
        dataSourceStack.poll();
        if (dataSourceStack.isEmpty())
        {
            CONTEXT_HOLDER.remove();
        }
    }
}
