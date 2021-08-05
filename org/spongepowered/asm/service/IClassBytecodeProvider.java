package org.spongepowered.asm.service;

import java.io.IOException;
import org.spongepowered.asm.lib.tree.ClassNode;

public interface IClassBytecodeProvider {

    byte[] getClassBytes(String s, String s1) throws IOException;

    byte[] getClassBytes(String s, boolean flag) throws ClassNotFoundException, IOException;

    ClassNode getClassNode(String s) throws ClassNotFoundException, IOException;
}
