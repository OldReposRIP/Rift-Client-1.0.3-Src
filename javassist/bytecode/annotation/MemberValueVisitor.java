package javassist.bytecode.annotation;

public interface MemberValueVisitor {

    void visitAnnotationMemberValue(AnnotationMemberValue annotationmembervalue);

    void visitArrayMemberValue(ArrayMemberValue arraymembervalue);

    void visitBooleanMemberValue(BooleanMemberValue booleanmembervalue);

    void visitByteMemberValue(ByteMemberValue bytemembervalue);

    void visitCharMemberValue(CharMemberValue charmembervalue);

    void visitDoubleMemberValue(DoubleMemberValue doublemembervalue);

    void visitEnumMemberValue(EnumMemberValue enummembervalue);

    void visitFloatMemberValue(FloatMemberValue floatmembervalue);

    void visitIntegerMemberValue(IntegerMemberValue integermembervalue);

    void visitLongMemberValue(LongMemberValue longmembervalue);

    void visitShortMemberValue(ShortMemberValue shortmembervalue);

    void visitStringMemberValue(StringMemberValue stringmembervalue);

    void visitClassMemberValue(ClassMemberValue classmembervalue);
}
