.data
__strbuf: .space 256
m: .space 400
.str0: .asciiz " "
.text
.globl main
j main
fill:
addi $sp, $sp, -32
sw $ra, 28($sp)
sw $a0, 0($sp)
sw $a1, 4($sp)
sw $a2, 8($sp)
sw $a3, 12($sp)
addi $t0, $sp, -4
li $t2, 1
sw $t2, 0($t0)
addi $t3, $sp, -8
li $t5, 1
sw $t5, 0($t3)
L1:
addi $t6, $sp, -8
lw $t7, 0($t6)
addi $t8, $sp, 0
lw $t9, 0($t8)
slt $t0, $t9, $t7
xori $t0, $t0, 1
beq $t0, $zero, L2
addi $t1, $sp, -12
li $t3, 1
sw $t3, 0($t1)
L3:
addi $t4, $sp, -12
lw $t5, 0($t4)
addi $t6, $sp, 0
lw $t7, 0($t6)
slt $t8, $t7, $t5
xori $t8, $t8, 1
beq $t8, $zero, L4
addi $t9, $sp, -4
lw $t0, 0($t9)
li $t2, 3
mul $t3, $t0, $t2
li $t5, 7
add $t6, $t3, $t5
li $t8, 20
div $t6, $t8
mfhi $t9
addi $t0, $sp, -8
lw $t1, 0($t0)
addi $t2, $sp, -12
lw $t3, 0($t2)
li $t5, 10
mul $t6, $t1, $t5
add $t7, $t6, $t3
li $t9, 4
mul $t0, $t7, $t9
la $t1, m
add $t2, $t1, $t0
sw $t9, 0($t2)
addi $t3, $sp, -4
lw $t4, 0($t3)
li $t6, 1
add $t7, $t4, $t6
addi $t8, $sp, -4
sw $t7, 0($t8)
addi $t9, $sp, -12
lw $t0, 0($t9)
li $t2, 1
add $t3, $t0, $t2
addi $t4, $sp, -12
sw $t3, 0($t4)
j L3
L4:
addi $t5, $sp, -8
lw $t6, 0($t5)
li $t8, 1
add $t9, $t6, $t8
addi $t0, $sp, -8
sw $t9, 0($t0)
j L1
L2:
lw $ra, 28($sp)
addi $sp, $sp, 32
jr $ra
main:
addi $sp, $sp, -32
sw $ra, 28($sp)
sw $a0, 0($sp)
sw $a1, 4($sp)
sw $a2, 8($sp)
sw $a3, 12($sp)
li $t2, 5
move $a0, $t2
jal fill
addi $t3, $sp, -8
li $t5, 1
sw $t5, 0($t3)
L5:
addi $t6, $sp, -8
lw $t7, 0($t6)
li $t9, 5
slt $t0, $t9, $t7
xori $t0, $t0, 1
beq $t0, $zero, L6
addi $t1, $sp, -12
li $t3, 1
sw $t3, 0($t1)
L7:
addi $t4, $sp, -12
lw $t5, 0($t4)
li $t7, 5
slt $t8, $t7, $t5
xori $t8, $t8, 1
beq $t8, $zero, L8
addi $t9, $sp, -8
lw $t0, 0($t9)
addi $t1, $sp, -12
lw $t2, 0($t1)
li $t4, 10
mul $t5, $t0, $t4
add $t6, $t5, $t2
li $t8, 4
mul $t9, $t6, $t8
la $t0, m
add $t1, $t0, $t9
lw $t2, 0($t1)
move $a0, $t2
jal __print_int
la $a0, .str0
jal __print_string
addi $t3, $sp, -12
lw $t4, 0($t3)
li $t6, 1
add $t7, $t4, $t6
addi $t8, $sp, -12
sw $t7, 0($t8)
j L7
L8:
jal __print_newline
addi $t9, $sp, -8
lw $t0, 0($t9)
li $t2, 1
add $t3, $t0, $t2
addi $t4, $sp, -8
sw $t3, 0($t4)
j L5
L6:
li $t6, 0
move $v0, $t6
li $v0, 10
syscall

__print_int:
li $v0, 1
syscall
jr $ra
__print_char:
li $v0, 11
syscall
jr $ra
__print_string:
li $v0, 4
syscall
jr $ra
__read_int:
li $v0, 5
syscall
jr $ra
__read_char:
li $v0, 12
syscall
jr $ra
__read_string:
# $a0 = buffer
# $a1 = length
li $v0, 8
syscall
jr $ra
__print_newline:
li $a0, 10
li $v0, 11
syscall
jr $ra
