.class public GeneratedClass
.super java/lang/Object

.field static x I
.field static a I
.field static y F
.method public static main([Ljava/lang/String;)V
.limit stack 100
.limit locals 100

ldc 42 
putstatic GeneratedClass/x I
ldc 3.14 
putstatic GeneratedClass/y F
ldc 131 
putstatic GeneratedClass/x I
ldc 166.8 
putstatic GeneratedClass/y F
ldc 1 
putstatic GeneratedClass/a I
ldc 1 
putstatic GeneratedClass/a I
goto LabelEnd0
LabelElse1:
ldc 2 
putstatic GeneratedClass/a I
LabelEnd0:
ldc 1 
putstatic GeneratedClass/a I
LabelEnd2:
WhileStart3:
getstatic GeneratedClass/a I
ldc 3
if_icmpge WhileEnd4
getstatic GeneratedClass/a I
ldc 1 
iadd
putstatic GeneratedClass/a I
getstatic GeneratedClass/a I
ldc 2 
iadd
putstatic GeneratedClass/a I
goto WhileStart3
WhileEnd4:
ldc 1
istore 1
ForLoopStart5:
iload 1
ldc 5
if_icmpgt ForLoopEnd6
getstatic GeneratedClass/a I
iload 1
iadd
putstatic GeneratedClass/a I
iinc 1 1
goto ForLoopStart5
ForLoopEnd6:
return
.end method
