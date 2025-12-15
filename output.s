.text
.globl main
main:
addi $sp, $sp, -32
sw $ra, 28($sp)
add:
addi $sp, $sp, -32
sw $ra, 28($sp)
lw $t8, -4($sp)
lw $t8, -8($sp)
add $t0, $t8, $t8
move $v0, $t0
lw $ra, 28($sp)
addi $sp, $sp, 32
jr $ra
main:
addi $sp, $sp, -32
sw $ra, 28($sp)
li $t9, 3
move $a0, $t9
li $t9, 4
move $a1, $t9
jal add
move $t1, $v0
sw $t1, -12($sp)
lw $t8, -12($sp)
move $v0, $t8
lw $ra, 28($sp)
addi $sp, $sp, 32
jr $ra
lw $ra, 28($sp)
addi $sp, $sp, 32
jr $ra
