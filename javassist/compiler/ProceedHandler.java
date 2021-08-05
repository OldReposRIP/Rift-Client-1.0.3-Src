package javassist.compiler;

import javassist.bytecode.Bytecode;
import javassist.compiler.ast.ASTList;

public interface ProceedHandler {

    void doit(JvstCodeGen jvstcodegen, Bytecode bytecode, ASTList astlist) throws CompileError;

    void setReturnType(JvstTypeChecker jvsttypechecker, ASTList astlist) throws CompileError;
}
