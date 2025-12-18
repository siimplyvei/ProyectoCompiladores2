.data
__strbuf: .space 256
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
addi $t0, $sp, 0
lw $t1, 0($t0)
move $a0, $t1
jal __print_int
jal __print_newline
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
li $t3, 5
move $a0, $t3
jal fill
li $t5, 0
move $v0, $t5
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
