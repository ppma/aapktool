package com.ppma.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Utile {
    public Utile() {
        ;
    }

    public static Method getMethod(String className, String methodName, Class<?>... parameterTypes) {
        Class clazz;
        try {
            return getMethod(Class.forName(className), methodName, parameterTypes);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Method getMethod(Class clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object runMethod(Object object, Method method, Object... args) {
        method.setAccessible(true);

        try {
            return method.invoke(object, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            return null;
        }
    }
}
