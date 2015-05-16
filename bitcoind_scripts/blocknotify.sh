#!/bin/bash
if [ ${#1} -lt 1 ]
then
  exit -1
fi

# We know that the len(txid) >= 1
echo "[$(date)] blocknotify.sh: $1" >> /home/main/notify.log