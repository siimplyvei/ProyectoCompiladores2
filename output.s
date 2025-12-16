.text
.globl main
main:
addi $sp, $sp, -32
sw $ra, 28($sp)
main:
addi $sp, $sp, -32
sw $ra, 28($sp)
li $t9, 1
sw $t9, -4($sp)
L1:
lw $t8, -4($sp)
li $t9, 3
slt $t0, $t9, $t8
xori $t0, $t0, 1
beq $t0, $zero, L2
lw $t8, -4($sp)
li $t9, 1
add $t1, $t8, $t9
sw $t1, -4($sp)
lw $t8, -4($sp)
li $t9, 1
add $t2, $t8, $t9
sw $t2, -4($sp)
j L1
L2:
lw $t8, -4($sp)
move $v0, $t8
lw $ra, 28($sp)
addi $sp, $sp, 32
jr $ra
lw $ra, 28($sp)
addi $sp, $sp, 32
jr $ra
