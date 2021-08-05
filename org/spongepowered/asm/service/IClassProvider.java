package org.spongepowered.asm.service;

import java.net.URL;

public interface IClassProvider {

    URL[] getClassPath();

    Class findClass(String s) throws ClassNotFoundException;

    Class findClass(String s, boolean flag) throws ClassNotFoundException;

    Class findAgentClass(String s, boolean flag) throws ClassNotFoundException;
}
