package com.npctranslator;
import net.minecraft.client.KeyMapping;
import java.lang.reflect.Constructor;
public class Test3 {
    public static void main(String[] args) {
        for (Constructor<?> c : KeyMapping.class.getConstructors()) {
            System.out.println(c);
        }
    }
}
