package com.gypsyengineer.innerclass.field;

public class Outer {

    private int secret = 10;

    public void check() {
        if (secret < 0) {
            System.out.println("Oops");
        } else {
            System.out.println("Okay");
        }
    }

    public class Inner {

        public void go(int value) {
            secret = value;
        }
    }

}
