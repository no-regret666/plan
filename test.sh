#!/bin/bash

mkdir -p ../build && cd ../build

cmake .. && make

cd ..
if [ $? -eq 0 ]; then
  if [ -x Strbuf ]; then
    ./Strbuf
    rm -f Strbuf
  else
    echo "编译错误，请检查错误信息"
    exit 1
  fi
else

  echo "构建失败，请检查错误信息"
  exit 1
fi
