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
