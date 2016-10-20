package com.gypsyengineer.innerclass.field;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class AccessPrivateField {

    public static ClassLoader NO_PARENT_CLASSLOADER = null;

    public static void main(String[] args) throws Exception {
        String classpath = System.getProperty("java.class.path");
        System.out.println("classpath = " + classpath);

        // we assume that classpath contains only one element
        String url = "file://" + (new File(classpath).getAbsolutePath()) + "/";

        URL[] urls = new URL[] { new URL(url) };
        URLClassLoader cl = new URLClassLoader(urls, NO_PARENT_CLASSLOADER);

        // make sure that we run with security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        test01();
        test02(cl);
    }

    public static void test01() throws Exception {
        System.out.println("Test #1: try to modify a private field with the same classloader");
        System.out.println("         (oops is expected)");

        Outer outer = new Outer();
        go(outer);
        outer.check();
    }

    public static void test02(ClassLoader cl) throws Exception {
        System.out.println("Test #2: try to modify a private field with different classloader");
        System.out.println("         (an exception is expected)");

        Class clazz = cl.loadClass(
            "com.gypsyengineer.innerclass.field.AccessPrivateField");

        // make sure that we loaded new class
        if (AccessPrivateField.class.equals(clazz)) {
            throw new RuntimeException("Couldn't load different AccessPrivateField with new class loader");
        }

        Outer b = new Outer();
        Method m = clazz.getDeclaredMethod("go", Object.class);
        m.invoke(null, b);
    }

    public static void go(Object t) throws Exception {
        Method m = t.getClass().getDeclaredMethod(
                "access$002", t.getClass(), int.class);
        m.invoke(null, t, -1);
    }
}
