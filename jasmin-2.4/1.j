.class public Example
.super java/lang/Object

.field static a I
.field static p I
.field static y F
.field static x I
.method public static main([Ljava/lang/String;)V
.limit stack 100
.limit locals 100

ldc 42
putstatic Example/x I
ldc 3.14
putstatic Example/y F
ldc 131
putstatic Example/x I
ldc 166.8
putstatic Example/y F
ldc true
putstatic Example/p I
ldc 1
putstatic Example/p I
ldc 1
putstatic Example/a I
ldc 1
putstatic Example/a I
goto LabelEnd0
LabelElse1:
ldc 2
putstatic Example/a I
LabelEnd0:
ldc 1
putstatic Example/a I
LabelEnd2:
WhileStart3:
getstatic Example/a I
ldc 3
if_icmpge WhileEnd4
getstatic Example/a I
ldc 1
iadd
putstatic Example/a I
getstatic Example/a I
ldc 2
iadd
putstatic Example/a I
goto WhileStart3
WhileEnd4:
ldc 1
istore 1
ForLoopStart5:
iload 1
ldc 5
if_icmpgt ForLoopEnd6
getstatic Example/a I
iload 1
iadd
putstatic Example/a I
iinc 1 1
goto ForLoopStart5
ForLoopEnd6:
return
.end method
