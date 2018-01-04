#!/usr/bin/python
# -*- coding: UTF-8 -*-
import subprocess, os

from sys import argv

import json

script, code, url, num = argv

print "The script is called:", script
print "Your first variable is:", code
print "Your second variable is:", url
print "Your third variable is:", num

msg = []

#获取上次Tag
msgStr = subprocess.check_output("git tag", shell=True)
msgStrs = msgStr.split();
secondLastTag = msgStrs[-1]
print "Your secondLastTag is:", secondLastTag

#获取最近log
msgSt = subprocess.check_output("git log --oneline -1", shell=True)
firstLastTag = msgSt[0:8]
print "Your firstLastTag is:", firstLastTag

#获取变动的log msg信息
logCmd = 'git log ' + str(secondLastTag) + '..' + firstLastTag + '--oneline'
print "Your filter logCmd =============== :", logCmd
msgSt = subprocess.check_output(logCmd, shell=True)
print "Your filter log msg :", msgSt
msgSt = msgSt.split('\n');
print ">>>>>>>>>>>>>", msgSt
for item in msgSt:
    s = str(item)
    s = s[8:]

    if len(s) > 0:
        msg.append(s)
msg.append("优化性能及稳定性")
print msg

#搜集数据
data = {'code':code, 'url':url, 'num':num, 'msg': msg}

#data转成json file ensure_ascii=False 防止乱码
file = '/Users/peter/git/Searcher/searcher_update_info'
json.dump(data, open(file, 'wb'), ensure_ascii=False)

# 上传到服务器
subprocess.call("/usr/local/bin/coscmd upload " + file + " ./", shell=True)