.class Test
.super java/lang/Object

.field private x I
.field private y F

.method static <clinit>()V
    .limit stack 2
    .limit locals 0

    ; Save an integer value to the 'x' field
    ldc 42     ; Load the integer value 42 onto the stack
    putstatic Test/x I   ; Store the value into the 'x' field of the 'Test' class

    ; Save a float value to the 'y' field
    ldc 3.14   ; Load the float value 3.14 onto the stack
    putstatic Test/y F   ; Store the value into the 'y' field of the 'Test' class

    return
.end method

.method static main([Ljava/lang/String;)V
    .limit stack 2
    .limit locals 1

    ; Load a new integer value onto the stack
    ldc 9.3    ; Load the new integer value 9.3 onto the stack

    ; Store the new value into the 'x' field of the 'Test' class
    putstatic Test/x I

    return
.end method
