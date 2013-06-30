interface MemberClassRefinement {
    abstract class Abstract() {
        shared formal class Inner() {
            shared formal String do();
        }
    }
    class Concrete() 
            extends Abstract() {
        shared actual class Inner() 
                extends super.Inner() {
            shared actual String do() { return "hello"; }
        }
    }
    class BadConcrete() 
            extends Abstract() {
        @error shared actual class Inner() 
                extends super.Inner() {}
    }
    @error class BrokenConcrete() 
            extends Abstract() {}
}

class ClassDefaultMemberClassWithTypeParams<T>() {
    shared default class Member<U>() {}
}
class ClassDefaultMemberClassWithTypeParams_sub<X>() 
        extends ClassDefaultMemberClassWithTypeParams<X>() {
    shared actual class Member<Y>() 
            extends super.Member<Y>() {}
}

abstract class Many<Element>() {
    formal shared class Iterator() {
        formal shared Element next();
    }
}

class Singleton(String s) 
        extends Many<String>() {
    shared actual class Iterator()
            extends super.Iterator() {
        shared actual String next() {
            return s;
        }
    }
    shared class Alias() => Iterator();
}

void testSin() {
    print(Singleton("hello").Iterator().next());
    @type:"Singleton.Iterator" Singleton("hello").Iterator();
    Singleton.Iterator i1 = Singleton("hello").Iterator();
    @type:"Singleton.Alias" Singleton("goodbye").Alias();
    Singleton.Iterator i2 = Singleton("goodbye").Alias();
}

@error class SingletonAlias() => Singleton.Iterator();
@error class Alias() => Many<Integer>.Iterator();

interface InterfaceFormalMemberClass {
    shared formal class Member() {}
}

class InterfaceFormalMemberClass1() satisfies InterfaceFormalMemberClass {
    shared actual default class Member() extends InterfaceFormalMemberClass.Member(super)() {}
}
class InterfaceFormalMemberClass2() extends InterfaceFormalMemberClass1() {
    @error shared actual class Member() extends InterfaceFormalMemberClass.Member(super)() {}
}
class InterfaceFormalMemberClass3() extends InterfaceFormalMemberClass1() {
    shared actual class Member() extends InterfaceFormalMemberClass1.Member(super)() {}
}

interface OuterInter<T> {
    shared interface SuperInter {}
    shared class SuperClass() {}
}

@error class BrokenToplevelClass() extends OuterInter<Integer>.SuperClass() {}
@error interface BrokenToplevelInterface satisfies OuterInter<Integer>.SuperInter {}

class OuterClass() satisfies OuterInter<String> {
    shared interface SubInter satisfies OuterInter<String>.SuperInter {}
    @error shared interface BrokenInter satisfies OuterInter<Integer>.SuperInter {}
    shared class SubClass() extends OuterInter<String>.SuperClass(super)() {}
    @error shared class BrokenClass() extends OuterInter<Integer>.SuperClass(super)() {}
    @error shared class BrokenClass0() extends OuterInter<String>.SuperClass() {}
    @error shared class BrokenClass1() extends OuterInter<String>.SuperClass(super) {}
    @error shared class BrokenClass2() extends OuterInter<String>.SuperClass {}
    @error shared class BrokenClass3() extends OuterInter<String>.SuperClass(this)() {}
    @error shared class BrokenClass4() extends OuterInter<String>.SuperClass(super)(1) {}
} 