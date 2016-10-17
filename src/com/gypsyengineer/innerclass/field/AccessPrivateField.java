package com.gypsyengineer.innerclass.field;

import java.lang.reflect.Method;

public class AccessPrivateField {

    public static void main(String[] args) throws Exception {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        go();
    }

    public static void go() throws Exception {
        Outer t = new Outer();
        Method m = Outer.class.getDeclaredMethod(
                "access$002", Outer.class, int.class);
        m.invoke(null, t, -1);
        t.check();
    }
}
