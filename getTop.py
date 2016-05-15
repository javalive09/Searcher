from pyquery import PyQuery as pq
#baidu realtime hotspot
print '\n'.join(map(lambda x:x.text, pq(url='http://top.baidu.com/buzz.php?p=top10')('.key').find('a')))
#sogou top 10 days
print '\n'.join(map(lambda x:x.text, pq(url='http://top.sogou.com/hotword0.html')('li').find('a')))
