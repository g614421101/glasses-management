package com.glasses.config;

import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.UpdateListener;
import com.glasses.entity.Customer;
import com.glasses.entity.OperationLog;
import com.glasses.entity.OptometryRecord;
import com.glasses.entity.SalesRecord;
import com.glasses.entity.SysUser;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Set;

@Component
public class EntityFillListener implements InsertListener, UpdateListener {

    private static final Set<Class<?>> INSERT_FILL_ENTITIES = Set.of(
            Customer.class, SysUser.class, SalesRecord.class,
            OptometryRecord.class, OperationLog.class
    );

    private static final Set<Class<?>> UPDATE_FILL_ENTITIES = Set.of(
            Customer.class, SysUser.class, SalesRecord.class, OptometryRecord.class
    );

    @Override
    public void onInsert(Object entity) {
        if (!INSERT_FILL_ENTITIES.contains(entity.getClass())) {
            return;
        }
        Date now = new Date();
        setIfNull(entity, "createTime", now);
        setIfNull(entity, "updateTime", now);
    }

    @Override
    public void onUpdate(Object entity) {
        if (!UPDATE_FILL_ENTITIES.contains(entity.getClass())) {
            return;
        }
        setIfNull(entity, "updateTime", new Date());
    }

    private void setIfNull(Object entity, String fieldName, Object value) {
        try {
            Field field = findField(entity.getClass(), fieldName);
            if (field == null) {
                return;
            }
            field.setAccessible(true);
            if (field.get(entity) == null) {
                field.set(entity, value);
            }
        } catch (IllegalAccessException ignored) {
        }
    }

    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
