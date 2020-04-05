# 分布式系统第四次实验思路说明

西安电子科技大学 17030140014 张笑天

******

[代码地址](https://github.com/EnigmaZhang/DistributedSystemHomework/)

## 基础设计

简单的Python Webservice，使用suds包。实际上request用HTTP也可以实现。

## 问题与解决

1. suds包无法pip安装问题

suds包已不维护，使用pip安装报错

解决方案：使用社区维护的suds-community包

2. 构建Client时报错无"http://www.w3.org/2001/XMLSchema"的模式问题
   
原因推测与WebService构建有关

经过查询，可以通过过滤解决

参考：
https://www.cnblogs.com/elephanyu/p/9136556.html
https://stackoverflow.com/questions/4719854/soap-suds-and-the-dreaded-schema-type-not-found-error


解决代码：

```python
from suds.xsd.doctor import ImportDoctor, Import
from suds import sudsobject

imp = Import('http://www.w3.org/2001/XMLSchema',
             location='http://www.w3.org/2001/XMLSchema.xsd')
imp.filter.add('http://WebXml.com.cn/')
doctor = ImportDoctor(imp)
client = Client(url, doctor=doctor)
```

3. 服务返回值问题

最终结果为suds的ArrayOfString类型，不太适合用户观看与之后的数据处理

并且结果中存在xx.gif字符串，应该为图片无法传输便只传输了名称的原因

解决方法：转换为字典，取出其中字符串转换为列表，并且过滤掉含gif的字符串，最终循环显示

解决代码：

```python
result_list = [i for i in sudsobject.asdict(result)["string"] if "gif" not in i]
for i in result_list:
    print(i)
```

## 实验结果展示

客户端
```text
Please input the city name.
北京
直辖市 北京
北京
792
2020/04/05 22:12:04
今日天气实况：气温：13℃；风向/风力：南风 1级；湿度：24%
紫外线强度：很强。空气质量：中。
紫外线指数：很强，涂擦SPF20以上，PA++护肤品，避强光。
健臻·血糖指数：较易波动，血糖较易波动，注意监测。
穿衣指数：较舒适，建议穿薄外套或牛仔裤等服装。
洗车指数：适宜，天气较好，适合擦洗汽车。
空气污染指数：中，易感人群应适当减少室外活动。

4月5日 晴
6℃/19℃
南风小于3级
4月6日 多云
9℃/23℃
南风小于3级转东北风3-4级
4月7日 多云转晴
4℃/18℃
东北风转南风小于3级
4月8日 晴转多云
5℃/16℃
南风转西南风小于3级
4月9日 多云
5℃/15℃
南风转西南风小于3级
Please input the city name.
上海
直辖市 上海
上海
2013
2020/04/05 22:02:20
今日天气实况：气温：10℃；风向/风力：静风 0级；湿度：61%
紫外线强度：强。空气质量：良。
紫外线指数：强，涂擦SPF大于15、PA+防晒护肤品。
健臻·血糖指数：易波动，气温多变，血糖易波动，请注意监测。
穿衣指数：较冷，建议着厚外套加毛衣等服装。
洗车指数：适宜，天气较好，适合擦洗汽车。
空气污染指数：良，气象条件有利于空气污染物扩散。

4月5日 多云转阴
9℃/16℃
东北风3-4级转小于3级
4月6日 多云转阴
10℃/17℃
东风3-4级转小于3级
4月7日 多云
11℃/19℃
东北风转北风小于3级
4月8日 多云转晴
10℃/19℃
东北风3-4级
4月9日 晴
11℃/18℃
东北风3-4级转东南风小于3级

```