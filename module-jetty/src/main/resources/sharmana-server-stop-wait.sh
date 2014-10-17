#!/bin/bash
times=10
while [ -n "$(pgrep -f sharmana/server/runner.jar)" ] && [ $times -gt 0 ]; do
sleep 5;
let "times=$times-1"
done;
if [ -n "$(pgrep -f sharmana/server/runner.jar)" ]; then kill -9 $(pgrep -f sharmana/server/runner.jar); fi
