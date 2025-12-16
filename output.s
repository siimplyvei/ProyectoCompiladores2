.text
.globl main
main:
addi $sp, $sp, -32
sw $ra, 28($sp)
addi $t0, $sp, -4
li $t9, 1
sw $t9, 0($t0)
L1:
addi $t1, $sp, -4
lw $t2, 0($t1)
li $t9, 3
slt $t3, $t9, $t2
xori $t3, $t3, 1
beq $t3, $zero, L2
addi $t4, $sp, -4
lw $t5, 0($t4)
li $t9, 1
add $t6, $t5, $t9
addi $t7, $sp, -4
sw $t6, 0($t7)
addi $t8, $sp, -4
lw $t9, 0($t8)
li $t9, 1
add $t0, $t9, $t9
addi $t1, $sp, -4
sw $t0, 0($t1)
j L1
L2:
addi $t2, $sp, -4
lw $t3, 0($t2)
move $v0, $t3
li $v0, 10
syscall
