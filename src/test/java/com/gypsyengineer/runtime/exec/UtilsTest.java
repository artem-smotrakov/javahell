package com.gypsyengineer.runtime.exec;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void testCommandInjection() throws IOException {
        String output = Utils.cowsay("moo ; echo oops");
        System.out.println(output);
        assertTrue(output.contains("moo ; echo oops"));
        assertFalse(output.endsWith("oops"));

        output = Utils.cowsay("moo && echo oops");
        assertTrue(output.contains("moo && echo oops"));
        assertFalse(output.endsWith("oops"));

        output = Utils.cowsay("moo | echo oops");
        assertTrue(output.contains("moo | echo oops"));
        assertFalse(output.endsWith("oops"));

        output = Utils.cowsay("moo \n echo oops");
        assertTrue(output.contains("echo oops"));
        assertFalse(output.endsWith("oops"));
    }
}

