package com.gypsyengineer.innerclass.field;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/*
 * This example shows how a private field of a class can be accessed
 * by other classes in the same package if the class contais an inner class.
 *
 * How to compile and run:
 *
 *      mkdir -p classes
 *      javac -d classes src/com/gypsyengineer/innerclass/field/*.java
 *
 *      # run without security manager
 *      java -classpath classes com.gypsyengineer.innerclass.field.AccessPrivateField
 *
 *      # run with security manager
 *      java -classpath classes com.gypsyengineer.innerclass.field.AccessPrivateField security
 */
public class AccessPrivateField {

    public static ClassLoader NO_PARENT_CLASSLOADER = null;

    public static void main(String[] args) throws Exception {

        // Reading system properties, getting an absolute path, creating a classloader
        // require security permissions. So, we do it before setting a security manager

        String classpath = System.getProperty("java.class.path");
        System.out.println("classpath = " + classpath);

        // We assume that classpath contains only one element
        String url = "file://" + (new File(classpath).getAbsolutePath()) + "/";

        // Create a new classloader for test #2
        // The classloader doesn't have a parent classloader
        // to prevent class loading delegation
        URL[] urls = new URL[] { new URL(url) };
        URLClassLoader cl = new URLClassLoader(urls, NO_PARENT_CLASSLOADER);

        // Make sure that we run with security manager
        if (args.length > 0 && "security".equals(args[0])
                && System.getSecurityManager() == null) {

            System.setSecurityManager(new SecurityManager());
        }

        // Run actual test cases

        test01();
        test02(cl);
    }

    public static void test01() throws Exception {
        System.out.println("Test #1: try to modify a private field with the same classloader");
        System.out.println("         (no exception is expected, 'oops' should be printed out)");

        Outer outer = new Outer();
        go(outer);
        outer.check();
    }

    public static void test02(ClassLoader cl) throws Exception {
        System.out.println("Test #2: try to modify a private field with different classloader");
        System.out.println("         (an exception is expected)");

        // Load AccessPrivateField class with different classloader
        Class clazz = cl.loadClass(
            "com.gypsyengineer.innerclass.field.AccessPrivateField");

        // Make sure that we loaded a new class
        if (AccessPrivateField.class.equals(clazz)) {
            throw new RuntimeException("Couldn't load different AccessPrivateField with new class loader");
        }

        // Get "go" method from clazz, and run with an instance of Outer
        // Note that Outer class and clazz were loaded by different classloaders
        Outer outer = new Outer();
        Method m = clazz.getDeclaredMethod("go", Object.class);
        m.invoke(null, outer);
    }

    // Modify a private field by calling a synthetic method
    public static void go(Object t) throws Exception {
        Method m = t.getClass().getDeclaredMethod(
                "access$002", t.getClass(), int.class);
        m.invoke(null, t, -1);
    }
}
