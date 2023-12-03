.class public Example
.super java/lang/Object

.field static a I
.field static x I
.field static y F
.field static p I
.method public static main([Ljava/lang/String;)V
.limit stack 100
.limit locals 100

ldc 42 
putstatic Example/x I
ldc 3.14 
putstatic Example/y F
ldc 9.3 
putstatic Example/x I
ldc 1 
putstatic Example/p I
ldc 1 
putstatic Example/a I
getstatic Example/a I
ldc 2
if_icmpgt LabelTrue2
iconst_0
goto LabelEnd3
LabelTrue2:
iconst_1
LabelEnd3:
ldc 1 
putstatic Example/a I
goto LabelEnd1
LabelElse0:
ldc 2 
putstatic Example/a I
LabelEnd1:
return
.end method
