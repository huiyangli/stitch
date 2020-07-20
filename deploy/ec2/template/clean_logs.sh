#!/bin/bash
echo "Remove logs and results"
rm -f /tmp/*.log
rm -f /tmp/*.log.gz
rm -f ~/*.log
rm -f ~/*.log.gz
rm -rf /root/requestInfoScheduler.txt
rm -rf /root/RequestInfoMaster.txt
rm -rf /root/tasksInfoWorker.txt
rm -rf /root/*.info
