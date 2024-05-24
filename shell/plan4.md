# 实现自己的shell

### Part 1

假设有以下文件 `1.txt` 记录着学长们悲惨的成绩：

```
zzw   环境编程    33
rzj   环境编程    55
lsh   网络编程    33
hzn   网络编程    55
zzy   数据结构    33
zt    计算机组成原理  55
lsh   计算机组成原理  55
zzy   计算机组成原理  55
xjj   数据结构    33
```

1. `cat 1.txt | awk '{print $1}' | sort | uniq -c | sort -r -n | head -n 5`

结果：	2 zzy
		2 lsh
		1 zzw
		1 zt
		1 xjj

**管道( | )：连接左边进程的输出和右边进程的输入**

`cat 1.txt`：将文件1.txt的内容打印到标准输出设备上，但其后跟了管道，表示输出结果直接作为下一条命令的输入。

`awk '{print $1}'`：每行按空格或TAB分割，输出文本中第1项。

`sort`：以行为单位对文本文件内容排序。

`uniq -c`：在每列旁边显示该行重复出现的次数。

`sort -r -n`：按照数值的大小，以相反的顺序排序。

`head -n 5`：显示前5行的内容。



2. `grep "rzj" > 2.txt < 1.txt`

结果：在2.txt文件中写入了 `rzj   环境编程    55`

| 命令            | 说明                                               |
| --------------- | -------------------------------------------------- |
| command > file  | 将输出重定向到 file。                              |
| command < file  | 将输入重定向到 file。                              |
| command >> file | 将输出以追加的方式重定向到 file。                  |
| n > file        | 将文件描述符为 n 的文件重定向到 file。             |
| n >> file       | 将文件描述符为 n 的文件以追加的方式重定向到 file。 |
| n >& m          | 将输出文件 m 和 n 合并。                           |
| n <& m          | 将输入文件 m 和 n 合并。                           |
| << tag          | 将开始标记 tag 和结束标记 tag 之间的内容作为输入。 |

**文件描述符 0 通常是标准输入（STDIN），1 是标准输出（STDOUT），2 是标准错误输出（STDERR）。**

`grep "rzj"`：在文件中查找`rzj`。

如果希望对 stdin 和 stdout 都重定向，可以这样写：

```
$ command < file1 > file2
```

这里这条命令将输入重定向到1.txt，将输出重定向到2.txt。


3.`echo "the answer is 42" > 1.txt`

结果：文件1.txt清空，被写入了 `the answer is 42`

`echo "the answer is 42"`：输出 `the answer is 42`


### part 2
