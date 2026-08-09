package com.glasses.config;

import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.logicdelete.LogicDeleteProcessor;
import com.mybatisflex.core.logicdelete.impl.BooleanLogicDeleteProcessor;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import com.glasses.entity.Customer;
import com.glasses.entity.OperationLog;
import com.glasses.entity.OptometryRecord;
import com.glasses.entity.SalesRecord;
import com.glasses.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisFlexConfig implements MyBatisFlexCustomizer {

    @Autowired
    private EntityFillListener fillListener;

    @Override
    public void customize(FlexGlobalConfig globalConfig) {
        globalConfig.setLogicDeleteColumn("deleted");

        globalConfig.registerInsertListener(fillListener,
                Customer.class, SysUser.class, SalesRecord.class,
                OptometryRecord.class, OperationLog.class);

        globalConfig.registerUpdateListener(fillListener,
                Customer.class, SysUser.class, SalesRecord.class, OptometryRecord.class);
    }

    @Bean
    public LogicDeleteProcessor logicDeleteProcessor() {
        return new BooleanLogicDeleteProcessor();
    }
}
