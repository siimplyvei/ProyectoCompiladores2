.data
__strbuf: .space 256
.str0: .asciiz "Hola "
.text
.globl main
main:
addi $sp, $sp, -32
sw $ra, 28($sp)
la $a0, .str0
jal __print_string
li $t0, 123
move $a0, $t0
jal __print_int
jal __print_newline
li $t1, 0
move $v0, $t1
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
